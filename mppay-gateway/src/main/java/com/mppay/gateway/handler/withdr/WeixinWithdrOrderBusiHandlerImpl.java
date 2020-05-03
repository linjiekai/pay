package com.mppay.gateway.handler.withdr;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.mppay.service.entity.WithdrOrder;
import com.mppay.service.service.*;
import com.mppay.service.service.common.ICipherService;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.MapEntryConverter;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.Align;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.OrderStatus;
import com.mppay.core.constant.RouteCode;
import com.mppay.core.constant.SeqIncrType;
import com.mppay.core.constant.TradeType;
import com.mppay.core.constant.WithdrOrderStatus;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.AESCoder;
import com.mppay.core.sign.Sign;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.core.utils.CharacterUtil;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.core.utils.HttpClientUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.DepositProcHandler;
import com.mppay.gateway.handler.WithdrOrderBusiHandler;
import com.mppay.service.entity.CardBind;
import com.mppay.service.entity.RouteWithdrConf;
import com.mppay.service.entity.RouteWithdrConf;
import com.mppay.service.entity.WithdrOrderWeixin;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;

import lombok.extern.slf4j.Slf4j;

@Service("weixinWithdrOrderBusiHandler")
@Slf4j
public class WeixinWithdrOrderBusiHandlerImpl implements WithdrOrderBusiHandler{

	@Autowired
	private IWithdrOrderWeixinService withdrOrderWeixinService;
	
	@Autowired
	private ISeqIncrService seqIncrService;
	
	@Autowired
	private IRouteWithdrConfService routeWithdrConfService;
	
	@Autowired
	private ICardBindService cardBindService;
	
	@Autowired
	private ICipherService iCipherService;

	@Autowired
	private IWithdrOrderService withdrOrderService;

	@Value("${weixin.transfersquery}")
	private String weixinTransfersQuery;

	@Value("${weixin.transfers}")
	private String weixinTransfers;
	@Override
	public ResponseMsg getOutMercInfo(RequestMsg requestMsg) throws Exception {
		ResponseMsg responseMsg = new ResponseMsg();
    	responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        return responseMsg;
	}
	
	/**
	 * 提现绑卡
	 *
	 * @param requestMsg
	 * @return
	 * @throws Exception
	 */
	@Override
	public ResponseMsg withdrCardBind(RequestMsg requestMsg) throws Exception {
		//微信没有
	    return null;
	}

	/**
	 * 提现解绑
	 *
	 * @param requestMsg
	 * @return
	 * @throws Exception
	 */
	@Override
	public ResponseMsg withdrUnCardBind(RequestMsg requestMsg) throws Exception {
		//微信没有
	    return null;
	}

