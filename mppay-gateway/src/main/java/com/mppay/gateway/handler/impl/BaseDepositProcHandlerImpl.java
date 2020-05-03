package com.mppay.gateway.handler.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mppay.core.constant.*;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.Sign;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.core.utils.HttpClientUtil;
import com.mppay.core.utils.RedisUtil;
import com.mppay.gateway.handler.DepositProcHandler;
import com.mppay.service.entity.*;
import com.mppay.service.service.*;
import com.mppay.service.service.common.ICipherService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 交易资金处理
 */
@Service("baseDepositProcHandler")
@Transactional(rollbackFor = Exception.class)
@Slf4j
public abstract class BaseDepositProcHandlerImpl implements DepositProcHandler {

    @Autowired
    protected ITradeOrderService tradeOrderService;

    @Autowired
    protected IMercOrderService mercOrderService;

    @Autowired
    protected ITradeRefundService tradeRefundService;

    @Autowired
    private ISeqIncrService seqIncrService;

    @Autowired
    private IMasterAccountBalService masterAccountBalService;

    @Autowired
    private IMercService mercService;

    @Autowired
    private IWithdrOrderService withdrOrderService;

    @Autowired
    private IQuickAgrService quickAgrService;

    @Autowired
    private ICipherService cipherService;
    @Autowired
    private IUserOperService userOperService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.exchange}")
    private String exchange;
    @Value("${spring.rabbitmq.queues.notify.routing-key}")
    private String routingKye;

    @Override
    abstract public void proc(Map<String, Object> data) throws Exception;

    @Override
    public void pre(Map<String, Object> data) throws Exception {

        TradeOrder tradeOrder = tradeOrderService.getOne(new QueryWrapper<TradeOrder>().eq("trade_no", data.get("tradeNo")));

        if (null == tradeOrder) {
            log.error("交易订单不存在tradeNo[{}]", data.get("tradeNo").toString());
            throw new BusiException("11110", ApplicationYmlUtil.get("11110"));
        }

        if (OrderStatus.SUCCESS.getId().equals(tradeOrder.getOrderStatus())
                || OrderStatus.REFUND_WAIT.getId().equals(tradeOrder.getOrderStatus())
                || OrderStatus.REFUND_FULL.getId().equals(tradeOrder.getOrderStatus())
                || OrderStatus.REFUND_PART.getId().equals(tradeOrder.getOrderStatus())) {
            log.error("交易订单号tradeNo[{}]状态[{}],直接返回", tradeOrder.getTradeNo(), tradeOrder.getOrderStatus());
            return;
        }

        if (!OrderStatus.WAIT_PAY.getId().equals(tradeOrder.getOrderStatus())) {
            log.error("交易订单号tradeNo[{}]状态[{}],异常返回", tradeOrder.getTradeNo(), tradeOrder.getOrderStatus());
            throw new BusiException(11002);
        }

        BigDecimal price = new BigDecimal(data.get("price").toString());

        if (price.compareTo(tradeOrder.getPrice()) != 0) {
            log.error("交易订单号tradeNo[{}], 银行返回金额[{}] != 交易金额[{}]", tradeOrder.getTradeNo(), price, tradeOrder.getPrice());
            throw new BusiException("11101", ApplicationYmlUtil.get("11101"));
        }

        BeanUtils.populate(tradeOrder, data);

        MercOrder mercOrder = after(tradeOrder, data);

        if (null != mercOrder && !StringUtils.isBlank(mercOrder.getNotifyUrl())
                && mercOrder.getOrderStatus().equals(OrderStatus.SUCCESS.getId())) {

            Merc merc = mercService.getOne(new QueryWrapper<Merc>().eq("merc_id", mercOrder.getMercId()));

            Map<String, Object> notifyMsg = new HashMap<String, Object>();
            try {
                notifyMsg.put("mercId", mercOrder.getMercId());
                notifyMsg.put("payNo", mercOrder.getOutTradeNo());
                notifyMsg.put("orderNo", mercOrder.getOrderNo());
                notifyMsg.put("orderStatus", OrderStatus.SUCCESS.getId());
                notifyMsg.put("userId", mercOrder.getUserId());
                notifyMsg.put("orderDate", mercOrder.getOrderDate());
                notifyMsg.put("orderTime", mercOrder.getOrderTime());
                notifyMsg.put("payDate", mercOrder.getPayDate());
                notifyMsg.put("payTime", mercOrder.getPayTime());
                notifyMsg.put("price", mercOrder.getPrice());
                notifyMsg.put("bankCode", mercOrder.getBankCode());
                notifyMsg.put("tradeType", mercOrder.getTradeType());
                notifyMsg.put("openId", data.get("openId"));
                notifyMsg.put("reducePrice", mercOrder.getReducePrice());

                String plain = Sign.getPlain(notifyMsg);
                plain += "&key=" + merc.getPrivateKey();
                String sign = Sign.sign(plain);

                Map<String, Object> headerMap = new HashMap<String, Object>();
                headerMap.put("X-MPMALL-SignVer", "v1");
                headerMap.put("X-MPMALL-Sign", sign);

                log.info("支付结果后台通知订单系统：url:{} ,header：{}，参数：{}", mercOrder.getNotifyUrl(), JSONUtil.toJsonStr(headerMap), JSONUtil.toJsonStr(notifyMsg));

                if (!StringUtils.isBlank(mercOrder.getNotifyUrl())) {
                    String result = HttpClientUtil.sendPostJson(mercOrder.getNotifyUrl(), notifyMsg, headerMap);
                    log.info("支付结果后台通知订单系统：outTradeNo:{}，result:{} ",mercOrder.getOutTradeNo() ,result);

                    Optional.ofNullable(result).orElseThrow(()->new BusiException(15201));
                    JSONObject obj = JSONObject.parseObject(result);
                    if (StringUtils.isBlank(obj.getString("code")) || !ConstEC.SUCCESS_10000.equals(obj.getString("code"))) {
                        throw new BusiException(15201);
                    }

                }
                
                RedisUtil.del(ConstEC.USER_REDUCE_PRICE + mercOrder.getUserOperNo());
            } catch (Exception e) {
                notifyMsg.put("mercId", mercOrder.getMercId());
                notifyMsg.put("orderNo", mercOrder.getOrderNo());

                log.info("支付结果失败订单系统，发送到MQ", notifyMsg);
                //支付结果后台通知
                rabbitTemplate.convertAndSend(exchange, routingKye, JSON.toJSONString(notifyMsg));
                log.error("notifyUrl[{}], notifyMsg[{}],支付结果通知订单系统失败", mercOrder.getNotifyUrl(), notifyMsg, e);
            }
        }

        String fundBank = (String) data.get("fundBank");

        if (!StringUtils.isBlank(fundBank) && BankCode.UPOP.getId().equals(fundBank)) {
            log.info("fundBank={}", fundBank);
            String bankCardNo = (String) data.get("openId");
            String bankCardNoAbbr = null;

            try {
                bankCardNoAbbr = bankCardNo;
                bankCardNoAbbr = bankCardNoAbbr.substring(0, 4) + "****" + bankCardNoAbbr.substring(bankCardNoAbbr.length() - 4, bankCardNoAbbr.length());

                bankCardNo = cipherService.encryptAES(bankCardNo);

            } catch (Exception e) {
                log.error("加密失败， bankCardNoAbbr={}", bankCardNoAbbr);
                throw new BusiException(31102);
            }

            log.info("bankCardNo={}", bankCardNo);

            UserOper userOper = userOperService.getOne(new QueryWrapper<UserOper>()
                    .eq("merc_id", mercOrder.getMercId()).eq("user_id", mercOrder.getUserId()));

            QuickAgr quickAgr = quickAgrService.getOne(new QueryWrapper<QuickAgr>()
                    .eq("user_no", userOper.getUserNo())
                    .eq("bank_card_type", BankCardType.UPOP.getId())
                    .eq("bank_card_no", bankCardNo)
                    .eq("status", 1)
            );
            log.info("quickAgr={}", quickAgr);
            if (null == quickAgr) {

                quickAgr = new QuickAgr();
                quickAgr.setMercId(mercOrder.getMercId());
                quickAgr.setBankCode(fundBank);

                quickAgr.setBankCardNoAbbr(bankCardNoAbbr);
                quickAgr.setBankCardNo(bankCardNo);

                String agrNo = DateTimeUtil.date8() + seqIncrService.nextVal(SeqIncrType.AGR_NO.getId(), SeqIncrType.AGR_NO.getLength(), Align.LEFT);
                quickAgr.setAgrNo(agrNo);

                quickAgr.setUserId(mercOrder.getUserId());
                quickAgr.setUserOperNo(mercOrder.getUserOperNo());
                quickAgr.setUserNo(userOper.getUserNo());
                quickAgr.setBankCardType(BankCardType.UPOP.getId());
                quickAgr.setBindDate(DateTimeUtil.date10());
                quickAgr.setBindTime(DateTimeUtil.time8());
                quickAgrService.save(quickAgr);

                log.info("新增绑卡协议，协议号agrNo={}", agrNo);
            }

        }

    }

    @Override
    public MercOrder after(TradeOrder tradeOrder, Map<String, Object> data) throws Exception {

        MercOrder mercOrder = mercOrderService.getOne(new QueryWrapper<MercOrder>().eq("merc_order_no", tradeOrder.getMercOrderNo()));

        boolean flagRefund = true;
        //如果商户订单的
        if (null != mercOrder && OrderStatus.WAIT_PAY.getId().equals(mercOrder.getOrderStatus())) {

            tradeOrder.setOrderStatus(OrderStatus.SUCCESS.getId());
            tradeOrderService.updateById(tradeOrder);

            BeanUtils.populate(mercOrder, data);
            mercOrder.setOrderStatus(OrderStatus.SUCCESS.getId());
            mercOrder.setPayDate(DateTimeUtil.date10());
            mercOrder.setPayTime(DateTimeUtil.time8());

            boolean operFlag = mercOrderService.update(mercOrder,
                    new UpdateWrapper<MercOrder>()
                            .eq("merc_order_no", tradeOrder.getMercOrderNo())
                            .eq("order_status", OrderStatus.WAIT_PAY.getId())
            );

            if (!operFlag) {
                flagRefund = true;
            } else {
                flagRefund = false;
            }

            if (operFlag && tradeOrder.getBusiType().equals(BusiType.INCOME.getId())) {
                operFlag = masterAccountBalService.addAcBal(tradeOrder);
                if (!operFlag) {
                    throw new BusiException("15101", ApplicationYmlUtil.get("15101") + JSON.toJSONString(tradeOrder));
                }
            } else if (operFlag && tradeOrder.getBusiType().equals(BusiType.SUBTRACT.getId())) {
                operFlag = masterAccountBalService.subtractAcBal(tradeOrder);
                if (!operFlag) {
                    throw new BusiException("15101", ApplicationYmlUtil.get("15101") + JSON.toJSONString(tradeOrder));
                }
            } else if (operFlag && tradeOrder.getBusiType().equals(BusiType.CASH.getId())) {
                operFlag = masterAccountBalService.addSctBal(tradeOrder);
                if (!operFlag) {
                    throw new BusiException("15102", ApplicationYmlUtil.get("15102") + JSON.toJSONString(tradeOrder));
                }
            } else if (operFlag && tradeOrder.getBusiType().equals(BusiType.SUBTRACTSCT.getId())) {
                operFlag = masterAccountBalService.subtractSctBal(tradeOrder);
                if (!operFlag) {
                    throw new BusiException("15102", ApplicationYmlUtil.get("15102") + JSON.toJSONString(tradeOrder));
                }
            }
        }

        if (flagRefund) {
            tradeOrder.setOrderStatus(OrderStatus.REFUND_FULL.getId());
            tradeOrder.setRefundPrice(tradeOrder.getPrice());

            tradeOrderService.updateById(tradeOrder);

            //登记交易退款明细表并且更新充值订单表的订单状态,在发往微信进行退款的时候还得登记微信退款明细表
            TradeRefund tradeRefund = new TradeRefund();
            tradeRefund.setMercId(tradeOrder.getMercId());
            tradeRefund.setOutTradeNo(tradeOrder.getOutTradeNo());
            tradeRefund.setTradeNo(tradeOrder.getTradeNo());
            tradeRefund.setBankTradeNo(tradeOrder.getBankTradeNo());
            tradeRefund.setBankCode(tradeOrder.getBankCode());
            tradeRefund.setRouteCode(tradeOrder.getRouteCode());
            tradeRefund.setFundBank(tradeOrder.getFundBank());
            tradeRefund.setTradeType(tradeOrder.getTradeType());
            tradeRefund.setRefundDate(DateTimeUtil.date10());
            tradeRefund.setRefundTime(DateTimeUtil.time8());
            tradeRefund.setTradeCode(TradeCode.TRADEREFUND.getId());
            // 退款登记
            tradeRefund.setOrderStatus(OrderStatus.REFUND.getId());

            tradeRefund.setApplyPrice(tradeOrder.getPrice());
            tradeRefund.setPrice(tradeOrder.getPrice());

            String refundNo = DateTimeUtil.date8() + seqIncrService.nextVal(SeqIncrType.REFUND_NO.getId(), SeqIncrType.REFUND_NO.getLength(), Align.LEFT);

            log.info("交易订单号outTradeNo[{}]发起退款,退款流水号refundNo[{}]", tradeOrder.getOutTradeNo(), refundNo);

            tradeRefund.setRefundNo(refundNo);
            tradeRefundService.save(tradeRefund);
            return null;
        }

        return mercOrder;
    }

    @Override
    public void preWithdr(Map<String, Object> data) throws Exception {
        //大订单
        WithdrOrder withdrOrder = withdrOrderService.getOne(new QueryWrapper<WithdrOrder>().eq("withdr_order_no", data.get("withdrOrderNo")));
        if (null == withdrOrder) {
            log.error("提现订单不存在withdrOrderNo[{}]", data.get("withdrOrderNo").toString());
            throw new BusiException("11110", ApplicationYmlUtil.get("11110"));
        }
        if (!WithdrOrderStatus.BANK_WAIT.getId().equals(withdrOrder.getOrderStatus())) {
            log.error("交易订单号withdrOrderNo[{}]状态[{}],异常返回", withdrOrder.getWithdrOrderNo(), withdrOrder.getOrderStatus());
            throw new BusiException(11002);
        }

        BigDecimal price = withdrOrder.getPrice();
        BeanUtils.populate(withdrOrder, data);
        withdrOrder.setPrice(price);
        //更新提现大订单为成功 。data里面有order_status=S
        boolean flag = withdrOrderService.update(withdrOrder, new UpdateWrapper<WithdrOrder>()
                .eq("withdr_order_no", data.get("withdrOrderNo"))
                .eq("order_status", WithdrOrderStatus.BANK_WAIT.getId())
        );

        if (!flag) {
            log.error("提现订单号withdrOrderNo[{}], 订单状态不等于[{}]", withdrOrder.getWithdrOrderNo(), WithdrOrderStatus.BANK_WAIT.getId());
            throw new BusiException("11101", ApplicationYmlUtil.get("11101"));
        }
        //减少提现不可用余额
        masterAccountBalService.subtractWithdrUavaBal(withdrOrder);

    }

    @Override
    abstract public void procWithdr(Map<String, Object> data) throws Exception;

    @Override
    public void afterWithdr(WithdrOrder withdrOrder, Map<String, Object> data) throws Exception {

    }

}
