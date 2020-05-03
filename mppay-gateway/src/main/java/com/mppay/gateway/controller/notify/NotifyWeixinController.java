package com.mppay.gateway.controller.notify;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.MapEntryConverter;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.RouteCode;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.Sign;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.core.utils.LogUtil;
import com.mppay.gateway.handler.DepositProcHandler;
import com.mppay.service.entity.RouteConf;
import com.mppay.service.entity.TradeOrderWeixin;
import com.mppay.service.service.IRouteConfService;
import com.mppay.service.service.ITradeOrderWeixinService;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;

import lombok.extern.slf4j.Slf4j;

/**
 * 支付结果通知
 *
 */
@Controller
@RequestMapping("/notify/wxpay")
@Slf4j
public class NotifyWeixinController {
	
	@Autowired
	private IRouteConfService routeConfService;
	
	@Autowired
	private ITradeOrderWeixinService tradeOrderWeixinService;
	
	@RequestMapping("/offline")
    public void offline(HttpServletRequest request, HttpServletResponse response)  {
		InputStream inputStream = null;
		ByteArrayOutputStream byteArrayOutputStream = null;
		String requestXml = null;
		OutputStream outputStream = null;
		String return_code = "FAIL";
		String return_msg = "FAIL";
		XStream xStream = new XStream(new DomDriver("UTF-8", new XmlFriendlyNameCoder("-_", "_")));
		xStream.alias("xml", Map.class);
		xStream.registerConverter(new MapEntryConverter());
		try {
			
			inputStream = request.getInputStream();
			byteArrayOutputStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len;
			while ((len = inputStream.read(buffer)) > -1) {
				byteArrayOutputStream.write(buffer, 0, len);
			}
			requestXml = byteArrayOutputStream.toString("UTF-8");

			LogUtil.NOTIFY.info("收到微信支付结果通知报文[{}]", requestXml);
			
			Map<String, Object> xmlMap = (Map<String, Object>) xStream.fromXML(requestXml);
			String out_trade_no = (String)xmlMap.get("out_trade_no");
			TradeOrderWeixin tradeOrderWeixin = tradeOrderWeixinService.getOne(new QueryWrapper<TradeOrderWeixin>().eq("out_trade_no", out_trade_no));
			
			RouteConf routeConf = routeConfService.getOne(new QueryWrapper<RouteConf>()
					.eq("bank_merc_id", xmlMap.get("mch_id"))
					.eq("trade_type", xmlMap.get("trade_type"))
					.eq("route_code", tradeOrderWeixin.getRouteCode())
					.eq("platform", tradeOrderWeixin.getPlatform())
					);

			verify(xmlMap, routeConf);
			
			BigDecimal price = new BigDecimal(xmlMap.get("total_fee").toString());
			
			price = price.multiply(new BigDecimal(0.01)).setScale(2, BigDecimal.ROUND_HALF_UP);
			
			Date payTime = DateTimeUtil.formatStringToDate(xmlMap.get("time_end").toString(), "yyyyMMddHHmmss");

			Map<String, Object> data = new HashMap<String, Object>();
			data.put("routeCode", RouteCode.WEIXIN.getId());
			data.put("openId", xmlMap.get("openid"));
			data.put("appId", xmlMap.get("appid"));
			data.put("tradeType", xmlMap.get("trade_type"));
			data.put("fundBank", xmlMap.get("bank_type"));
			data.put("payDate", DateTimeUtil.formatTimestamp2String(payTime, "yyyy-MM-dd"));
			data.put("payTime", DateTimeUtil.formatTimestamp2String(payTime, "HH:mm:ss"));
			data.put("outTradeNo", out_trade_no);
			data.put("bankTradeNo", xmlMap.get("transaction_id"));
			data.put("price", price);
			data.put("returnCode", "10000");
			data.put("returnMsg", "交易成功");
			
			DepositProcHandler handler = (DepositProcHandler) SpringContextHolder.getBean(RouteCode.WEIXIN.getId().toLowerCase() + ConstEC.DEPOSITPROCHANDLER);
			handler.proc(data);
			
			return_code = "SUCCESS";
			return_msg = "OK";
		} catch (Exception e) {
			if (e instanceof BusiException) {
				BusiException busiException = (BusiException) e;
				return_msg = busiException.getMsg();
				return_code = "FAIL";
				// log.error("code[" + busiException.getCode() + "], return_msg[" + busiException.getMsg() + "]");
				LogUtil.NOTIFY.error("code[{}], return_msg[{}]", busiException.getCode(), busiException.getMsg());
			}
			log.error("支付结果异步通知处理失败[{}]", e);
		} finally {
			if (null != byteArrayOutputStream) {
				try {
					byteArrayOutputStream.flush();
					byteArrayOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (null != inputStream) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (null != outputStream) {
				try {
					outputStream.flush();
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		Map<String, Object> respMap = new HashMap<String, Object>();
		respMap.put("return_code", return_code);
		respMap.put("return_msg", return_msg);
		String responseXml = xStream.toXML(respMap);
		
		try {
			response.getOutputStream().write(responseXml.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void verify(Map<String, Object> resultMap, RouteConf routeConf) throws Exception {
		
		String plain = Sign.getPlain(resultMap);
		plain += "&key=" + routeConf.getPrivateKey();
		
		String sign = resultMap.get("sign").toString();
		
		if (!Sign.verifyToHex(plain, sign)) {
			LogUtil.NOTIFY.error("微信返回报文验签失败,待签名串[{}],微信返回签名串[{}]", plain,
					sign);
			throw new BusiException("11012", ApplicationYmlUtil.get("11012"));
		}
		
		if (null == resultMap.get("return_code") || ConstEC.FAIL.equals(resultMap.get("return_code"))) {
			LogUtil.NOTIFY.error("微信请求处理失败,错误码是[{}],错误信息是[{}]",
					resultMap.get("return_code"), resultMap.get("return_msg"));
			throw new BusiException(resultMap.get("return_code").toString(), resultMap.get("return_msg").toString());
		}
		
		if (null == resultMap.get("result_code") || ConstEC.FAIL.equals(resultMap.get("result_code"))) {
			LogUtil.NOTIFY.error("微信请求处理失败,错误码是[{}],错误信息是[{}]",
					resultMap.get("err_code"), resultMap.get("err_code_des"));
			throw new BusiException(resultMap.get("err_code").toString(), resultMap.get("err_code_des").toString());
		}
		
	}
}
