package com.mppay.gateway.handler.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mppay.core.constant.BankCode;
import com.mppay.gateway.mq.sender.ISenderService;
import io.micrometer.core.instrument.util.StringUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.Align;
import com.mppay.core.constant.CardBindStatus;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.SeqIncrType;
import com.mppay.core.constant.TradeCode;
import com.mppay.core.constant.WithdrOrderStatus;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.AESCoder;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.gateway.handler.WithdrOrderBusiHandler;
import com.mppay.service.entity.BankRoute;
import com.mppay.service.entity.CardBind;
import com.mppay.service.entity.WithdrOrder;
import com.mppay.service.service.IBankRouteService;
import com.mppay.service.service.ICardBindService;
import com.mppay.service.service.IDictionaryService;
import com.mppay.service.service.IMasterAccountBalService;
import com.mppay.service.service.ISeqIncrService;
import com.mppay.service.service.IWithdrOrderService;
import com.mppay.service.service.common.ICipherService;

import lombok.extern.slf4j.Slf4j;

/**
 * 提现订单申请处理
 *
 * @author chenfeihang
 */
@Service("withdrApplyHandler")
@Slf4j
public class WithdrApplyHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler {

    @Value("${spring.rabbitmq.exchange}")
    private String exchange;
    @Value("${spring.rabbitmq.queues.withdr-order.routing-key}")
    private String withdrOrderroutingKye;

    @Autowired
    private IWithdrOrderService withdrOrderService;

    @Autowired
    private ISeqIncrService seqIncrService;

    @Autowired
    private IMasterAccountBalService masterAccountBalService;

    @Autowired
    private ICardBindService cardBindService;

    @Autowired
    private IBankRouteService bankRouteService;

    @Autowired
    private PlatformTransactionManager txManager;

    @Autowired
    private ICipherService cipherServiceImpl;

    @Autowired
    private ISenderService iSenderService;

