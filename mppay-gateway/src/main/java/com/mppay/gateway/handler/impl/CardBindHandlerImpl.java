package com.mppay.gateway.handler.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.api.R;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.Align;
import com.mppay.core.constant.BankCode;
import com.mppay.core.constant.BankStatus;
import com.mppay.core.constant.CardBindStatus;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.NeedSmsStatus;
import com.mppay.core.constant.PlatformType;
import com.mppay.core.constant.RouteCode;
import com.mppay.core.constant.SeqIncrType;
import com.mppay.core.constant.SmsOrderAgrType;
import com.mppay.core.constant.SmsOrderStatus;
import com.mppay.core.constant.SmsOrderType;
import com.mppay.core.constant.TradeCode;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.AESCoder;
import com.mppay.core.sign.gaohuitong.GaohuitongConstants;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.gateway.handler.QuickBusiHandler;
import com.mppay.gateway.handler.utils.RequestMsgUtil;
import com.mppay.service.entity.Bank;
import com.mppay.service.entity.BankRoute;
import com.mppay.service.entity.CardBind;
import com.mppay.service.entity.SmsOrder;
import com.mppay.service.service.IBankRouteService;
import com.mppay.service.service.IBankService;
import com.mppay.service.service.ICardBindService;
import com.mppay.service.service.IDictionaryService;
import com.mppay.service.service.ISeqIncrService;
import com.mppay.service.service.ISmsOrderService;
import com.mppay.service.vo.CardBindVO;

import lombok.extern.slf4j.Slf4j;

/**
 * 提现绑卡
 */
@Service("cardBindHandler")
@Slf4j
public class CardBindHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler {

    @Autowired
    private IBankService bankService;

    @Autowired
    private IBankRouteService bankRouteService;

    @Autowired
    private ICardBindService cardBindService;

    @Autowired
    private ISeqIncrService seqIncrService;

    @Autowired
    private IDictionaryService dictionaryService;

    @Autowired
    private ISmsOrderService smsOrderService;

    @Value("${spring.rabbitmq.exchange}")
    private String exchange;
    @Value("${spring.rabbitmq.queues.withdrcardbind.routing-key}")
    private String withdrCardBindKey;
    
    @Override
    public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        log.info("|提现绑卡|cardBindHandler|requestMsg:{}", JSON.toJSONString(requestMsg));
        //校验下加密数据
        RequestMsgUtil.validateRequestMsg(requestMsg);

        String key = dictionaryService.findForString("SecretKey", "AES","0");
        String iv = dictionaryService.findForString("SecretKey", "IV","0");
        String bankCardNo = (String) requestMsg.get("bankCardNo");
        String bankCode = (String)requestMsg.get("bankCode");
        String mercId = (String)requestMsg.get("mercId");
        String userOperNo = (String)requestMsg.get("userOperNo");
        String bankCardType = (String)requestMsg.get("bankCardType");
        String bankCardImgFront = (String) requestMsg.get("bankCardImgFront");

        CardBind cardBind = cardBindService.getOne(new QueryWrapper<CardBind>()
                .eq("bank_card_no", bankCardNo.trim())
                .eq("bank_code", bankCode)
                .eq("status", CardBindStatus.BINDING.getId())
                .eq("user_oper_no", userOperNo)
        );

        if (null != cardBind) {
            log.error("银行卡号已绑定" + requestMsg.toString());
            throw new BusiException("15001", ApplicationYmlUtil.get("15001"));
        }
        
        //查询之前需要确认所绑的卡为借记卡
        RequestMsg requestMsgCard = new RequestMsg();
        ResponseMsg responseMsgCard = new ResponseMsg();
        
        requestMsgCard.put("mercId", PlatformType.MPMALL.getId());
        requestMsgCard.put("platform", PlatformType.MPMALL.getCode());
        requestMsgCard.put("routeCode", RouteCode.GAOHUITONG.getId());
        requestMsgCard.put("bankCardNo", AESCoder.decrypt(bankCardNo, key, iv));
        requestMsgCard.put("tradeCode", TradeCode.WITHDRAW.getId());
        