	@Override
	public ResponseMsg withdrOrder(RequestMsg requestMsg) throws Exception{
		ResponseMsg responseMsg = new ResponseMsg();
		String withdrOrderNo = (String) requestMsg.get("withdrOrderNo");

		//查询绑卡信息
		WithdrOrder withdrOrder = withdrOrderService.getOne(new QueryWrapper<WithdrOrder>().eq("withdr_order_no", withdrOrderNo));
		CardBind cardBind = cardBindService.getOne(new QueryWrapper<CardBind>().eq("agr_no", withdrOrder.getAgrNo()).eq("merc_id", requestMsg.get("mercId")));

		String tradeType = cardBind.getTradeType();
		
		if (null != requestMsg.get("sysCnl") && requestMsg.get("sysCnl").toString().equals("WX-PUBLIC")) {
			tradeType = "PUBLIC";
		}
		
		RouteWithdrConf routeWithdrConf = routeWithdrConfService.getOne(new QueryWrapper<RouteWithdrConf>()
				.eq("merc_id", requestMsg.get("mercId"))
				.eq("route_code", requestMsg.get("routeCode"))
				.eq("trade_type", tradeType)
				.eq("platform", cardBind.getPlatform())
				);
		
		// 证书密钥
		String keyWord = routeWithdrConf.getBankMercId();
		// 证书路径
		String keyPath = routeWithdrConf.getKeyPath();
		// 加签验签密钥
		String key = routeWithdrConf.getPrivateKey();

		if (StringUtils.isBlank(withdrOrderNo)) {
			log.error("响应微信订单提现请求报文[{}]", requestMsg);
			return null;
		}
		
		WithdrOrderWeixin withdrOrderWeixin = withdrOrderWeixinService.getOne(new QueryWrapper<WithdrOrderWeixin>().eq("withdr_order_no", withdrOrderNo));  
		
		Map<String, Object> bankMap = new HashMap<String, Object>();
		
		String outTradeNo = null;
		
		String bankCardNo = null;
		
		if (null != withdrOrderWeixin) {
			
			if (withdrOrderWeixin.getOrderStatus().equals(OrderStatus.SUCCESS.getId())) {
				log.error("订单提现状态已成功[{}]", withdrOrderWeixin.getOrderStatus());
				
				responseMsg.put("outTradeNo", withdrOrderWeixin.getOutTradeNo());
				responseMsg.put("bankWithdrNo", withdrOrderWeixin.getBankWithdrNo());
				responseMsg.put("orderStatus", withdrOrderWeixin.getOrderStatus());
				responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
				responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
				return responseMsg;
			}
			
			bankCardNo = iCipherService.decryptAES(withdrOrderWeixin.getBankCardNo());

			BigDecimal price = withdrOrderWeixin.getPrice().multiply(new BigDecimal(100));
			
			bankMap.put("mch_appid", routeWithdrConf.getAppId());
			bankMap.put("mchid", routeWithdrConf.getBankMercId());
			bankMap.put("nonce_str", CharacterUtil.getRandomString(32));
			bankMap.put("partner_trade_no", withdrOrderWeixin.getOutTradeNo());
			bankMap.put("openid", bankCardNo);
			bankMap.put("check_name", withdrOrderWeixin.getCheckName());
			bankMap.put("re_user_name", withdrOrderWeixin.getBankCardName());
			bankMap.put("amount", price.intValue());
			bankMap.put("desc", "用户转账");
			bankMap.put("spbill_create_ip", withdrOrderWeixin.getClientIp());
			
		} else {
			//为空则是新订单
			withdrOrderWeixin = new WithdrOrderWeixin();
			BeanUtils.populate(withdrOrderWeixin, requestMsg.getMap());
			
			outTradeNo = DateTimeUtil.date8() + seqIncrService.nextVal(SeqIncrType.OUT_WITHDR_NO_WEIXIN.getId(), SeqIncrType.OUT_WITHDR_NO_WEIXIN.getLength(), Align.LEFT);

			withdrOrderWeixin.setOutTradeNo(outTradeNo);
			withdrOrderWeixin.setOrderDate(DateTimeUtil.date10());
			withdrOrderWeixin.setOrderTime(DateTimeUtil.time8());

			BigDecimal orgPrice = withdrOrder.getPrice();
			BigDecimal price = withdrOrder.getWithdrPrice();
			withdrOrderWeixin.setPrice(price);
			withdrOrderWeixin.setServicePrice(orgPrice.subtract(price));
			
			withdrOrderWeixinService.save(withdrOrderWeixin);
			
			bankCardNo = iCipherService.decryptAES(withdrOrderWeixin.getBankCardNo());
			
			price = price.multiply(new BigDecimal(100));
			//组装银行统一下单请求报文
			
			bankMap.put("mch_appid", routeWithdrConf.getAppId());
			bankMap.put("mchid", routeWithdrConf.getBankMercId());
			bankMap.put("nonce_str", CharacterUtil.getRandomString(32));
			bankMap.put("partner_trade_no", withdrOrderWeixin.getOutTradeNo());
			bankMap.put("openid", bankCardNo);
			bankMap.put("check_name", withdrOrderWeixin.getCheckName());
			bankMap.put("re_user_name", withdrOrderWeixin.getBankCardName());
			bankMap.put("amount", price.intValue());
			bankMap.put("desc", "用户转账");
			bankMap.put("spbill_create_ip", withdrOrderWeixin.getClientIp());
			
		}
		
		
		String plain = Sign.getPlain(bankMap);
		plain += "&key=" + key;
		String sign = Sign.signToHex(plain);
		
		bankMap.put("sign", sign);
		
		// 组装报文
		XStream xStream = new XStream(new DomDriver("UTF-8", new XmlFriendlyNameCoder("-_", "_")));
		xStream.alias("xml", Map.class);
		xStream.registerConverter(new MapEntryConverter());
		String requestXml = xStream.toXML(bankMap);
		
		log.info("请求微信订单提现接口报文[{}]", requestXml);
		
		String responseXml = HttpClientUtil.httpsRequestCert(weixinTransfers, "POST",
				requestXml, keyWord, keyPath);
		
		log.info("响应微信订单提现接口报文[{}]", responseXml);
		
		Map<String, Object> resultMap = verify(responseXml, routeWithdrConf);
		
		if (!ConstEC.SUCCESS.equals(resultMap.get("return_code")) || !ConstEC.SUCCESS.equals(resultMap.get("result_code"))) {
			withdrOrderWeixin.setReturnCode(resultMap.get("err_code") + "");
			withdrOrderWeixin.setReturnMsg(resultMap.get("err_code_des") + "");
			withdrOrderWeixinService.updateById(withdrOrderWeixin);
			
			responseMsg.put(ConstEC.RETURNCODE, "11001");
			responseMsg.put(ConstEC.RETURNMSG, ApplicationYmlUtil.get("11001"));
			return responseMsg;
		}
		
		withdrOrderWeixin.setBankWithdrNo(resultMap.get("payment_no").toString());
		withdrOrderWeixin.setOrderStatus(WithdrOrderStatus.BANK_WAIT.getId());
		
		withdrOrderWeixinService.updateById(withdrOrderWeixin);
		
		responseMsg.put("outTradeNo", withdrOrderWeixin.getOutTradeNo());
		responseMsg.put("bankWithdrNo", withdrOrderWeixin.getBankWithdrNo());
		responseMsg.put("orderStatus", withdrOrderWeixin.getOrderStatus());
		responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
		responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
		return responseMsg;
	}

