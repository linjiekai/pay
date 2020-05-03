package com.mppay.gateway.handler.impl;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.mppay.core.utils.*;
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
import com.mppay.core.constant.GoodsPriceType;
import com.mppay.core.constant.OrderStatus;
import com.mppay.core.constant.RouteCode;
import com.mppay.core.constant.SeqIncrType;
import com.mppay.core.constant.TradeType;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.Sign;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BankBusiHandler;
import com.mppay.gateway.handler.DepositProcHandler;
import com.mppay.service.entity.Route;
import com.mppay.service.entity.RouteConf;
import com.mppay.service.entity.TradeOrderWeixin;
import com.mppay.service.entity.TradeRefundWeixin;
import com.mppay.service.service.IRouteConfService;
import com.mppay.service.service.IRouteService;
import com.mppay.service.service.ISeqIncrService;
import com.mppay.service.service.ITradeOrderWeixinService;
import com.mppay.service.service.ITradeRefundWeixinService;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;

import lombok.extern.slf4j.Slf4j;

@Service("weixinBankBusiHandler")
@Slf4j
public class WeixinBankBusiHandlerImpl implements BankBusiHandler {

    @Autowired
    private ITradeOrderWeixinService tradeOrderWeixinService;

    @Autowired
    private ITradeRefundWeixinService tradeRefundWeixinService;

    @Autowired
    private ISeqIncrService seqIncrService;

    @Autowired
    private IRouteConfService routeConfService;

    @Autowired
    private IRouteService routeService;

    @Value("${weixin.orderquery}")
    private String weixinOrderQuery;
    @Value("${weixin.refundquery}")
    private String weixinRefundQuery;
    @Value("${weixin.unifiedorder}")
    private String weixinUnifiedOrder;
    @Value("${weixin.refund}")
    private String weixinRefund;

