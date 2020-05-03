package com.mppay.gateway.handler.impl;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.mppay.core.constant.*;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.Sign;
import com.mppay.core.sign.alipay.AlipaySignature;
import com.mppay.core.sign.alipay.WebUtils;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.core.utils.HttpClientUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BankBusiHandler;
import com.mppay.gateway.handler.DepositProcHandler;
import com.mppay.service.entity.Route;
import com.mppay.service.entity.RouteConf;
import com.mppay.service.entity.TradeOrderAlipay;
import com.mppay.service.entity.TradeRefundAlipay;
import com.mppay.service.service.IRouteConfService;
import com.mppay.service.service.IRouteService;
import com.mppay.service.service.ISeqIncrService;
import com.mppay.service.service.ITradeOrderAlipayService;
import com.mppay.service.service.ITradeRefundAlipayService;

import lombok.extern.slf4j.Slf4j;

@Service("alipayBankBusiHandler")
@Slf4j
public class AlipayBankBusiHandlerImpl implements BankBusiHandler{

	@Autowired
	private ITradeOrderAlipayService tradeOrderAlipayService;
	
	@Autowired
	private ITradeRefundAlipayService tradeRefundAlipayService;
	
	@Autowired
	private ISeqIncrService seqIncrService;
	
	@Autowired
	private IRouteConfService routeConfService;
	
	@Autowired
	private IRouteService routeService;
	@Value("${alipay.gatewayurl}")
	private String gatewayurl;

	@Override
	public void unifiedOrder(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
		String periodUnit = (String)requestMsg.get("periodUnit");
		Integer period = (Integer)requestMsg.get("period");
		String timeout_express = period+ PeriodTimeEnum.parse(periodUnit).getFlag().toLowerCase();
		Route route = routeService.getOne(new QueryWrapper<Route>().eq("route_code", requestMsg.get("routeCode")));
		
		TradeOrderAlipay tradeOrderAlipay = new TradeOrderAlipay();
		String seq = seqIncrService.nextVal(SeqIncrType.OUT_TRADE_NO_ALIPAY.getId(), 10, Align.LEFT);
		String outTradeNo = DateTimeUtil.date8() + seq;
		
		requestMsg.put("notifyUrl", route.getNotifyUrl());
		requestMsg.put("outTradeNo", outTradeNo);
		
		BeanUtils.populate(tradeOrderAlipay, requestMsg.getMap());
		
		tradeOrderAlipay.setTradeDate(DateTimeUtil.date10());
		tradeOrderAlipay.setTradeTime(DateTimeUtil.time8());
		
		tradeOrderAlipay.setOrderStatus(OrderStatus.ADVANCE.getId());
		//创建订单流水
		tradeOrderAlipayService.save(tradeOrderAlipay);
		
		String tradeType = (String) requestMsg.get("tradeType");
		RouteConf routeConf = routeConfService.getOne(new QueryWrapper<RouteConf>()
				.eq("merc_id", requestMsg.get("mercId"))
				.eq("route_code", requestMsg.get("routeCode"))
				.eq("trade_type", tradeType)
				.eq("platform", requestMsg.get("platform"))
				);
		
		//组装银行统一下单请求报文
		TreeMap<String, Object> contentMap = new TreeMap<String, Object>();
		
		contentMap.put("timeout_express", timeout_express);
		contentMap.put("total_amount", tradeOrderAlipay.getPrice() + "");
		contentMap.put("out_trade_no", outTradeNo);
		contentMap.put("subject", GoodsPriceType.parasName(requestMsg.get("platform").toString()));
		
		Map<String, Object> bankMap = new HashMap<String, Object>();

		if (TradeType.APP.getId().equals(tradeType)) {
			//组装银行支付请求报文
			bankMap.put("method", "alipay.trade.app.pay");
			bankMap.put("product_code", "QUICK_MSECURITY_PAY");
			contentMap.put("body", GoodsPriceType.parasName(requestMsg.get("platform").toString()));
		} else if (TradeType.MWEB.getId().equals(tradeType)) {
			bankMap.put("method", "alipay.trade.wap.pay");
			bankMap.put("product_code", "QUICK_WAP_WAY");
		}
		
		bankMap.put("app_id", routeConf.getAppId());
		bankMap.put("format", "JSON");
		bankMap.put("charset", ConstEC.ENCODE_UTF8);
		bankMap.put("sign_type", "RSA2");
		bankMap.put("timestamp", DateTimeUtil.formatTimestamp2String(new Date(), "yyy-MM-dd HH:mm:ss"));
		bankMap.put("version", "1.0");
		bankMap.put("return_url", requestMsg.get("callbackUrl"));
		bankMap.put("notify_url", requestMsg.get("notifyUrl"));
		bankMap.put("biz_content", JSONObject.toJSONString(contentMap));
		
		String plain = Sign.getPlain(bankMap, true);
		String sign = AlipaySignature.rsa256Sign(plain, routeConf.getPrivateKey(), ConstEC.ENCODE_UTF8);
		bankMap.put("sign", sign);
		
		
		unifiedOrderResponseMsg(requestMsg, routeConf, bankMap, contentMap, responseMsg);
		
		tradeOrderAlipay.setOrderStatus(OrderStatus.WAIT_PAY.getId());
		tradeOrderAlipayService.updateById(tradeOrderAlipay);
		
		responseMsg.put("appId", routeConf.getAppId());
		responseMsg.put("outTradeNo", outTradeNo);
		responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
		responseMsg.put(ConstEC.RETURNMSG, "成功");
	}

