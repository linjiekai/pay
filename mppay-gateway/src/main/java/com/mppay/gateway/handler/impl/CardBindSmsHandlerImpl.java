package com.mppay.gateway.handler.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.client.dto.ResponseDTO;
import com.mppay.client.dto.common.SmsDTO;
import com.mppay.client.feignClient.CommonClient;
import com.mppay.core.config.SMSParams;
import com.mppay.core.constant.CardBindStatus;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.SmsOrderType;
import com.mppay.core.exception.BusiException;
import com.mppay.core.utils.CharacterUtil;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.core.utils.RedisUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.service.entity.CardBind;
import com.mppay.service.entity.SmsOrder;
import com.mppay.service.service.ICardBindService;
import com.mppay.service.service.ISmsOrderService;
import com.mppay.service.service.common.ICipherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 提现绑卡-发送短信
 */
@Slf4j
@Service("cardBindSmsHandler")
public class CardBindSmsHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler {

    @Autowired
    private ICardBindService cardBindService;
    @Autowired
    private SMSParams smsParams;
    @Autowired
    private ISmsOrderService smsOrderService;
    @Autowired
    private ICipherService cipherServiceImpl;
    @Autowired
    private CommonClient commonClient;

    /**
     * 提现绑卡短信
     *
     * @param requestMsg
     * @param responseMsg
     * @throws Exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        log.info("|提现绑卡短信|cardBindSmsHandler|requestMsg:{}", requestMsg);
        String agrNoCipher = (String) requestMsg.get("agrNo");
        String agrNo = cipherServiceImpl.decryptAES(agrNoCipher);

        String smsOrderNo = (String) requestMsg.get("smsOrderNo");
        // 60s重发校验
        String smsRetryKey = ConstEC.REDIS_KEY_PREFIX_CARD_BIND_SMS_RETRY + smsOrderNo;
        boolean smsRetryFlag = RedisUtil.hasKey(smsRetryKey);
        if (smsRetryFlag) {
            long expireTime = RedisUtil.getExpire(smsRetryKey);
            log.error("|提现绑卡短信|短信重发校验|短信60秒内不可重发");
            responseMsg.put(ConstEC.RETURNCODE, "31022");
            responseMsg.put(ConstEC.RETURNMSG, expireTime + "秒后请再获取短信验证码");
            return;
        }

        // 获取绑卡订单
        CardBind cardBind = cardBindService.getOne(new QueryWrapper<CardBind>()
                .eq("agr_no", agrNo)
                .eq("status", CardBindStatus.CHECK));
        Optional.ofNullable(cardBind).orElseThrow(() -> new BusiException(15006));

        // 发送短信
        String smsCode = CharacterUtil.getRandomNum(6);
        JSONObject codeJson = new JSONObject();
        codeJson.put("code", smsCode);
        String mobileCode = "86";

        String mobile = cipherServiceImpl.decryptAES(cardBind.getMobile());

        String platForm = (String) requestMsg.get("platform");
        SmsDTO smsDTO = SmsDTO.builder().mobile(mobile).mobileCode(mobileCode).json(codeJson.toJSONString()).platForm(platForm).templateId(smsParams.getSmsTemplateBind()).build();

        ResponseDTO responseDTO = commonClient.sendSms(smsDTO);
        if (!ConstEC.SUCCESS_10000.equals(responseDTO.getCode())){
            log.error("|提现绑卡短信|获取绑卡信息|发送短信失败|");
            throw new BusiException("31025");
        }

        // 更新短信订单表
        SmsOrder smsOrder = smsOrderService.getOne(new QueryWrapper<SmsOrder>()
                .eq("agr_no", agrNo)
                .eq("sms_order_no", smsOrderNo));
        //计算失效时间 10分钟
        String expTime = DateTimeUtil.formatTimestamp2String(DateTimeUtil.getPeriodTime(10, "00"), "yyyyMMddHHmmss");
        smsOrder.setExpTime(expTime);
        smsOrder.setSmsOrderType(SmsOrderType.WITHDRAW_CARD_BIND.getId());
        smsOrder.setSmsCode(smsCode);
        smsOrderService.updateById(smsOrder);

        RedisUtil.set(smsRetryKey, smsOrderNo, 60);
        Map<String, Object> data = new HashMap<>();
        data.put("agrNo", agrNoCipher);
        data.put("smsOrderNo", smsOrder.getSmsOrderNo());
        responseMsg.put("data", data);
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        log.info("|提现绑卡短信|cardBindSmsHandler|responseMsg:{}", responseMsg);
    }
}
