package com.mppay.gateway.controller.notify;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.*;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.Sign;
import com.mppay.core.sign.gaohuitong.GaohuitongMessgeUtil;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.core.utils.LogUtil;
import com.mppay.core.utils.RedisUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.platform.gaohuitong.GHThkNotifyDTO;
import com.mppay.gateway.handler.BankBusiHandler;
import com.mppay.gateway.handler.DepositProcHandler;
import com.mppay.gateway.handler.UnifiedHandler;
import com.mppay.gateway.handler.impl.GaohuitonghkBankBusiHandlerImpl;
import com.mppay.service.entity.MercOrder;
import com.mppay.service.entity.RouteConf;
import com.mppay.service.entity.TradeOrderGaohuitong;
import com.mppay.service.entity.TradeRefund;
import com.mppay.service.service.*;
import com.mppay.service.service.common.ICipherService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.mppay.core.utils.StringUtil.getRandom;

/**
 * 支付结果通知
 */
@RestController
@RequestMapping("/notify/gaohuitonghk")
@Slf4j
public class NotifyGaohuitonghkController {

    @Autowired
    private ITradeOrderGaohuitongService tradeOrderGaohuitongService;

    @Autowired
    private IRouteConfService routeConfService;
    @Autowired
    private ICipherService iCipherService;