	@Override
	public ResponseMsg queryOrder(RequestMsg requestMsg) throws Exception {
		ResponseMsg responseMsg = new ResponseMsg();
		
		TradeOrderAlipay tradeOrderAlipay = tradeOrderAlipayService.getOne(new QueryWrapper<TradeOrderAlipay>().eq("out_trade_no", requestMsg.get("outTradeNo")));
		
		if (null == tradeOrderAlipay || OrderStatus.FAIL.getId().equals(tradeOrderAlipay.getOrderStatus())) {
			responseMsg.put(ConstEC.RETURNCODE, "11001");
			responseMsg.put(ConstEC.RETURNMSG, ApplicationYmlUtil.get("11001"));
			return responseMsg;
		}
		
		String tradeType = (String) requestMsg.get("tradeType");
		RouteConf routeConf = routeConfService.getOne(new QueryWrapper<RouteConf>()
				.eq("merc_id", requestMsg.get("mercId"))
				.eq("route_code", requestMsg.get("routeCode"))
				.eq("trade_type", tradeType)
				.eq("platform", requestMsg.get("platform"))
				);

		//组装银行请求报文
		Map<String, Object> contentMap = new HashMap<String, Object>();
		contentMap.put("out_trade_no", requestMsg.get("outTradeNo"));
		
		String content = JSONObject.toJSONString(contentMap);
		
		Map<String, Object> bankMap = new HashMap<String, Object>();
		
		bankMap.put("app_id", routeConf.getAppId());
		bankMap.put("method", "alipay.trade.query");
		bankMap.put("charset", ConstEC.ENCODE_UTF8);
		bankMap.put("sign_type", "RSA2");
		bankMap.put("format", "JSON");
		bankMap.put("timestamp", DateTimeUtil.formatTimestamp2String(new Date(), "yyy-MM-dd HH:mm:ss"));
		bankMap.put("version", "1.0");
		bankMap.put("biz_content", content);
		
		String plain = Sign.getPlain(bankMap, true);
		String sign = AlipaySignature.rsa256Sign(plain, routeConf.getPrivateKey(), ConstEC.ENCODE_UTF8);
		
		bankMap.put("sign", sign);
		
		String paramStr = Sign.getPlainURLEncoder(bankMap, ConstEC.ENCODE_UTF8);
		
		content = "biz_content=" + URLEncoder.encode(content, ConstEC.ENCODE_UTF8);
		
		log.info("请求支付宝查询订单接口报文[{}]", bankMap);
		
		String responseStr = HttpClientUtil.httpsRequest(gatewayurl + "?" + paramStr, "POST",	content);
		
		log.info("响应支付宝查询订单接口报文[{}]", responseStr);
		
		Map<String, Object> resultMap = verify(responseStr, "alipay_trade_query_response", routeConf);
		
		if (null == resultMap.get("code")) {
			responseMsg.put(ConstEC.RETURNCODE, "11001");
			responseMsg.put(ConstEC.RETURNMSG, ApplicationYmlUtil.get("11001"));
			return responseMsg;
		}
		
		if (!ConstEC.SUCCESS_10000.equals(resultMap.get("code"))) {
			responseMsg.put(ConstEC.RETURNCODE, resultMap.get("code"));
			responseMsg.put(ConstEC.RETURNMSG, resultMap.get("sub_msg"));
			return responseMsg;
		}
		
		String tradeStatus = (String) resultMap.get("trade_status");
		
		if (null != tradeStatus && tradeStatus.equals(ConstEC.TRADE_SUCCESS)) {
			log.info("响应支付宝查询订单接口报文[{}]", responseStr);
			Date payTime = null;
			
			if (null != resultMap.get("send_pay_date")) {
				payTime = DateTimeUtil.formatStringToDate(resultMap.get("send_pay_date").toString(), "yyyy-MM-dd HH:mm:ss");
			} else {
				payTime = new Date();
			}
			
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("routeCode", RouteCode.ALIPAY.getId());
			data.put("openId", resultMap.get("buyer_user_id"));
			data.put("appId", routeConf.getAppId());
			data.put("tradeType", tradeType);
			data.put("payDate", DateTimeUtil.formatTimestamp2String(payTime, "yyyy-MM-dd"));
			data.put("payTime", DateTimeUtil.formatTimestamp2String(payTime, "HH:mm:ss"));
			data.put("outTradeNo", resultMap.get("out_trade_no"));
			data.put("bankTradeNo", resultMap.get("trade_no"));
			data.put("price", resultMap.get("total_amount"));
			data.put("returnCode", "10000");
			data.put("returnMsg", "交易成功");
			
			DepositProcHandler handler = (DepositProcHandler) SpringContextHolder.getBean(RouteCode.ALIPAY.getId().toLowerCase() + ConstEC.DEPOSITPROCHANDLER);
			handler.proc(data);
		}
		
		responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
		responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
		return responseMsg;
	}