    @Override
    public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        log.info("|提现申请|接收到请求报文:{}", JSON.toJSONString(requestMsg));
        String agrNo = (String) requestMsg.get("agrNo");
        String userOperNo = (String)requestMsg.get("userOperNo");
        String mercId = (String)requestMsg.get("mercId");
        String bankCardNo = (String)requestMsg.get("bankCardNo");
        String bankCode = (String)requestMsg.get("bankCode");
        //查出订单
        WithdrOrder withdrOrder = withdrOrderService.getOne(new QueryWrapper<WithdrOrder>().eq("merc_id", requestMsg.get("mercId")).eq("order_no", requestMsg.get("orderNo")));
        boolean operFlag = false;
        if (null == withdrOrder) {
            QueryWrapper<CardBind> cardBindQueryWrapper = new QueryWrapper<>();
            cardBindQueryWrapper.eq("user_oper_no", userOperNo)
                    .eq("status", CardBindStatus.BINDING.getId())
                    .eq("merc_id", mercId);
            //这里二选一
            if (StringUtils.isNotBlank(agrNo)) {
                agrNo = cipherServiceImpl.decryptAES(agrNo);
                requestMsg.put("agrNo", agrNo);
                cardBindQueryWrapper.eq("agr_no", agrNo);
            } else {
                cardBindQueryWrapper.eq("bank_card_no", bankCardNo)
                        .eq("bank_code", bankCode);
            }
            CardBind cardBind = cardBindService.getOne(cardBindQueryWrapper);

            if (null == cardBind) {
                log.error(ApplicationYmlUtil.get("15002") + requestMsg.toString());
                throw new BusiException(15002);
            }

            BankRoute bankRoute = bankRouteService.getOne(new QueryWrapper<BankRoute>()
                    .eq("bank_code", cardBind.getBankCode())
                    .eq("trade_code", TradeCode.WITHDRAW.getId())
                    .eq("bank_card_type", cardBind.getBankCardType())
                    .eq("merc_id", mercId)
                    .last(" limit 1")
            );

            requestMsg.put("bankCode", cardBind.getBankCode());
            requestMsg.put("bankCardNo", cardBind.getBankCardNo());
            requestMsg.put("bankCardName", cardBind.getBankCardName());
            requestMsg.put("bankCardType", cardBind.getBankCardType());
            requestMsg.put("tradeCode", TradeCode.WITHDRAW.getId());
            requestMsg.put("routeCode", bankRoute.getRouteCode());
            requestMsg.put("agrNo", cardBind.getAgrNo());

            // 开启事务管理
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus status = txManager.getTransaction(def);

            // 判断是否是微信提现,设置提现状态
            String orderStatus = WithdrOrderStatus.AUDIT.getId();
            bankCode = cardBind.getBankCode();
            if (BankCode.WEIXIN.getId().equals(bankCode)) {
                orderStatus = WithdrOrderStatus.WAIT.getId();
            }

            try {

                //计算失效时间
                String expTime = DateTimeUtil.formatTimestamp2String(DateTimeUtil.getPeriodTime(30, "02"), "yyyyMMddHHmmss");

                withdrOrder = new WithdrOrder();
                String withdrOrderNo = DateTimeUtil.date8() + seqIncrService.nextVal(SeqIncrType.WITHDR_ORDER_NO.getId(), 8, Align.LEFT);
                requestMsg.put("withdrOrderNo", withdrOrderNo);

                BeanUtils.populate(withdrOrder, requestMsg.getMap());
                withdrOrder.setOrderDate(DateTimeUtil.date10());
                withdrOrder.setOrderTime(DateTimeUtil.time8());
                withdrOrder.setOrderStatus(orderStatus);
                withdrOrder.setExpTime(expTime);
                // 实际提现金额
                BigDecimal withdrRatio = withdrOrder.getWithdrRatio();
                if (withdrRatio == null || BigDecimal.ZERO.equals(withdrRatio)){
                    withdrOrder.setWithdrPrice(withdrOrder.getPrice());
                }

                //拼出service name
                String serviceName = withdrOrder.getRouteCode().toLowerCase() + ConstEC.WITHDRORDERBUSIHANDLER;
                //通过spring ApplicationContext获取service对象
                WithdrOrderBusiHandler withdrOrderBusiHandler = (WithdrOrderBusiHandler) SpringContextHolder.getBean(serviceName);

                //获取外部商户信息
                ResponseMsg resultMsg = withdrOrderBusiHandler.getOutMercInfo(requestMsg);

                requestMsg.putAll(resultMsg.getMap());
                withdrOrder.setBankMercId((String) resultMsg.get("bankMercId"));
                withdrOrderService.save(withdrOrder);

                operFlag = masterAccountBalService.addWithdrUavaBal(withdrOrder);

                if (!operFlag) {
                    log.error("更新账户余额失败， user_no=" + withdrOrder.getUserNo() + ", withdrOrderNo=" + withdrOrder.getWithdrOrderNo() + ", price=" + withdrOrder.getPrice());
                    throw new BusiException(15101);
                }
                txManager.commit(status);

            } catch (BusiException e) {
                txManager.rollback(status);
                log.error("[" + requestMsg +"]业务处理失败, 异常信息", e);
                throw new BusiException(e.getCode(), e.getMsg(), e);
            } catch (Exception e) {
                txManager.rollback(status);
                log.error("[" + requestMsg +"]业务处理失败, 异常信息", e);
                
                throw new BusiException("11001", ApplicationYmlUtil.get("11001"), e);
            }

        }

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("mercId", withdrOrder.getMercId());
        data.put("orderNo", withdrOrder.getOrderNo());
        data.put("orderDate", withdrOrder.getOrderDate());
        data.put("orderTime", withdrOrder.getOrderTime());
        data.put("bankWithdrDate", withdrOrder.getBankWithdrDate());
        data.put("bankWithdrTime", withdrOrder.getBankWithdrTime());
        data.put("outTradeNo", withdrOrder.getOutTradeNo());
        data.put("price", withdrOrder.getPrice());
        data.put("withdrPrice", withdrOrder.getWithdrPrice());
        data.put("withdrRatio", withdrOrder.getWithdrRatio());
        data.put("bankCode", withdrOrder.getBankCode());
        data.put("bankCardNo", withdrOrder.getBankCardNo());
        data.put("bankCardName", withdrOrder.getBankCardName());
        data.put("userId", withdrOrder.getUserId());
        data.put("orderStatus", withdrOrder.getOrderStatus());

        responseMsg.put(ConstEC.DATA, data);
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);

        // 如果订单状态为W,放入消息队列处理
        if (WithdrOrderStatus.WAIT.getId().equals(withdrOrder.getOrderStatus())) {
            JSONObject msgJson = new JSONObject();
            msgJson.put("id", withdrOrder.getId());
            msgJson.put("orderNo", withdrOrder.getOrderNo());
            msgJson.put("withdrOrderNo", withdrOrder.getWithdrOrderNo());
            iSenderService.send(exchange, withdrOrderroutingKye, msgJson.toString());
        }
    }
}