    @RequestMapping("/offline")
    public String offline(HttpServletRequest request) throws Exception {
        String responseStr = "fail";
        String jsonBody = "";
        String orderNo = "";
        String tradeStatus = "";
        String payTime = "";
        String payNo = "";
        String amount = "";
        String bankCode = "";
        String openId = "";
        Map<String, String[]> requestParams = request.getParameterMap();
        if (requestParams.size() > 0) {
            Map<String, Object> params = new HashMap<String, Object>();
            for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
                String name = (String) iter.next();
                String[] values = (String[]) requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i]
                            : valueStr + values[i] + ",";
                }
                params.put(name, valueStr);
            }
            LogUtil.NOTIFY.info("支付结果后台通知::高汇通HK::银行卡request：{}", JSON.toJSONString(params));
            orderNo = (String) params.get("order_no");
            tradeStatus = (String) params.get("pay_result");
            payTime = (String) params.get("pay_time");
            payNo = (String) params.get("pay_no");
            amount = (String) params.get("amount");
            bankCode = (String) params.get("bank_code");
            openId = (String) params.get("user_bank_card_no"); //类似这种形式621723*********0769
            LogUtil.NOTIFY.info("支付结果后台通知::高汇通HK::bankCode:{},银行订单号outTradeNo：{}", bankCode, orderNo);
        } else {
            jsonBody = getStreamByReq(request);
            LogUtil.NOTIFY.info("支付结果后台通知::高汇通HK::密文request：{}", jsonBody);
            Map map = JSON.parseObject(jsonBody, Map.class);
            String agencyId = (String) map.get("agencyId");
            RouteConf routeConf = routeConfService.getOne(
                    new QueryWrapper<RouteConf>()
                            .eq("bank_merc_id", agencyId)
                            .eq("route_code", RouteCode.GAOHUITONGHK.getId())
                            .last(" limit 1")
            );
            String keyStr = getRandom(16);
            PublicKey publicKey = GaohuitongMessgeUtil.getPublicKey(routeConf.getPublicKeyPath());
            PrivateKey privateKey = GaohuitongMessgeUtil.getPrivateKey(routeConf.getKeyPath());
            String resp = GaohuitongMessgeUtil.responseHandle(map, keyStr, publicKey, privateKey);
            LogUtil.NOTIFY.info("支付结果后台通知::高汇通HK::明文request:{}", resp);
            GHThkNotifyDTO ghThkNotifyDTO = JSON.parseObject(resp, GHThkNotifyDTO.class);
            if (ghThkNotifyDTO == null) {
                return responseStr;
            }
            orderNo = ghThkNotifyDTO.getOrder_no();
            tradeStatus = ghThkNotifyDTO.getPay_result();
            payTime = ghThkNotifyDTO.getPay_time();
            payNo = ghThkNotifyDTO.getPay_no();
            amount = ghThkNotifyDTO.getAmount();
            bankCode = ghThkNotifyDTO.getBank_code();
            openId = ghThkNotifyDTO.getUser_bank_card_no(); //微信的openid
            LogUtil.NOTIFY.info("支付结果后台通知::高汇通HK::bankCode:{},银行订单号outTradeNo：{}", bankCode, orderNo);
        }

        try {
            if (StringUtils.isNotBlank(orderNo)) {
                if (RedisUtil.hasKey(ConstEC.REDIS_KEY_ORDER_NOTIFY_GAOHUITONGHK + orderNo)) {
                    LogUtil.NOTIFY.info("支付结果后台通知::高汇通HK::已经处理过，outTradeNo：{}", orderNo);
                    return "success";
                }

                TradeOrderGaohuitong tradeOrderGaohuitong = tradeOrderGaohuitongService.getOne(new QueryWrapper<TradeOrderGaohuitong>().eq("out_trade_no", orderNo));
                if (tradeOrderGaohuitong != null) {
                    LogUtil.NOTIFY.info("支付结果后台通知::高汇通HK::支付结果：{}，outTradeNo：{}", tradeStatus, orderNo);
                    if (null != tradeStatus && tradeStatus.equals("1")) {
                        Date date = DateTimeUtil.formatStringToDate(payTime, "yyyyMMddHHmmss");
                        Map<String, Object> data = new HashMap<String, Object>();
                        data.put("routeCode", RouteCode.GAOHUITONGHK.getId());
                        data.put("payDate", DateTimeUtil.formatTimestamp2String(date, "yyyy-MM-dd"));
                        data.put("payTime", DateTimeUtil.formatTimestamp2String(date, "HH:mm:ss"));
                        data.put("outTradeNo", orderNo);
                        data.put("bankTradeNo", payNo);
                        if(BankCode.WEIXIN.getId().equalsIgnoreCase(bankCode)){
                            data.put("openId", StringUtils.isNotBlank(tradeOrderGaohuitong.getOpenId()) ? tradeOrderGaohuitong.getOpenId() : iCipherService.encryptAES(openId));
                        }
                        data.put("price", amount);
                        data.put("fundBank", bankCode);
                        data.put("returnCode", "10000");
                        data.put("returnMsg", "交易成功");
                        DepositProcHandler handler = (DepositProcHandler) SpringContextHolder.getBean(RouteCode.GAOHUITONGHK.getId().toLowerCase() + ConstEC.DEPOSITPROCHANDLER);
                        handler.proc(data);

                        responseStr = "success";
                        LogUtil.NOTIFY.info("支付结果后台通知::高汇通HK::处理成功，outTradeNo：{}", orderNo);
                        RedisUtil.set(ConstEC.REDIS_KEY_ORDER_NOTIFY_GAOHUITONGHK + orderNo, orderNo, ConstEC.CACHE_EXP_TIME);
                    }
                }
            }

        } catch (Exception e) {
            LogUtil.NOTIFY.info("支付结果后台通知::高汇通HK::处理失败，outTradeNo：{}", orderNo);
            log.error("支付结果后台通知::高汇通HK::处理失败，outTradeNo：{}，异常：{}", orderNo, e);
            responseStr = "fail";
        }
        return responseStr;
    }


    public String getStreamByReq(HttpServletRequest request) throws Exception {
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        StringBuffer buffer = new StringBuffer();
        try {
            inputStream = request.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            bufferedReader = new BufferedReader(inputStreamReader);
            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            return buffer.toString();
        } catch (Exception e) {
            return "";
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    log.error("支付结果后台通知::高汇通HK::处理失败：{}", e);
                }
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    log.error("支付结果后台通知::高汇通HK::处理失败：{}", e);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("支付结果后台通知::高汇通HK::处理失败：{}", e);
                }
                inputStream = null;
            }
        }
    }

}
