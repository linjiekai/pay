package com.mppay.gateway.controller.notify;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.RouteCode;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.Sign;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.core.utils.LogUtil;
import com.mppay.core.utils.RedisUtil;
import com.mppay.gateway.handler.DepositProcHandler;
import com.mppay.service.entity.RouteConf;
import com.mppay.service.entity.TradeOrderGaohuitong;
import com.mppay.service.service.IRouteConfService;
import com.mppay.service.service.ITradeOrderGaohuitongService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 支付结果通知
 */
@Controller
@RequestMapping("/notify/gaohuitong")
@Slf4j
public class NotifyGaohuitongController {

    @Autowired
    private ITradeOrderGaohuitongService tradeOrderGaohuitongService;

    @Autowired
    private IRouteConfService routeConfService;


    @RequestMapping("/offline")
    public void offline(HttpServletRequest request, HttpServletResponse response) {
        String responseStr = "fail";
        try {
            Map<String, String[]> requestParams = request.getParameterMap();
            LogUtil.NOTIFY.info("支付结果后台通知::高汇通::request：{}", JSON.toJSONString(requestParams));
            //获取支付宝通知信息
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
            String s = JSON.toJSONString(params);
            LogUtil.NOTIFY.info("支付结果后台通知::高汇通::params:{}", s);

            TradeOrderGaohuitong tradeOrderGaohuitong = tradeOrderGaohuitongService.getOne(new QueryWrapper<TradeOrderGaohuitong>().eq("out_trade_no", params.get("order_no")));
            RouteConf routeConf = routeConfService.getOne(
                    new QueryWrapper<RouteConf>()
                            .eq("bank_merc_id", tradeOrderGaohuitong.getOrgNo())
                            .eq("app_id", tradeOrderGaohuitong.getTerminalNo())
                            .last(" limit 1")
            );
            LogUtil.NOTIFY.info("支付结果后台通知::高汇通::银行bankCode:{},银行订单号out_trade_no：{}", params.get("bank_code"), params.get("order_no"));
            if (null != tradeOrderGaohuitong) {
                String outTradeNo = tradeOrderGaohuitong.getOutTradeNo();
                if (RedisUtil.hasKey(ConstEC.REDIS_KEY_ORDER_NOTIFY_GAOHUITONGHK + outTradeNo)) {
                    LogUtil.NOTIFY.info("支付结果后台通知::高汇通::已处理，outTradeNo：{}", outTradeNo);
                    return;
                }
                verify(params, routeConf);
                String tradeStatus = (String) params.get("pay_result");
                if (null != tradeStatus && tradeStatus.equals("1")) {
                    Date payTime = DateTimeUtil.formatStringToDate(params.get("pay_time").toString(), "yyyyMMddHHmmss");
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put("routeCode", RouteCode.GAOHUITONG.getId());
                    data.put("payDate", DateTimeUtil.formatTimestamp2String(payTime, "yyyy-MM-dd"));
                    data.put("payTime", DateTimeUtil.formatTimestamp2String(payTime, "HH:mm:ss"));
                    data.put("outTradeNo", params.get("order_no"));
                    data.put("bankTradeNo", params.get("pay_no"));
                    data.put("openId", tradeOrderGaohuitong.getOpenId());
                    data.put("price", params.get("amount"));
                    data.put("fundBank", params.get("bank_code"));
                    data.put("returnCode", "10000");
                    data.put("returnMsg", "交易成功");

                    DepositProcHandler handler = (DepositProcHandler) SpringContextHolder.getBean(RouteCode.GAOHUITONG.getId().toLowerCase() + ConstEC.DEPOSITPROCHANDLER);
                    handler.proc(data);
                    responseStr = "success";
                    LogUtil.NOTIFY.info("支付结果后台通知::高汇通::处理成功，outTradeNo：{}", outTradeNo);
                    RedisUtil.set(ConstEC.REDIS_KEY_ORDER_NOTIFY_GAOHUITONG + outTradeNo, outTradeNo, ConstEC.CACHE_EXP_TIME);
                }

            }
        } catch (Exception e) {
            if (e instanceof BusiException) {
                BusiException busiException = (BusiException) e;
                LogUtil.NOTIFY.info("code[{}], return_msg[{}]", busiException.getCode(), busiException.getMsg());
            }
            log.error("支付结果后台通知::高汇通::处理失败[{}]", e);
            responseStr = "fail";
        } finally {
            try {
                response.getOutputStream().write(responseStr.getBytes());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void verify(Map<String, Object> params, RouteConf routeConf) throws Exception {
        String sign = (String) params.get("sign");
        String plain = Sign.getPlain(params);
        plain += "&key=" + routeConf.getPublicKeyPath();
        boolean verify = Sign.verifySHA256ToHex(plain, sign);

        if (!verify) {
            LogUtil.NOTIFY.error("支付结果后台通知::高汇通::报文验签失败,待签名串[{}],高汇通返回签名串[{}]", plain,
                    sign);
            throw new BusiException("11012", ApplicationYmlUtil.get("11012"));
        }

    }
}
