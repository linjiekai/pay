package com.mppay.gateway.handler.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.*;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.AESCoder;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.gateway.handler.PlatformBusiHandler;
import com.mppay.gateway.handler.WithdrOrderBusiHandler;
import com.mppay.service.entity.*;
import com.mppay.service.service.*;
import com.mppay.service.service.common.ICipherService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Optional;

/**
 * 提现绑卡确认:通知第三方平台
 */
@Slf4j
@Service("cardBindConfirmHandler")
public class CardBindConfirmHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler {

    @Autowired
    private IBankRouteService bankRouteService;
    @Autowired
    private ICardBindService cardBindService;
    @Autowired
    private ISmsOrderService smsOrderService;
    @Autowired
    private IUserOperService userOperService;
    @Autowired
    private IUserRealNameDetailsService userRealNameDetailsService;
    @Autowired
	private ICipherService cipherServiceImpl;
    
    /**
     * 提现绑卡确认
     *
     * @param requestMsg
     * @param responseMsg
     * @throws Exception
     */
    @Override
    public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        log.info("|提现绑卡确认|cardBindConfirmHandler|requestMsg:{}", JSON.toJSONString(requestMsg));
        String agrNoCipher = (String) requestMsg.get("agrNo");
        String agrNo = cipherServiceImpl.decryptAES(agrNoCipher);

        String smsOrderNo = (String) requestMsg.get("smsOrderNo");
        String smsCode = (String) requestMsg.get("smsCode");
        String mercId = (String) requestMsg.get("mercId");
        String userId = (String) requestMsg.get("userId");
        // 校验短信验证码
        SmsOrder smsOrder = smsOrderService.getOne(new QueryWrapper<SmsOrder>()
                .eq("sms_order_no", smsOrderNo)
                .eq("agr_no", agrNo)
                .eq("sms_code", smsCode));
        Optional.ofNullable(smsOrder).orElseThrow(() -> {
            log.error("|提现绑卡确认|获取短信订单信息|短信验证码错误|{}", requestMsg);
            return new BusiException(31024);
        });
        String expTimeStr = smsOrder.getExpTime();
        Timestamp expTimestamp = DateTimeUtil.formatString2Timestamp(expTimeStr, "yyyyMMddHHmmss");
        long expTime = expTimestamp.getTime();
        long nowTime = System.currentTimeMillis();
        long msmIntervalTime = nowTime - expTime;
        Integer intervalTime = 10 * 60 * 1000;
        if (msmIntervalTime > intervalTime) {
            log.error("|提现绑卡确认|获取短信订单信息|短信验证码失效:超过失效时间|{}", requestMsg);
            throw new BusiException(31023);
        }

        // 根据[agrNo]获取银行卡及身份证信息
        CardBind cardBind = cardBindService.getOne(new QueryWrapper<CardBind>().eq("agr_no", agrNo));
        Optional.ofNullable(cardBind).orElseThrow(() -> {
            log.error("|提现绑卡确认|获取绑卡信息|无对应的绑卡信息|{}", requestMsg);
            return new BusiException(15006);
        });
        Integer status = cardBind.getStatus();
        if (CardBindStatus.BINDING.equals(status)) {
            log.error("|提现绑卡确认|获取绑卡信息|银行卡已绑定|{}", requestMsg);
            throw new BusiException(15001);
        }

        // 实名认证
        UserOper userOper = userOperService.getOne(new QueryWrapper<UserOper>().eq("user_id", userId).eq("merc_id",mercId));
        Optional.ofNullable(userOper).orElseThrow(() -> new BusiException(11308));
        if(userOper.getRealed().intValue()<RealedStatus.WEAK_REAL.getId()){
            throw  new BusiException(11306);
        }
        //实名过拿出证件照，后面需要上传到第三方
        UserRealNameDetails userRealNameDetails = userRealNameDetailsService.getOne(new QueryWrapper<UserRealNameDetails>()
                .eq("user_id", userId)
                .eq("card_no", cardBind.getCardNo())
                .eq("merc_id", mercId)
                .orderByDesc("create_time")
                .last("limit 1")
        );
        String imgFront = userRealNameDetails.getImgFront();
        String imgBack = userRealNameDetails.getImgBack();
        requestMsg.setAttr("imgFront",imgFront);
        requestMsg.setAttr("imgBack",imgBack);