        if (!bankCode.equals(BankCode.ALIPAY.getId()) && !bankCode.equals(BankCode.WEIXIN.getId())) {
        	//拼出service name
            String serviceName = RouteCode.GAOHUITONG.getId().toLowerCase() + ConstEC.QUICKBUSIHANDLER;
            QuickBusiHandler handler = SpringContextHolder.getBean(serviceName);
            if (null == handler) {
                log.error("serviceName[{}]业务处理服务不存在!", serviceName);
                throw new BusiException(11114);
            }
            handler.queryCardBind(requestMsgCard, responseMsgCard);

            String returnCode = (String) responseMsgCard.get(ConstEC.RETURNCODE);
            String returnMsg = (String) responseMsgCard.get(ConstEC.RETURNMSG);

            log.info("|提现绑卡|cardBindHandler|queryCardBind|responseMsgCard:{}", JSON.toJSONString(responseMsgCard));
            if (StringUtils.isBlank(returnCode)) {
                throw new BusiException(11001);
            }

            if (!returnCode.trim().equals(ConstEC.SUCCESS_10000)) {
                throw new BusiException(returnCode, returnMsg);
            }
        }

        Bank bank = bankService.getOne(new QueryWrapper<Bank>()
                .eq("bank_code", bankCode)
                .eq("status", BankStatus.NORMAL.getId())
        );

        cardBind = new CardBind();

        // 提现绑卡 - 调用第三方支付机构
        BankRoute bankRoute = bankRouteService.getOne(new QueryWrapper<BankRoute>()
                .eq("bank_code", bankCode)
                .eq("bank_card_type", bankCardType)
                .eq("merc_id", mercId)
                .eq("trade_code", TradeCode.WITHDRAW.getId())
                .last(" limit 1")
        );

        if (bankRoute == null) {
            log.error("无对应的第三方支付平台路由信息");
            throw new BusiException(30002);
        }

        Integer cardBindStatus = CardBindStatus.BINDING.getId();
        Boolean rabbitSend = false;
        String routeCode = bankRoute.getRouteCode();

        //需要短信：高汇通
        if (routeCode.equals(RouteCode.GAOHUITONG.getId())) {
            cardBindStatus = CardBindStatus.CHECK.getId();
            rabbitSend = true;
        }

        String agrNo = "1" + seqIncrService.nextVal(SeqIncrType.CARD_BIND_AGR_NO.getId(), SeqIncrType.CARD_BIND_AGR_NO.getLength(), Align.LEFT);

        BeanUtils.populate(cardBind, requestMsg.getMap());
        cardBind.setBankAbbr(bank.getBankAbbr());
        cardBind.setStatus(cardBindStatus);
        cardBind.setBindDate(DateTimeUtil.date10());
        cardBind.setBindTime(DateTimeUtil.time8());
        cardBind.setAgrNo(agrNo);
        cardBind.setBankCardImgFront(bankCardImgFront);
        cardBindService.save(cardBind);

        CardBindVO cardBindVO = new CardBindVO();
        org.springframework.beans.BeanUtils.copyProperties(cardBind, cardBindVO);
        cardBindVO.setNeedSms(NeedSmsStatus.NO.getId());
        cardBindVO.setAgrNo(AESCoder.encrypt(agrNo, key, iv));

        if (rabbitSend) {
            SmsOrder smsOrder = new SmsOrder();
            BeanUtils.populate(smsOrder, requestMsg.getMap());
            smsOrder.setAgrNo(agrNo);
            String smsOrderNo = DateTimeUtil.date8() + seqIncrService.nextVal(SeqIncrType.SMS_ORDER_NO.getId(), SeqIncrType.SMS_ORDER_NO.getLength(), Align.LEFT);
            smsOrder.setSmsOrderNo(smsOrderNo);
            smsOrder.setStatus(SmsOrderStatus.CHECK.getId());
            smsOrder.setSmsOrderType(SmsOrderType.WITHDRAW_CARD_BIND.getId());
            smsOrder.setAgrType(SmsOrderAgrType.QUICK.getId());
            smsOrder.setRouteCode(routeCode);
            smsOrder.setNeedSms(NeedSmsStatus.YES.getId());
            smsOrder.setBindDate(DateTimeUtil.date10());
            smsOrder.setBindTime(DateTimeUtil.time8());
            smsOrderService.save(smsOrder);
            cardBindVO.setNeedSms(NeedSmsStatus.YES.getId());
            cardBindVO.setSmsOrderNo(smsOrderNo);
        }

        responseMsg.put("data", cardBindVO);
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        log.info("|提现绑卡|cardBindHandler|responseMsg:{}", JSON.toJSONString(responseMsg));
    }

}