    @Override
    public void unifiedOrder(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        String periodUnit = (String) requestMsg.get("periodUnit");
        Integer period = (Integer) requestMsg.get("period");

        Route route = routeService.getOne(new QueryWrapper<Route>().eq("route_code", requestMsg.get("routeCode")));

        String tradeType = (String) requestMsg.get("tradeType");

        if (null != requestMsg.get("sysCnl") && requestMsg.get("sysCnl").toString().equals("WX-PUBLIC")) {
            tradeType = "PUBLIC";
        }

        RouteConf routeConf = routeConfService.getOne(new QueryWrapper<RouteConf>()
                .eq("merc_id", requestMsg.get("mercId"))
                .eq("route_code", requestMsg.get("routeCode"))
                .eq("trade_type", tradeType)
                .eq("platform", requestMsg.get("platform"))
        );

        TradeOrderWeixin tradeOrderWeixin = new TradeOrderWeixin();
        String seq = seqIncrService.nextVal(SeqIncrType.OUT_TRADE_NO_WEIXIN.getId(), SeqIncrType.OUT_TRADE_NO_WEIXIN.getLength(), Align.LEFT);
        String outTradeNo = DateTimeUtil.date8() + seq;

        requestMsg.put("notifyUrl", route.getNotifyUrl());
        requestMsg.put("outTradeNo", outTradeNo);

        BeanUtils.populate(tradeOrderWeixin, requestMsg.getMap());

        tradeOrderWeixin.setTradeDate(DateTimeUtil.date10());
        tradeOrderWeixin.setTradeTime(DateTimeUtil.time8());
        tradeOrderWeixin.setBankMercId(routeConf.getBankMercId());
        tradeOrderWeixin.setOrderStatus(OrderStatus.ADVANCE.getId());
        //创建订单流水
        tradeOrderWeixinService.save(tradeOrderWeixin);

        BigDecimal price = new BigDecimal(requestMsg.get("price").toString());
        //元转分乘于100
        price = price.multiply(new BigDecimal(100));

        //组装银行统一下单请求报文
        Map<String, Object> bankMap = new HashMap<String, Object>();
        bankMap.put("appid", routeConf.getAppId());
        bankMap.put("mch_id", routeConf.getBankMercId());
        bankMap.put("nonce_str", CharacterUtil.getRandomString(32));
        bankMap.put("sign_type", "MD5");
        bankMap.put("body", GoodsPriceType.parasName(requestMsg.get("platform").toString()));
        bankMap.put("out_trade_no", requestMsg.get("outTradeNo"));
        bankMap.put("fee_type", "CNY");
        bankMap.put("total_fee", price.intValue());
        bankMap.put("spbill_create_ip", requestMsg.get("clientIp"));
        bankMap.put("notify_url", requestMsg.get("notifyUrl"));
        bankMap.put("trade_type", requestMsg.get("tradeType"));
        bankMap.put("openid", requestMsg.get("openId"));
        bankMap.put("time_expire", DateTimeUtil.formatTimestamp2String(DateTimeUtil.getPeriodTime(period, periodUnit), DateUtil.DATEFORMAT_9));

        String plain = Sign.getPlain(bankMap);
        plain += "&key=" + routeConf.getPrivateKey();
        log.info("plain:" + plain);
        String sign = Sign.signToHex(plain);

        bankMap.put("sign", sign);

        // 组装报文
        XStream xStream = new XStream(new DomDriver("UTF-8", new XmlFriendlyNameCoder("-_", "_")));
        xStream.alias("xml", Map.class);
        xStream.registerConverter(new MapEntryConverter());
        String requestXml = xStream.toXML(bankMap);

        log.info("请求微信统一下单接口报文[{}]", requestXml);

        String responseXml = HttpClientUtil.httpsRequest(weixinUnifiedOrder, "POST",
                requestXml);

        log.info("响应微信统一下单接口报文[{}]", responseXml);
        Map<String, Object> resultMap = verify(responseXml, routeConf);

        if (!ConstEC.SUCCESS.equals(resultMap.get("return_code")) || !ConstEC.SUCCESS.equals(resultMap.get("result_code"))) {
            String returnCode = (String) resultMap.get("err_code");
            String returnMsg = (String) resultMap.get("err_code_des");
            tradeOrderWeixin.setReturnCode(returnCode);
            tradeOrderWeixin.setReturnMsg(returnMsg);
            tradeOrderWeixin.setOrderStatus(OrderStatus.FAIL.getId());

            tradeOrderWeixinService.updateById(tradeOrderWeixin);
            responseMsg.put(ConstEC.RETURNCODE, "11001");
            responseMsg.put(ConstEC.RETURNMSG, ApplicationYmlUtil.get("11001"));
            return;
        }

        unifiedOrderResponseMsg(requestMsg, routeConf, resultMap, responseMsg);

        //二维码
        tradeOrderWeixin.setQrcUrl(resultMap.get("code_url") == null ? null : resultMap.get("code_url").toString());

        tradeOrderWeixin.setOrderStatus(OrderStatus.WAIT_PAY.getId());
        tradeOrderWeixinService.updateById(tradeOrderWeixin);

        responseMsg.put("appId", routeConf.getAppId());
        responseMsg.put("outTradeNo", outTradeNo);
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, "成功");
    }

    @Override
    public ResponseMsg queryOrder(RequestMsg requestMsg) throws Exception {
        ResponseMsg responseMsg = new ResponseMsg();

        TradeOrderWeixin tradeOrderWeixin = tradeOrderWeixinService.getOne(new QueryWrapper<TradeOrderWeixin>().eq("out_trade_no", requestMsg.get("outTradeNo")));

        if (null == tradeOrderWeixin || OrderStatus.FAIL.getId().equals(tradeOrderWeixin.getOrderStatus())) {
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

        //组装银行统一下单请求报文
        Map<String, Object> bankMap = new HashMap<String, Object>();
        bankMap.put("appid", routeConf.getAppId());
        bankMap.put("mch_id", routeConf.getBankMercId());
        bankMap.put("nonce_str", CharacterUtil.getRandomString(32));
        bankMap.put("out_trade_no", requestMsg.get("outTradeNo"));

        String plain = Sign.getPlain(bankMap);
        plain += "&key=" + routeConf.getPrivateKey();
        String sign = Sign.signToHex(plain);

        bankMap.put("sign", sign);

        // 组装报文
        XStream xStream = new XStream(new DomDriver("UTF-8", new XmlFriendlyNameCoder("-_", "_")));
        xStream.alias("xml", Map.class);
        xStream.registerConverter(new MapEntryConverter());
        String requestXml = xStream.toXML(bankMap);

//		log.info("请求微信查询订单接口报文[{}]", requestXml);

        String responseXml = HttpClientUtil.httpsRequest(weixinOrderQuery, "POST",
                requestXml);

        Map<String, Object> resultMap = verify(responseXml, routeConf);

        if (null == resultMap) {
            responseMsg.put(ConstEC.RETURNCODE, "11001");
            responseMsg.put(ConstEC.RETURNMSG, ApplicationYmlUtil.get("11001"));
            return responseMsg;
        }

        String tradeState = (String) resultMap.get("trade_state");

        switch (tradeState) {
            case "SUCCESS":
                log.info("响应微信查询订单接口报文[{}]", responseXml);
                BigDecimal price = new BigDecimal(resultMap.get("total_fee").toString());

                price = price.multiply(new BigDecimal(0.01)).setScale(2, BigDecimal.ROUND_HALF_UP);

                Date payTime = DateTimeUtil.formatStringToDate(resultMap.get("time_end").toString(), "yyyyMMddHHmmss");

                Map<String, Object> data = new HashMap<String, Object>();
                data.put("routeCode", RouteCode.WEIXIN.getId());
                data.put("openId", resultMap.get("openid"));
                data.put("appId", resultMap.get("appid"));
                data.put("tradeType", resultMap.get("trade_type"));
                data.put("fundBank", resultMap.get("bank_type"));
                data.put("payDate", DateTimeUtil.formatTimestamp2String(payTime, "yyyy-MM-dd"));
                data.put("payTime", DateTimeUtil.formatTimestamp2String(payTime, "HH:mm:ss"));
                data.put("outTradeNo", resultMap.get("out_trade_no"));
                data.put("bankTradeNo", resultMap.get("transaction_id"));
                data.put("price", price);
                data.put("returnCode", "10000");
                data.put("returnMsg", "交易成功");

                DepositProcHandler handler = (DepositProcHandler) SpringContextHolder.getBean(RouteCode.WEIXIN.getId().toLowerCase() + ConstEC.DEPOSITPROCHANDLER);
                handler.proc(data);
                break;
            case "CLOSED":
//			tradeOrderWeixin.setOrderStatus(OrderStatus.FAIL.getId());
//			tradeOrderWeixinService.update(tradeOrderWeixin, 
//					new UpdateWrapper<TradeOrderWeixin>()
//					.eq("out_trade_no", tradeOrderWeixin.getOutTradeNo())
//					.eq("order_status", OrderStatus.WAIT_PAY.getId())
//					);
//			break;
            default:
        }

        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        return responseMsg;
    }

    @Override
    public ResponseMsg refundOrder(RequestMsg requestMsg) throws Exception {
        ResponseMsg responseMsg = new ResponseMsg();
        String outTradeNo = requestMsg.get("outTradeNo") + "";

        RouteConf routeConf = routeConfService.getOne(new QueryWrapper<RouteConf>()
                .eq("merc_id", requestMsg.get("mercId"))
                .eq("route_code", requestMsg.get("routeCode"))
                .eq("trade_type", requestMsg.get("tradeType"))
                .eq("platform", requestMsg.get("platform"))
        );

        // 证书密钥
        String keyWord = routeConf.getBankMercId();
        // 证书路径
        String keyPath = routeConf.getKeyPath();
        // 加签验签密钥
        String key = routeConf.getPrivateKey();

        TradeRefundWeixin tradeRefundWeixin = new TradeRefundWeixin();
        BeanUtils.populate(tradeRefundWeixin, requestMsg.getMap());
        String outRefundNo = DateTimeUtil.date8() + seqIncrService.nextVal(SeqIncrType.OUT_REFUND_NO_WEIXIN.getId(), 10, Align.LEFT);
        tradeRefundWeixin.setOutRefundNo(outRefundNo);
        tradeRefundWeixin.setRefundDate(DateTimeUtil.date10());
        tradeRefundWeixin.setRefundTime(DateTimeUtil.time8());
        tradeRefundWeixinService.save(tradeRefundWeixin);


        BigDecimal price = tradeRefundWeixin.getPrice().multiply(new BigDecimal(100));

        BigDecimal applyPrice = tradeRefundWeixin.getApplyPrice().multiply(new BigDecimal(100));


        //组装银行统一下单请求报文
        Map<String, Object> bankMap = new HashMap<String, Object>();
        bankMap.put("appid", routeConf.getAppId());
        bankMap.put("mch_id", routeConf.getBankMercId());
        bankMap.put("nonce_str", CharacterUtil.getRandomString(32));
        bankMap.put("out_trade_no", outTradeNo);
        bankMap.put("out_refund_no", outRefundNo);
        bankMap.put("total_fee", price.intValue());
        bankMap.put("refund_fee", applyPrice.intValue());

        String plain = Sign.getPlain(bankMap);
        plain += "&key=" + key;
        String sign = Sign.signToHex(plain);

        bankMap.put("sign", sign);

        // 组装报文
        XStream xStream = new XStream(new DomDriver("UTF-8", new XmlFriendlyNameCoder("-_", "_")));
        xStream.alias("xml", Map.class);
        xStream.registerConverter(new MapEntryConverter());
        String requestXml = xStream.toXML(bankMap);

        log.info("请求微信订单退款接口报文[{}]", requestXml);

        String responseXml = HttpClientUtil.httpsRequestCert(weixinRefund, "POST",
                requestXml, keyWord, keyPath);

        log.info("响应微信订单退款接口报文[{}]", responseXml);

        Map<String, Object> resultMap = verify(responseXml, routeConf);

        if (!ConstEC.SUCCESS.equals(resultMap.get("return_code")) || !ConstEC.SUCCESS.equals(resultMap.get("result_code"))) {
            String returnCode = (String) resultMap.get("err_code");
            String returnMsg = (String) resultMap.get("err_code_des");
            tradeRefundWeixin.setReturnCode(returnCode);
            tradeRefundWeixin.setReturnMsg(returnMsg);

            tradeRefundWeixinService.updateById(tradeRefundWeixin);
            responseMsg.put(ConstEC.RETURNCODE, "11001");
            responseMsg.put(ConstEC.RETURNMSG, ApplicationYmlUtil.get("11001"));
            return responseMsg;
        }

        tradeRefundWeixin.setBankRefundNo(resultMap.get("refund_id").toString());
        tradeRefundWeixin.setOrderStatus(OrderStatus.REFUND_WAIT.getId());

        tradeRefundWeixinService.updateById(tradeRefundWeixin);

        responseMsg.put("refundNo", requestMsg.get("refundNo"));
        responseMsg.put("outRefundNo", outRefundNo);
        responseMsg.put("bankRefundNo", tradeRefundWeixin.getBankRefundNo());

        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        return responseMsg;
    }

    @Override
    public ResponseMsg queryRefundOrder(RequestMsg requestMsg) throws Exception {

        TradeRefundWeixin tradeRefundWeixin = tradeRefundWeixinService.getOne(
                new QueryWrapper<TradeRefundWeixin>().eq("out_refund_no", requestMsg.get("outRefundNo")));

        ResponseMsg responseMsg = new ResponseMsg();
        if (OrderStatus.SUCCESS.getId().equals(tradeRefundWeixin.getOrderStatus())) {
            responseMsg.put("orderStatus", tradeRefundWeixin.getOrderStatus());
            responseMsg.put("refundChannel", tradeRefundWeixin.getRefundChannel());
            responseMsg.put("actualPrice", tradeRefundWeixin.getActualPrice());
            responseMsg.put("bankReturnDate", tradeRefundWeixin.getBankReturnDate());
            responseMsg.put("bankReturnTime", tradeRefundWeixin.getBankReturnTime());
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

        //组装银行请求报文
        Map<String, Object> bankMap = new HashMap<String, Object>();
        bankMap.put("appid", routeConf.getAppId());
        bankMap.put("mch_id", routeConf.getBankMercId());
        bankMap.put("nonce_str", CharacterUtil.getRandomString(32));
        bankMap.put("out_refund_no", tradeRefundWeixin.getOutRefundNo());

        String plain = Sign.getPlain(bankMap);
        plain += "&key=" + routeConf.getPrivateKey();
        String sign = Sign.signToHex(plain);

        bankMap.put("sign", sign);

        // 组装报文
        XStream xStream = new XStream(new DomDriver("UTF-8", new XmlFriendlyNameCoder("-_", "_")));
        xStream.alias("xml", Map.class);
        xStream.registerConverter(new MapEntryConverter());
        String requestXml = xStream.toXML(bankMap);

//		log.info("请求微信查询退款接口报文[{}]", requestXml);

        String responseXml = HttpClientUtil.httpsRequest(weixinRefundQuery, "POST",
                requestXml);

        Map<String, Object> resultMap = verify(responseXml, routeConf);
        ;

        if (!ConstEC.SUCCESS.equals(resultMap.get("return_code")) || !ConstEC.SUCCESS.equals(resultMap.get("result_code"))) {
            tradeRefundWeixin.setReturnCode(resultMap.get("err_code") + "");
            tradeRefundWeixin.setReturnMsg(resultMap.get("err_code_des") + "");

            tradeRefundWeixinService.updateById(tradeRefundWeixin);
            responseMsg.put(ConstEC.RETURNCODE, "11001");
            responseMsg.put(ConstEC.RETURNMSG, ApplicationYmlUtil.get("11001"));
            return responseMsg;
        }

        String refundStatus = (String) resultMap.get("refund_status_0");

        log.info("响应微信查询退款接口报文[{}]", responseXml);

        switch (refundStatus) {
            case "SUCCESS":
                BigDecimal actualPrice = new BigDecimal(resultMap.get("refund_fee_0").toString());
                actualPrice = actualPrice.multiply(new BigDecimal(0.01)).setScale(2, BigDecimal.ROUND_HALF_UP);

                Date bankReturnDate = DateTimeUtil.formatStringToDate(resultMap.get("refund_success_time_0").toString(), "yyyy-MM-dd HH:mm:ss");
                tradeRefundWeixin.setRefundChannel(resultMap.get("refund_channel_0").toString());
                tradeRefundWeixin.setActualPrice(actualPrice);
                tradeRefundWeixin.setOrderStatus(OrderStatus.SUCCESS.getId());
                tradeRefundWeixin.setReturnCode(ConstEC.SUCCESS_10000);
                tradeRefundWeixin.setReturnMsg(ConstEC.SUCCESS_MSG);
                tradeRefundWeixin.setBankReturnDate(DateTimeUtil.date10(bankReturnDate));
                tradeRefundWeixin.setBankReturnTime(DateTimeUtil.time8(bankReturnDate));

                tradeRefundWeixinService.updateById(tradeRefundWeixin);
                responseMsg.put("orderStatus", tradeRefundWeixin.getOrderStatus());
                responseMsg.put("refundChannel", tradeRefundWeixin.getRefundChannel());
                responseMsg.put("actualPrice", tradeRefundWeixin.getActualPrice());
                responseMsg.put("bankReturnDate", tradeRefundWeixin.getBankReturnDate());
                responseMsg.put("bankReturnTime", tradeRefundWeixin.getBankReturnTime());
                responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
                responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);

                break;
            case "REFUNDCLOSE":
                tradeRefundWeixin.setOrderStatus(OrderStatus.FAIL.getId());
                tradeRefundWeixin.setReturnCode(refundStatus);
                tradeRefundWeixin.setReturnMsg("退款关闭");
                tradeRefundWeixinService.updateById(tradeRefundWeixin);

                responseMsg.put("orderStatus", tradeRefundWeixin.getOrderStatus());
                responseMsg.put(ConstEC.RETURNCODE, refundStatus);
                responseMsg.put(ConstEC.RETURNMSG, "退款关闭");
                break;
            case "CHANGE":
                tradeRefundWeixin.setOrderStatus(OrderStatus.FAIL.getId());
                tradeRefundWeixin.setReturnCode(refundStatus);
                tradeRefundWeixin.setReturnMsg("退款异常");
                tradeRefundWeixinService.updateById(tradeRefundWeixin);

                responseMsg.put("orderStatus", tradeRefundWeixin.getOrderStatus());
                responseMsg.put(ConstEC.RETURNCODE, refundStatus);
                responseMsg.put(ConstEC.RETURNMSG, "退款关闭");
                break;
            case "PROCESSING":
                break;
            default:
        }

        return responseMsg;
    }

    private void unifiedOrderResponseMsg(RequestMsg requestMsg, RouteConf routeConf, Map<String, Object> resultMap, ResponseMsg responseMsg) throws Exception {

        String tradeType = (String) requestMsg.get("tradeType");

        Map<String, Object> dataMap = new HashMap<String, Object>();
        if (TradeType.APP.getId().equals(tradeType)) {
            //组装银行支付请求报文
            dataMap.put("appid", routeConf.getAppId());
            dataMap.put("partnerid", routeConf.getBankMercId());
            dataMap.put("prepayid", resultMap.get("prepay_id"));
            dataMap.put("package", "Sign=WXPay");
            dataMap.put("noncestr", CharacterUtil.getRandomString(32));
            dataMap.put("timestamp", DateTimeUtil.getTimeSecondStr());

        } else if (TradeType.JSAPI.getId().equals(tradeType)) {
            //组装银行支付请求报文
            dataMap.put("appId", routeConf.getAppId());
            dataMap.put("signType", "MD5");
            dataMap.put("package", "prepay_id=" + resultMap.get("prepay_id"));
            dataMap.put("nonceStr", CharacterUtil.getRandomString(32));
            dataMap.put("timeStamp", DateTimeUtil.getTimeSecondStr());

        } else if (TradeType.MWEB.getId().equals(tradeType)) {
            String mwebUrl = (String) resultMap.get("mweb_url");
            String callbackUrl = (String) requestMsg.get("callbackUrl");
            if (!StringUtils.isBlank(callbackUrl)) {
                mwebUrl += "&redirect_url=" + URLEncoder.encode(callbackUrl, "utf-8");
            }
            //组装银行支付请求报文
            dataMap.put("mwebUrl", mwebUrl);

            responseMsg.put(ConstEC.DATA, dataMap);
            return;
        }

        String plain = Sign.getPlain(dataMap);
        plain += "&key=" + routeConf.getPrivateKey();
        String sign = Sign.signToHex(plain);

        dataMap.put("sign", sign);

        responseMsg.put(ConstEC.DATA, dataMap);
    }

    private Map<String, Object> verify(String xmlStr, RouteConf routeConf) throws Exception {
        // 组装报文
        XStream xStream = new XStream(new DomDriver("UTF-8", new XmlFriendlyNameCoder("-_", "_")));
        xStream.alias("xml", Map.class);
        xStream.registerConverter(new MapEntryConverter());
        Map<String, Object> resultMap = (Map<String, Object>) xStream.fromXML(xmlStr);

        String plain = Sign.getPlain(resultMap);
        plain += "&key=" + routeConf.getPrivateKey();

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
