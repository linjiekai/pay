package com.mppay.gateway.controller.notify;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.joinpay.JoinpayConstants;
import com.mppay.core.exception.BusiException;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.core.utils.DateUtil;
import com.mppay.core.utils.LogUtil;
import com.mppay.core.utils.RedisUtil;
import com.mppay.gateway.handler.DepositProcHandler;
import com.mppay.service.entity.TradeOrderJoinpay;
import com.mppay.service.entity.TradeRefundJoinpay;
import com.mppay.service.service.IRouteConfService;
import com.mppay.service.service.ITradeOrderJoinpayService;
import com.mppay.service.service.ITradeRefundJoinpayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 支付结果通知
 */
@Controller
@RequestMapping("/notify/joinpay")
@Slf4j
public class NotifyJoinpayController {

    @Autowired
    private ITradeOrderJoinpayService iTradeOrderJoinpayService;

    //支付结果通知
    @RequestMapping("/offline")
    public void payOffline(HttpServletRequest request, HttpServletResponse response) {
        String responseStr = "fail";
        String r2_orderNo = "";
        try {
            Map<String, String[]> requestParams = request.getParameterMap();
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
            LogUtil.NOTIFY.info("支付结果后台通知::joinpay::params:{}", s);
            r2_orderNo = (String) params.get("r2_OrderNo");
            String rc_bankCode = (String) params.get("rc_BankCode");

            TradeOrderJoinpay joinpayServiceOne = iTradeOrderJoinpayService.getOne(new QueryWrapper<TradeOrderJoinpay>().eq("out_trade_no", r2_orderNo));

            LogUtil.NOTIFY.info("支付结果后台通知::joinpay::bankCode:{},out_trade_no：{}", params.get("rc_BankCode"), r2_orderNo);
            if (null != joinpayServiceOne) {
                String outTradeNo = joinpayServiceOne.getOutTradeNo();
                if (RedisUtil.hasKey(JoinpayConstants.REDIS_KEY_ORDER_NOTIFY_JOINPAY + outTradeNo)) {
                    LogUtil.NOTIFY.info("支付结果后台通知::joinpay::done，outTradeNo：{}", outTradeNo);
                    return;
                }
                String ra_status = (String) params.get("r6_Status");
                String ra_PayTime = (String) params.get("ra_PayTime");
                String decode = URLDecoder.decode(ra_PayTime, "utf-8");
                String r7_TrxNo = (String) params.get("r7_TrxNo");
                String r3_Amount = (String) params.get("r3_Amount");
                String rd_OpenId = (String) params.get("rd_OpenId");
                if (StrUtil.isNotBlank(ra_status) && JoinpayConstants.RESPCODE_100.equalsIgnoreCase(ra_status)) {
                    Map<String, Object> data = new HashMap<String, Object>();
                    String routeCode = joinpayServiceOne.getRouteCode();
                    data.put("routeCode", routeCode);
                    data.put("payDate", DateTimeUtil.formatDateStringToString(decode, DateUtil.DATE_TIME_PATTERN, DateUtil.DATE_PATTERN));
                    data.put("payTime", DateTimeUtil.formatDateStringToString(decode, DateUtil.DATE_TIME_PATTERN, DateUtil.TIME_PATTERN));
                    data.put("outTradeNo", r2_orderNo);
                    data.put("bankTradeNo", r7_TrxNo);
                    data.put("openId", StrUtil.isNotBlank(rd_OpenId) ? rd_OpenId : joinpayServiceOne.getOpenId());
                    data.put("price", r3_Amount);
                    data.put("fundBank", rc_bankCode); //资金银行
                    data.put("returnCode", "10000");
                    data.put("returnMsg", "交易成功");

                    DepositProcHandler handler = (DepositProcHandler) SpringContextHolder.getBean(routeCode.toLowerCase() + ConstEC.DEPOSITPROCHANDLER);
                    handler.proc(data);
                    responseStr = "success";
                    LogUtil.NOTIFY.info("支付结果后台通知::joinpay::success，outTradeNo：{}", outTradeNo);
                    RedisUtil.set(JoinpayConstants.REDIS_KEY_ORDER_NOTIFY_JOINPAY + outTradeNo, outTradeNo, ConstEC.CACHE_EXP_TIME);
                }

            }
        } catch (Exception e) {
            if (e instanceof BusiException) {
                BusiException busiException = (BusiException) e;
                LogUtil.NOTIFY.info("支付结果后台通知:joinpay:fail,outTradeNo：{}，code：{}, return_msg：{}", r2_orderNo, busiException.getCode(), busiException.getMsg());
            }
            log.error("支付结果后台通知::joinpay:outTradeNo：{},error:{}", r2_orderNo, e);
            responseStr = "fail";
        } finally {
            try {
                response.getOutputStream().write(responseStr.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }


    //退款结果通知
    @RequestMapping("/refund/offline")
    public void refundOffline(HttpServletRequest request, HttpServletResponse response) {
        String responseStr = "success";
        String r2_orderNo = "";
        try {
            Map<String, String[]> requestParams = request.getParameterMap();
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
            r2_orderNo = (String) params.get("r2_OrderNo");
            LogUtil.NOTIFY.info("退款结果通知::joinpay::params:{}", s);
            LogUtil.NOTIFY.info("退款结果通知::joinpay::默认返回success,out_refund_no:{}  ", r2_orderNo);
        } catch (Exception e) {
            if (e instanceof BusiException) {
                BusiException busiException = (BusiException) e;
                LogUtil.NOTIFY.info("退款结果通知:joinpay:fail,out_refund_no：{}，code：{}, return_msg：{}", r2_orderNo, busiException.getCode(), busiException.getMsg());
            }
            log.error("退款结果通知::joinpay:out_refund_no：{},error:{}", r2_orderNo, e);
            responseStr = "fail";
        } finally {
            try {
                response.getOutputStream().write(responseStr.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

}