	@Override
	public ResponseMsg refundOrder(RequestMsg requestMsg) throws Exception {
		ResponseMsg responseMsg = new ResponseMsg();
		
		RouteConf routeConf = routeConfService.getOne(new QueryWrapper<RouteConf>()
				.eq("merc_id", requestMsg.get("mercId"))
				.eq("route_code", requestMsg.get("routeCode"))
				.eq("trade_type", requestMsg.get("tradeType"))
				.eq("platform", requestMsg.get("platform"))
				);
		
		
		TradeRefundAlipay tradeRefundAlipay = new TradeRefundAlipay();
		BeanUtils.populate(tradeRefundAlipay, requestMsg.getMap());
		
		String seq = seqIncrService.nextVal(SeqIncrType.OUT_REFUND_NO_ALIPAY.getId(),8, Align.LEFT);
		String outRefundNo = DateTimeUtil.date8() + seq;
		
		tradeRefundAlipay.setOrderStatus(OrderStatus.REFUND_WAIT.getId());
		tradeRefundAlipay.setOutRefundNo(outRefundNo);
		
		tradeRefundAlipay.setRefundDate(DateTimeUtil.date10());
		tradeRefundAlipay.setRefundTime(DateTimeUtil.time8());
		
		tradeRefundAlipayService.save(tradeRefundAlipay);
		
		//组装银行统一下单请求报文
		Map<String, Object> contentMap = new HashMap<String, Object>();
		contentMap.put("out_trade_no", requestMsg.get("outTradeNo"));
		contentMap.put("refund_amount", tradeRefundAlipay.getPrice());
		contentMap.put("out_request_no", outRefundNo);
		
		String content = JSONObject.toJSONString(contentMap);
		
		Map<String, Object> bankMap = new HashMap<String, Object>();
		bankMap.put("app_id", routeConf.getAppId());
		bankMap.put("method", "alipay.trade.refund");
		bankMap.put("charset", ConstEC.ENCODE_UTF8);
		bankMap.put("sign_type", "RSA2");
		bankMap.put("timestamp", DateTimeUtil.formatTimestamp2String(new Date(), "yyy-MM-dd HH:mm:ss"));
		bankMap.put("version", "1.0");
		bankMap.put("biz_content", content);
		
		String plain = Sign.getPlain(bankMap, true);
		String sign = AlipaySignature.rsa256Sign(plain, routeConf.getPrivateKey(), ConstEC.ENCODE_UTF8);
		
		bankMap.put("sign", sign);
		
		String paramStr = Sign.getPlainURLEncoder(bankMap, ConstEC.ENCODE_UTF8);
		
		content = "biz_content=" + URLEncoder.encode(content, ConstEC.ENCODE_UTF8);
		
		log.info("请求支付宝订单退款接口报文[{}]", bankMap);
		
		String responseStr = HttpClientUtil.httpsRequest(gatewayurl + "?" + paramStr, "POST",	content);
		
		log.info("响应支付宝订单退款接口报文[{}]", responseStr);
		Map<String, Object> resultMap = verify(responseStr, "alipay_trade_refund_response", routeConf);
		
		
		if (null == resultMap.get("code") || !ConstEC.SUCCESS_10000.equals(resultMap.get("code"))) {
			responseMsg.put(ConstEC.RETURNCODE, "11001");
			responseMsg.put(ConstEC.RETURNMSG, ApplicationYmlUtil.get("11001"));
			return responseMsg;
		}

		if (!ConstEC.SUCCESS_10000.equals(resultMap.get("code"))) {
			String returnCode = (String) resultMap.get("sub_code");
			String returnMsg = (String) resultMap.get("sub_msg");
			tradeRefundAlipay.setReturnCode(returnCode);
			tradeRefundAlipay.setReturnMsg(returnMsg);
			
			tradeRefundAlipayService.updateById(tradeRefundAlipay);
			
			responseMsg.put(ConstEC.RETURNCODE, resultMap.get("code"));
			responseMsg.put(ConstEC.RETURNMSG, resultMap.get("sub_msg"));
			return responseMsg;
		}
		
		tradeRefundAlipay.setOrderStatus(OrderStatus.REFUND_WAIT.getId());
		
		tradeRefundAlipayService.updateById(tradeRefundAlipay);
		
		responseMsg.put("refundNo", requestMsg.get("refundNo"));
		responseMsg.put("outRefundNo", outRefundNo);
		responseMsg.put("bankRefundNo", "");
		
		responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
		responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
		return responseMsg;
	}

