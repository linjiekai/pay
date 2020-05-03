package com.mppay.gateway.controller.notify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mppay.core.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.RouteCode;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.alipay.AlipaySignature;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.gateway.handler.DepositProcHandler;
import com.mppay.service.entity.RouteConf;
import com.mppay.service.entity.TradeOrderAlipay;
import com.mppay.service.service.IRouteConfService;
import com.mppay.service.service.ITradeOrderAlipayService;

import lombok.extern.slf4j.Slf4j;

/**
 * 支付结果通知
 *
 */
@Controller
@RequestMapping("/notify/alipay")
@Slf4j
public class NotifyAlipayController {
	
	@Autowired
	private IRouteConfService routeConfService;
	
	@Autowired
	private ITradeOrderAlipayService tradeOrderAlipayService;
	
	@RequestMapping("/offline")
    public void offline(HttpServletRequest request, HttpServletResponse response)  {
		String responseStr = "fail";
		try {
			
			//获取支付宝通知信息
			Map<String, String> params = new HashMap<String, String>();
			Map<String,String[]> requestParams = request.getParameterMap();
			for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
			    String name = (String) iter.next();
			    String[] values = (String[]) requestParams.get(name);
			    String valueStr = "";
			    for (int i = 0; i < values.length; i++) {
			        valueStr = (i == values.length - 1) ? valueStr + values[i]
			                    : valueStr + values[i] + ",";
			  	}
			    //乱码解决，这段代码在出现乱码时使用。
				//valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
				params.put(name, valueStr);
			}

			LogUtil.NOTIFY.info("支付宝支付结果后台通知[{}]", params);
			
			TradeOrderAlipay tradeOrderAlipay = tradeOrderAlipayService.getOne(new QueryWrapper<TradeOrderAlipay>().eq("out_trade_no", params.get("out_trade_no")));

			if (null != tradeOrderAlipay) {
				
				RouteConf routeConf = routeConfService.getOne(new QueryWrapper<RouteConf>()
						.eq("app_id", params.get("app_id"))
						.eq("route_code", tradeOrderAlipay.getRouteCode())
						.eq("trade_type", tradeOrderAlipay.getTradeType())
						.eq("platform", tradeOrderAlipay.getPlatform())
						);
				
				verify(params, routeConf);
				
				String tradeStatus = params.get("trade_status");
				
				if (null != tradeStatus && tradeStatus.equals(ConstEC.TRADE_SUCCESS)) {
					
					Date payTime = DateTimeUtil.formatStringToDate(params.get("gmt_payment").toString(), "yyyy-MM-dd HH:mm:ss");
					
					List<Map<String, Object>> fund_bill_list  = (List<Map<String, Object>>) JSONObject.parseObject(params.get("fund_bill_list"), List.class);
					
					if (null == fund_bill_list) {
						fund_bill_list = new ArrayList<Map<String,Object>>();
					}
					
					Map<String, Object> data = new HashMap<String, Object>();
					data.put("routeCode", RouteCode.ALIPAY.getId());
					data.put("openId", params.get("buyer_id"));
					data.put("appId", params.get("app_id"));
					data.put("tradeType", tradeOrderAlipay.getTradeType());
					data.put("fundBank", fund_bill_list.get(0) == null ? "" : fund_bill_list.get(0).get("fundChannel"));
					data.put("payDate", DateTimeUtil.formatTimestamp2String(payTime, "yyyy-MM-dd"));
					data.put("payTime", DateTimeUtil.formatTimestamp2String(payTime, "HH:mm:ss"));
					data.put("outTradeNo", params.get("out_trade_no"));
					data.put("bankTradeNo", params.get("trade_no"));
					data.put("price", params.get("total_amount"));
					data.put("returnCode", "10000");
					data.put("returnMsg", "交易成功");
					
					DepositProcHandler handler = (DepositProcHandler) SpringContextHolder.getBean(RouteCode.ALIPAY.getId().toLowerCase() + ConstEC.DEPOSITPROCHANDLER);
					handler.proc(data);
					responseStr = "success";
				} else if (null != tradeStatus && tradeStatus.equals(ConstEC.TRADE_CLOSED)) {
//					tradeOrderAlipay.setOrderStatus(OrderStatus.FAIL.getId());
//					tradeOrderAlipayService.update(tradeOrderAlipay, new UpdateWrapper<TradeOrderAlipay>()
//							.eq("out_trade_no", tradeOrderAlipay.getOutTradeNo())
//							.eq("order_status", OrderStatus.WAIT_PAY.getId())
//							);
				}
			}
			
			response.getOutputStream().write(responseStr.getBytes());
		} catch (Exception e) {
			if (e instanceof BusiException) {
				BusiException busiException = (BusiException) e;
				LogUtil.NOTIFY.error("code[{}], return_msg[{}]", busiException.getCode(), busiException.getMsg());
			}
			log.error("支付结果异步通知处理失败[{}]", e);
			responseStr = "fail";
			try {
				response.getOutputStream().write(responseStr.getBytes());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} 
		
	}
	
	private void verify(Map<String, String> resultMap, RouteConf routeConf) throws Exception {
		
		String sign = resultMap.get("sign");
		boolean verify = AlipaySignature.rsaCheckV1(resultMap, routeConf.getPublicKey(), ConstEC.ENCODE_UTF8,"RSA2");
		
		if (!verify) {
			log.error("支付宝返回报文验签失败,待签名串[{}],支付宝返回签名串[{}]", resultMap,
					sign);
			throw new BusiException("11012", ApplicationYmlUtil.get("11012"));
		}
		
	}
}