	@Override
	public ResponseMsg queryWithdrOrder(RequestMsg requestMsg) throws Exception{
		WithdrOrderWeixin withdrOrderWeixin = withdrOrderWeixinService.getOne(
				new QueryWrapper<WithdrOrderWeixin>().eq("out_trade_no", requestMsg.get("outTradeNo")));
		
		ResponseMsg responseMsg = new ResponseMsg();
		if (OrderStatus.SUCCESS.getId().equals(withdrOrderWeixin.getOrderStatus())) {
			responseMsg.put("orderStatus", withdrOrderWeixin.getOrderStatus());
			responseMsg.put("outTradeNo", withdrOrderWeixin.getOutTradeNo());
			responseMsg.put("bankWithdrNo", withdrOrderWeixin.getBankWithdrNo());
			responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
			responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
			
			return responseMsg;
		}
		
		String tradeType = (String) requestMsg.get("sysCnl");
		
		if (null != requestMsg.get("sysCnl") && requestMsg.get("sysCnl").toString().equals("WX-PUBLIC")) {
			tradeType = "PUBLIC";
		}
		
		RouteWithdrConf routeWithdrConf = routeWithdrConfService.getOne(new QueryWrapper<RouteWithdrConf>()
				.eq("merc_id", requestMsg.get("mercId"))
				.eq("route_code", requestMsg.get("routeCode"))
				.eq("trade_type", tradeType)
				.eq("platform", requestMsg.get("platform"))
				);
		
		// 证书密钥
		String keyWord = routeWithdrConf.getBankMercId();
		// 证书路径
		String keyPath = routeWithdrConf.getKeyPath();
		
		//组装银行请求报文
		Map<String, Object> bankMap = new HashMap<String, Object>();
		bankMap.put("appid", routeWithdrConf.getAppId());
		bankMap.put("mch_id", routeWithdrConf.getBankMercId());
		bankMap.put("nonce_str", CharacterUtil.getRandomString(32));
		bankMap.put("partner_trade_no", withdrOrderWeixin.getOutTradeNo());
		
		String plain = Sign.getPlain(bankMap);
		plain += "&key=" + routeWithdrConf.getPrivateKey();
		String sign = Sign.signToHex(plain);
		bankMap.put("sign", sign);


		// 组装报文
		XStream xStream = new XStream(new DomDriver("UTF-8", new XmlFriendlyNameCoder("-_", "_")));
		xStream.alias("xml", Map.class);
		xStream.registerConverter(new MapEntryConverter());
		String requestXml = xStream.toXML(bankMap);
		
		String responseXml = HttpClientUtil.httpsRequestCert(weixinTransfersQuery, "POST",
				requestXml, keyWord, keyPath);
		
		Map<String, Object> resultMap = verify(responseXml, routeWithdrConf);
		
		log.info("响应微信查询提现接口报文[{}]", responseXml);
		
		if (!ConstEC.SUCCESS.equals(resultMap.get("return_code")) || !ConstEC.SUCCESS.equals(resultMap.get("result_code"))) {
			withdrOrderWeixin.setReturnCode(resultMap.get("err_code") + "");
			withdrOrderWeixin.setReturnMsg(resultMap.get("err_code_des") + "");
			withdrOrderWeixinService.updateById(withdrOrderWeixin);
			
			responseMsg.put(ConstEC.RETURNCODE, "11001");
			responseMsg.put(ConstEC.RETURNMSG, ApplicationYmlUtil.get("11001"));
			return responseMsg;
		}
		
		
		String status = (String) resultMap.get("status");
		
		switch (status) {
		case "SUCCESS":
			BigDecimal price = new BigDecimal(resultMap.get("payment_amount").toString());
			price = price.multiply(new BigDecimal(0.01)).setScale(2, BigDecimal.ROUND_HALF_UP);
			
			Date bankReturnDate = DateTimeUtil.formatStringToDate(resultMap.get("payment_time").toString(), "yyyy-MM-dd HH:mm:ss");
//			withdrOrderWeixin.setBankWithdrNo(resultMap.get("detail_id").toString());
//			withdrOrderWeixin.setOrderStatus(OrderStatus.SUCCESS.getId());
//			withdrOrderWeixin.setReturnCode(ConstEC.SUCCESS_10000);
//			withdrOrderWeixin.setReturnMsg(ConstEC.SUCCESS_MSG);
//			withdrOrderWeixin.setBankWithdrDate(DateTimeUtil.date10(bankReturnDate));
//			withdrOrderWeixin.setBankWithdrTime(DateTimeUtil.time8(bankReturnDate));
			
//			withdrOrderWeixinService.updateById(withdrOrderWeixin);
			
			//成功了需要处理下
	        Map<String, Object> data = new HashMap<String, Object>();
	        data.put("bankWithdrDate", DateTimeUtil.date10(bankReturnDate));
	        data.put("bankWithdrTime", DateTimeUtil.time8(bankReturnDate));
	        data.put("orderStatus", WithdrOrderStatus.SUCCESS.getId());
	        data.put("outTradeNo", withdrOrderWeixin.getOutTradeNo());
	        data.put("bankWithdrNo", resultMap.get("detail_id").toString());
	        data.put("price", price);
	        data.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
	        data.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
	        DepositProcHandler handler = (DepositProcHandler) SpringContextHolder.getBean(RouteCode.WEIXIN.getId().toLowerCase() + ConstEC.DEPOSITPROCHANDLER);
	       //更新订单状态等处理
	        handler.procWithdr(data);
	        
			responseMsg.put("orderStatus", withdrOrderWeixin.getOrderStatus());
			responseMsg.put("bankWithdrNo", withdrOrderWeixin.getBankWithdrNo());
			responseMsg.put("bankWithdrDate", withdrOrderWeixin.getBankWithdrDate());
			responseMsg.put("bankWithdrTime", withdrOrderWeixin.getBankWithdrTime());
			responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
			responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
			
			break;
		case "FAILED":
			withdrOrderWeixin.setOrderStatus(OrderStatus.FAIL.getId());
			withdrOrderWeixin.setReturnCode(status);
			withdrOrderWeixin.setReturnMsg(resultMap.get("reason ").toString());
			withdrOrderWeixinService.updateById(withdrOrderWeixin);
			
			responseMsg.put(ConstEC.RETURNCODE, status);
			responseMsg.put(ConstEC.RETURNMSG, resultMap.get("reason").toString());
			break;
		case "PROCESSING":
			break;
		default:
		}
		
		return responseMsg;
	}

	@Override
	public void beforeWithdrCardBind(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {

	}

	private Map<String, Object> verify(String xmlStr, RouteWithdrConf routeWithdrConf) throws Exception {
		// 组装报文
		XStream xStream = new XStream(new DomDriver("UTF-8", new XmlFriendlyNameCoder("-_", "_")));
		xStream.alias("xml", Map.class);
		xStream.registerConverter(new MapEntryConverter());
		Map<String, Object> resultMap = (Map<String, Object>) xStream.fromXML(xmlStr);
		
		String plain = Sign.getPlain(resultMap);
		plain += "&key=" + routeWithdrConf.getPrivateKey();
		
		String sign = (String) resultMap.get("sign");
		
		if (!StringUtils.isBlank(sign)) {
			if (!Sign.verifyToHex(plain, sign)) {
				log.error("微信返回报文验签失败,待签名串[{}],微信返回签名串[{}]", plain,
						sign);
				throw new BusiException("11012", ApplicationYmlUtil.get("11012"));
			}
		}
		
		return resultMap;
		
	}
}