	@Override
	public ResponseMsg queryRefundOrder(RequestMsg requestMsg) throws Exception {
		
		TradeRefundAlipay tradeRefundAlipay = tradeRefundAlipayService.getOne(
				new QueryWrapper<TradeRefundAlipay>().eq("out_refund_no", requestMsg.get("outRefundNo")));
		
		ResponseMsg responseMsg = new ResponseMsg();
		if (OrderStatus.SUCCESS.getId().equals(tradeRefundAlipay.getOrderStatus())) {
			responseMsg.put("orderStatus", tradeRefundAlipay.getOrderStatus());
			responseMsg.put("refundChannel", tradeRefundAlipay.getRefundChannel());
			responseMsg.put("actualPrice", tradeRefundAlipay.getActualPrice());
			responseMsg.put("bankReturnDate", tradeRefundAlipay.getBankReturnDate());
			responseMsg.put("bankReturnTime", tradeRefundAlipay.getBankReturnTime());
			responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
			responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
			
			return responseMsg;
		}
		
		String tradeType = (String) requestMsg.get("tradeType");
		RouteConf routeConf = routeConfService.getOne(new QueryWrapper<RouteConf>()
				.eq("merc_id", requestMsg.get("mercId"))
				.eq("route_code", requestMsg.get("routeCode"))
				.eq("trade_type", tradeType)
				.eq("platform", requestMsg.get("platform"))
				);
		
		//组装银行统一下单请求报文
		Map<String, Object> contentMap = new HashMap<String, Object>();
		contentMap.put("out_trade_no", requestMsg.get("outTradeNo"));
		contentMap.put("out_request_no", tradeRefundAlipay.getOutRefundNo());
		
		String content = JSONObject.toJSONString(contentMap);
		
		Map<String, Object> bankMap = new HashMap<String, Object>();
		bankMap.put("app_id", routeConf.getAppId());
		bankMap.put("method", "alipay.trade.fastpay.refund.query");
		bankMap.put("charset", ConstEC.ENCODE_UTF8);
		bankMap.put("sign_type", "RSA2");
		bankMap.put("timestamp", DateTimeUtil.formatTimestamp2String(new Date(), "yyy-MM-dd HH:mm:ss"));
		bankMap.put("version", "1.0");
		bankMap.put("biz_content", content);
		
		String plain = Sign.getPlain(bankMap, true);
		String sign = AlipaySignature.rsa256Sign(plain, routeConf.getPrivateKey(), ConstEC.ENCODE_UTF8);
		
		bankMap.put("sign", sign);
		
		String paramStr = Sign.getPlainURLEncoder(bankMap, ConstEC.ENCODE_UTF8);
		
		content = "biz_content=" + URLEncoder.encode(content, ConstEC.ENCODE_UTF8);
		
//		log.info("请求支付宝退款查询接口报文[{}]", bankMap);
		
		String responseStr = HttpClientUtil.httpsRequest(gatewayurl + "?" + paramStr, "POST",	content);
		
//		log.info("响应支付宝退款查询接口报文[{}]", responseStr);
		Map<String, Object> resultMap = verify(responseStr, "alipay_trade_fastpay_refund_query_response", routeConf);
		
		if (null == resultMap) {
			responseMsg.put(ConstEC.RETURNCODE, "11001");
			responseMsg.put(ConstEC.RETURNMSG, ApplicationYmlUtil.get("11001"));
			return responseMsg;
		}
		
		String refundStatus = resultMap.get("sub_code") == null ? "" : resultMap.get("sub_code").toString();
		
		switch (refundStatus) {
		case "ACQ.TRADE_HAS_SUCCESS":
			BigDecimal actualPrice = new BigDecimal(resultMap.get("refund_amount").toString());
			
			Date bankReturnDate = new Date();
			if (null != resultMap.get("gmt_refund_pay") && !StringUtils.isBlank(resultMap.get("gmt_refund_pay").toString().trim())) {
				bankReturnDate = DateTimeUtil.formatStringToDate(resultMap.get("gmt_refund_pay").toString(), "yyyy-MM-dd HH:mm:ss");
			} 
			
			tradeRefundAlipay.setRefundChannel(resultMap.get("refund_channel_0").toString());
			tradeRefundAlipay.setActualPrice(actualPrice);
			tradeRefundAlipay.setOrderStatus(OrderStatus.SUCCESS.getId());
			tradeRefundAlipay.setBankReturnDate(DateTimeUtil.date10(bankReturnDate));
			tradeRefundAlipay.setBankReturnTime(DateTimeUtil.time8(bankReturnDate));
			tradeRefundAlipay.setReturnCode(ConstEC.SUCCESS_10000);
			tradeRefundAlipay.setReturnMsg(ConstEC.SUCCESS_MSG);
			
			tradeRefundAlipayService.updateById(tradeRefundAlipay);
			responseMsg.put("orderStatus", tradeRefundAlipay.getOrderStatus());
			responseMsg.put("refundChannel", tradeRefundAlipay.getRefundChannel());
			responseMsg.put("actualPrice", actualPrice);
			responseMsg.put("bankReturnDate", tradeRefundAlipay.getBankReturnDate());
			responseMsg.put("bankReturnTime", tradeRefundAlipay.getBankReturnTime());
			responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
			responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
			
			break;
		default:
		}
		
		return responseMsg;
	}

	