        // 提现绑卡 - 调用第三方支付机构 - 获取路由
        BankRoute bankRoute = bankRouteService.getOne(new QueryWrapper<BankRoute>()
                .eq("bank_code", cardBind.getBankCode())
                .eq("bank_card_type", cardBind.getBankCardType())
                .eq("merc_id", cardBind.getMercId())
                .eq("trade_code", TradeCode.WITHDRAW.getId())
                .last(" limit 1")
        );
        Optional.ofNullable(bankRoute).orElseThrow(() -> {
            log.error("|提现绑卡确认|获取路由信息|无对应的第三方支付平台路由信息" + JSON.toJSONString(requestMsg));
            return new BusiException(30002);
        });

        packageGHTReqMsg(cardBind, requestMsg);
        requestMsg.put("routeCode", smsOrder.getRouteCode());

        WithdrOrderBusiHandler withdrOrderBusiHandler = SpringContextHolder.getBean(bankRoute.getRouteCode().toLowerCase() + ConstEC.WITHDRORDERBUSIHANDLER);

        //绑卡前操作
        withdrOrderBusiHandler.beforeWithdrCardBind(requestMsg,responseMsg);

        // 商户银行卡信息登记
        ResponseMsg responseMsg1 = withdrOrderBusiHandler.withdrCardBind(requestMsg);
        if (!ConstEC.SUCCESS_10000.equals(responseMsg1.get(ConstEC.RETURNCODE))) {
            log.error("|提现绑卡确认|商户银行卡信息登记|失败|" + JSON.toJSONString(requestMsg));
            throw new BusiException(15011);
        }

        // 更新下状态
        if (userOper.getRealed().intValue()<RealedStatus.MIDDLE_REAL.getId()) {
            userOper.setRealed(RealedStatus.MIDDLE_REAL.getId()); //中实名
            userOperService.updateById(userOper);
        }

        //更新下绑卡表状态
        cardBind.setAgrNo(agrNo);
        cardBind.setStatus(CardBindStatus.BINDING.getId());
        cardBindService.updateById(cardBind);

        responseMsg.put(ConstEC.DATA, cardBind);
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        log.info("|提现绑卡确认|cardBindConfirmHandler|responseMsg:{}", JSON.toJSONString(responseMsg));
    }

    /**
     * 封装请求道高汇通的报文
     *
     * @param cardBind 提现绑卡报文
     * @return
     */
    private void packageGHTReqMsg(CardBind cardBind, RequestMsg requestMsg) {
        
        String certNo = cipherServiceImpl.decryptAES(cardBind.getCardNo());

        requestMsg.put("cardBindId", cardBind.getId());
        requestMsg.put("userId", cardBind.getUserId());
        requestMsg.put("cardNo", cardBind.getCardNo());
        requestMsg.put("certNo", certNo);
        requestMsg.put("cardType", cardBind.getCardType());
        requestMsg.put("mobile", cardBind.getMobile());
        requestMsg.put("bankCode", cardBind.getBankCode());
        requestMsg.put("bankCardName", cardBind.getBankCardName());
        requestMsg.put("bankCardNo", cardBind.getBankCardNo());
        requestMsg.put("bankCardType", cardBind.getBankCardType());
        requestMsg.put("bankNo", cardBind.getBankNo());
        requestMsg.put("bankProv", cardBind.getBankProv());
        requestMsg.put("bankCity", cardBind.getBankCity());
        requestMsg.put("tradeType", cardBind.getTradeType());
        requestMsg.put("sysCnl", cardBind.getSysCnl());
        requestMsg.put("clientIp", cardBind.getClientIp());
        requestMsg.put("userOperNo", cardBind.getUserOperNo());
        requestMsg.put("bankCardImgFront", cardBind.getBankCardImgFront());

    }


}
