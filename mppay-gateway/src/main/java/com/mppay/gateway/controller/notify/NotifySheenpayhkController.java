package com.mppay.gateway.controller.notify;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.RouteCode;
import com.mppay.core.sign.Sign;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.core.utils.LogUtil;
import com.mppay.gateway.dto.platform.sheen.SheenNotifyDTO;
import com.mppay.gateway.handler.DepositProcHandler;
import com.mppay.service.entity.RouteConf;
import com.mppay.service.entity.TradeOrderSheenPay;
import com.mppay.service.service.IRouteConfService;
import com.mppay.service.service.ITradeOrderSheenPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 信來-支付结果通知
 */
@RestController
@RequestMapping("/notify/sheenpayhk")
@Slf4j
public class NotifySheenpayhkController {

    @Autowired
    private IRouteConfService iRouteConfService;
    @Autowired
    private ITradeOrderSheenPayService iTradeOrderSheenPayService;

    @PostMapping("/offline")
    public String offline(@RequestBody JSONObject jsonObject) throws Exception {
        LogUtil.NOTIFY.info("支付结果后台通知::sheenpayHK::请求报文：{}", jsonObject);
        SheenNotifyDTO sheenNotifyDTO = JSONObject.toJavaObject(jsonObject, SheenNotifyDTO.class);
        Map<String, String> retMap = new HashMap<>();
        // 通知
        String outTradeNo = sheenNotifyDTO.getPartnerOrderId();
        try {
            TradeOrderSheenPay tradeOrderSheenPay = iTradeOrderSheenPayService.getOne(new QueryWrapper<TradeOrderSheenPay>().eq("out_trade_no", outTradeNo));
            if (tradeOrderSheenPay != null) {
                // 签名校验
                RouteConf routeConf = iRouteConfService.getOne(new QueryWrapper<RouteConf>()
                        .eq("bank_merc_id", tradeOrderSheenPay.getBankMercId())
                        .eq("route_code", tradeOrderSheenPay.getRouteCode())
                        .eq("trade_type", tradeOrderSheenPay.getTradeType())
                        .eq("platform", tradeOrderSheenPay.getPlatform())
                );
                String partnerCode = routeConf.getBankMercId();
                String credentialCode = routeConf.getPublicKey();
                Boolean signFlag = checkSign(sheenNotifyDTO, partnerCode, credentialCode);
                if (!signFlag) {
                    LogUtil.NOTIFY.info("支付结果后台通知::sheenpayHK::签名异常，签名对象：{}", sheenNotifyDTO);
                    log.error("支付结果后台通知::sheenpayHK::签名异常，签名对象：{}", sheenNotifyDTO);
                    retMap.put("return_code", "FAIL");
                    return JSONObject.toJSONString(retMap);
                }

                LogUtil.NOTIFY.info("支付结果后台通知::sheenpayHK：通知平台,outTradeNo：{}", outTradeNo);
                int realFee = sheenNotifyDTO.getRealFee();
                BigDecimal price = new BigDecimal(realFee);
                price = price.multiply(new BigDecimal(0.01)).setScale(2, BigDecimal.ROUND_HALF_UP);
                Date payTime = sheenNotifyDTO.getPayTime();

                Map<String, Object> data = new HashMap<String, Object>();
                data.put("routeCode", RouteCode.SHEENPAYHK.getId());
                data.put("openId", tradeOrderSheenPay.getOpenId());
                data.put("appId", tradeOrderSheenPay.getAppId());
                data.put("tradeType", tradeOrderSheenPay.getTradeType());
                // data.put("fundBank", "");
                data.put("payDate", DateTimeUtil.formatTimestamp2String(payTime, "yyyy-MM-dd"));
                data.put("payTime", DateTimeUtil.formatTimestamp2String(payTime, "HH:mm:ss"));
                data.put("outTradeNo", sheenNotifyDTO.getPartnerOrderId());
                data.put("bankTradeNo", sheenNotifyDTO.getOrderId());
                data.put("price", price);
                data.put("returnCode", "10000");
                data.put("returnMsg", "交易成功");

                DepositProcHandler handler = (DepositProcHandler) SpringContextHolder.getBean(RouteCode.SHEENPAYHK.getId().toLowerCase() + ConstEC.DEPOSITPROCHANDLER);
                handler.proc(data);
            }
        } catch (Exception e) {
            LogUtil.NOTIFY.info("支付结果后台通知::sheenpayHK::处理失败，outTradeNo：{}", outTradeNo);
            log.error("支付结果后台通知::sheenpayHK::处理失败，outTradeNo：{}，异常：{}", outTradeNo, e);
            retMap.put("return_code", "FAIL");
            return JSONObject.toJSONString(retMap);
        }
        LogUtil.NOTIFY.info("支付结果后台通知::sheenpayHK::完成outTradeNo：{}", outTradeNo);
        retMap.put("return_code", "SUCCESS");
        return JSONObject.toJSONString(retMap);
    }

    /**
     * 校验签名
     *
     * @return
     * @throws Exception
     */
    private Boolean checkSign(SheenNotifyDTO sheenNotifyDTO, String partnerCode, String credentialCode) throws Exception {
        long time = sheenNotifyDTO.getTime();
        String nonceStr = sheenNotifyDTO.getNonceStr();
        String validStr = partnerCode + "&" + time + "&" + nonceStr + "&" + credentialCode;
        String signOrg = Sign.signSHA256ToHex(validStr).toLowerCase();
        return signOrg.equals(sheenNotifyDTO.getSign());
    }
}