	private void unifiedOrderResponseMsg(RequestMsg requestMsg, RouteConf routeConf, Map<String, Object> bankMap, TreeMap<String, Object> contentMap, ResponseMsg responseMsg) throws Exception {
		
		String tradeType = (String) requestMsg.get("tradeType");
		
		Map<String, Object> dataMap = new HashMap<String, Object>();
		if (TradeType.APP.getId().equals(tradeType)) {
			//组装银行支付请求报文
			dataMap.put("payData", WebUtils.buildQuery(bankMap, ConstEC.ENCODE_UTF8));
			
		} else if (TradeType.MWEB.getId().equals(tradeType)) {
			//组装银行支付请求报文
			
			String bizContent = (String) bankMap.get("biz_content");
			bizContent = bizContent.replace("\"", "&quot;");
			
			bankMap.put("biz_content", "");
			
			String requestUrl = "https://openapi.alipay.com/gateway.do?" + WebUtils.buildQuery(bankMap, ConstEC.ENCODE_UTF8);
			
			dataMap.put("requestUrl", requestUrl);
			dataMap.put("bizContent", bizContent);
		}
		
		responseMsg.put(ConstEC.DATA, dataMap);
	}

	private Map<String, Object> verify(String jsonStr, String nodeName, RouteConf routeConf) throws Exception {
		Map<String, Object> resultMap = (Map<String, Object>) JSONObject.parseObject(jsonStr, Map.class);
		
		Map<String, Object> bodyMap = (Map<String, Object>) resultMap.get(nodeName);
		String sign = resultMap.get("sign").toString();
		
		int signDataStartIndex = jsonStr.indexOf(nodeName) + nodeName.length() + 2;
		
		String content = AlipaySignature.extractSignContent(jsonStr, signDataStartIndex);
		
		boolean verify = AlipaySignature.rsa256CheckContent(content, sign, routeConf.getPublicKey(), "UTF-8");
		
		if (!verify) {
			log.error("支付宝返回报文验签失败,待签名串[{}],支付宝返回签名串[{}]", content,
					sign);
			throw new BusiException("11012", ApplicationYmlUtil.get("11012"));
		}
		
		return bodyMap;
		
	}
}
