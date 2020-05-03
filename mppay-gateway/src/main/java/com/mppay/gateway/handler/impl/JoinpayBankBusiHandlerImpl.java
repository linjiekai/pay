package com.mppay.gateway.handler.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.*;
import com.mppay.core.constant.joinpay.JoinpayConstants;
import com.mppay.core.exception.BusiException;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.core.utils.DateUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.dto.platform.joinpay.JoinPayReq;
import com.mppay.gateway.dto.platform.joinpay.JoinpayRefundReq;
import com.mppay.gateway.dto.platform.joinpay.JoinpayRefundResp;
import com.mppay.gateway.dto.platform.joinpay.JoinpayResp;
import com.mppay.gateway.handler.BankBusiHandler;
import com.mppay.gateway.handler.DepositProcHandler;
import com.mppay.gateway.handler.utils.JoinpayUtil;
import com.mppay.gateway.handler.utils.RequestMsgUtil;
import com.mppay.service.entity.Route;
import com.mppay.service.entity.RouteConf;
import com.mppay.service.entity.TradeOrderJoinpay;
import com.mppay.service.entity.TradeRefundJoinpay;
import com.mppay.service.service.IRouteService;
import com.mppay.service.service.ISeqIncrService;
import com.mppay.service.service.ITradeOrderJoinpayService;
import com.mppay.service.service.ITradeRefundJoinpayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service("joinpayBankBusiHandler")
@Slf4j
public class JoinpayBankBusiHandlerImpl implements BankBusiHandler {


    @Value("${joinpay.version}")
    private String version;
    @Value("${joinpay.refundVersion}")
    private String refundVersion;
    @Value("${joinpay.payUrl}")
    private String payUrl;
    @Value("${joinpay.queryOrderUrl}")
    private String queryOrderUrl;
    @Value("${joinpay.refundUrl}")
    private String refundUrl;
    @Value("${joinpay.queryRefundUrl}")
    private String queryRefundUrl;
    @Value("${joinpay.tradeMerchantNo}")
    private String tradeMerchantNo;
    @Value("${joinpay.refundNotifyUrl}")
    private String refundNotifyUrl;

    @Resource
    private IRouteService routeService;
    @Resource
    private ISeqIncrService seqIncrService;
    @Resource
    private ITradeOrderJoinpayService iTradeOrderJoinpayService;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private ITradeRefundJoinpayService iTradeRefundJoinpayService;

    @Override
    public void unifiedOrder(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        log.info("|joinpay|unifiedOrder|开始：{}", JSON.toJSONString(requestMsg.getMap()));
        String routeCode = (String) requestMsg.get("routeCode");
        String mercId = (String) requestMsg.get("mercId");
        String platform = (String) requestMsg.get("platform");
        BigDecimal price = (BigDecimal) requestMsg.get("price");
        String tradeType = (String) requestMsg.get("tradeType");
        String bankCode = (String) requestMsg.get("bankCode");
        String openId = (String) requestMsg.get("openId");
        String tradeNo = (String) requestMsg.get("tradeNo");
        String sysCnl = (String) requestMsg.get("sysCnl");

        Route one = routeService.getOne(new QueryWrapper<Route>().eq("route_code", RouteCode.JOINPAY.getId()).last("limit 1"));
        // 路由配置
        RouteConf routeConf = RequestMsgUtil.getRouteConf(mercId, platform, tradeType, routeCode, null);
        String seq = seqIncrService.nextVal(SeqIncrType.OUT_TRADE_NO_SHEENPAY.getId(), SeqIncrType.OUT_TRADE_NO_SHEENPAY.getLength(), Align.LEFT);
        String outTradeNo = DateTimeUtil.date8() + seq;
        String bankMercId = routeConf.getBankMercId();
        String publicKey = routeConf.getPublicKey();
        //创建订单流水
        TradeOrderJoinpay joinpay = new TradeOrderJoinpay();
        joinpay.setOutTradeNo(outTradeNo);
        joinpay.setTradeDate(DateTimeUtil.date10());
        joinpay.setTradeTime(DateTimeUtil.time8());
        joinpay.setBankMercId(bankMercId);
        joinpay.setOrderStatus(OrderStatus.ADVANCE.getId());
        joinpay.setRouteCode(routeCode);
        joinpay.setTradeType(tradeType);
        joinpay.setPlatform(platform);
        joinpay.setPrice(price);
        joinpay.setTradeNo(tradeNo);
        joinpay.setOpenId(openId);
        joinpay.setBankCode(bankCode);
        joinpay.setAppId(routeConf.getAppId());
        iTradeOrderJoinpayService.save(joinpay);

        JoinPayReq dto = JoinPayReq.builder().p0_Version(version).p1_MerchantNo(bankMercId).build();
        dto.setP2_OrderNo(outTradeNo);
        dto.setP3_Amount(price);
        dto.setP4_Cur("1");
        dto.setP5_ProductName(PlatformType.parasByCode(platform).getName() + "商品");
        dto.setP9_NotifyUrl(one.getNotifyUrl());
        dto.setQ1_FrpCode(JoinpayUtil.queryFrpcode(bankCode, sysCnl));
        dto.setQ7_AppId(routeConf.getAppId());
        dto.setQ5_OpenId(openId);
        dto.setQa_TradeMerchantNo(tradeMerchantNo);
        Map<String, Object> map = BeanUtil.beanToMap(dto);
        String hmac = JoinpayUtil.createLinkString(map, publicKey);
        map.put("hmac", hmac);

        long l = System.currentTimeMillis();
        String plain = JoinpayUtil.getPlain(map);
        String s = payUrl + "?" + plain;
        log.info("|joinpay|unifiedOrder|outTradeNo：{}，url:{},request：{}", outTradeNo, s, JSONUtil.toJsonStr(map));
        String resp = restTemplate.postForObject(s, null, String.class);
        log.info("|joinpay|unifiedOrder|outTradeNo：{}，costTime:{},response：{}", outTradeNo, System.currentTimeMillis() - l, resp);


        JoinpayResp joinpayResp = JSONUtil.toBean(resp, JoinpayResp.class);
        String ra_code = joinpayResp.getRa_Code();
        if (!JoinpayConstants.RESPCODE_100.equalsIgnoreCase(ra_code)) {
            log.error("|joinpay|unifiedOrder|outTradeNo：{}，return_code：{}，return_msg：{}", outTradeNo, ra_code, joinpayResp.getRb_CodeMsg());
            throw new BusiException(11001, joinpayResp.getRb_CodeMsg());
        }

        //订单更新为W
        joinpay.setOrderStatus(OrderStatus.WAIT_PAY.getId());
        iTradeOrderJoinpayService.updateById(joinpay);

        //组装返回报文
        unifiedOrderResponseMsg(requestMsg, routeConf, joinpayResp, responseMsg);

        responseMsg.put("outTradeNo", outTradeNo);
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        log.info("|joinpay|unifiedOrder|结束：{}", JSON.toJSONString(responseMsg.getMap()));
    }

    @Override
    public ResponseMsg queryOrder(RequestMsg requestMsg) throws Exception {
        log.info("|joinpay|queryOrder|开始：{}", JSON.toJSONString(requestMsg.getMap()));
        ResponseMsg responseMsg = new ResponseMsg();
        String routeCode = (String) requestMsg.get("routeCode");
        String outTradeNo = (String) requestMsg.get("outTradeNo");
        String platform = (String) requestMsg.get("platform");
        String tradeType = (String) requestMsg.get("tradeType");
        String mercId = (String) requestMsg.get("mercId");

        RouteConf routeConf = RequestMsgUtil.getRouteConf(mercId, platform, tradeType, routeCode, null);
        String bankMercId = routeConf.getBankMercId();
        String publicKey = routeConf.getPublicKey();

        TradeOrderJoinpay joinpay = iTradeOrderJoinpayService.getOne(new QueryWrapper<TradeOrderJoinpay>().eq("out_trade_no", outTradeNo));
        if (null == joinpay || OrderStatus.FAIL.getId().equals(joinpay.getOrderStatus())) {
            responseMsg.put(ConstEC.RETURNCODE, "11001");
            responseMsg.put(ConstEC.RETURNMSG, ApplicationYmlUtil.get("11001"));
            return responseMsg;
        }

        JoinPayReq dto = JoinPayReq.builder().p0_Version(version).p1_MerchantNo(bankMercId).build();
        dto.setP2_OrderNo(outTradeNo);
        Map<String, Object> map = BeanUtil.beanToMap(dto);
        String hmac = JoinpayUtil.createLinkString(map, publicKey);
        map.put("hmac", hmac);
        long l = System.currentTimeMillis();
        String plain = JoinpayUtil.getPlain(map);
        String s = queryOrderUrl + "?" + plain;
        log.info("|joinpay|queryOrder|outTradeNo：{}，url:{},request：{}", outTradeNo, s, JSONUtil.toJsonStr(map));
        String resp = restTemplate.postForObject(s, null, String.class);
        log.info("|joinpay|queryOrder|outTradeNo：{}，costTime:{},response：{}", outTradeNo, System.currentTimeMillis() - l, resp);


        JoinpayResp joinpayResp = JSONUtil.toBean(resp, JoinpayResp.class);
        String ra_code = joinpayResp.getRa_Code();
        if (!JoinpayConstants.RESPCODE_100.equalsIgnoreCase(ra_code) && StrUtil.isNotBlank(ra_code)) {
            log.error("|joinpay|queryOrder|outTradeNo：{}，return_code：{}，return_msg：{}", outTradeNo, ra_code, joinpayResp.getRb_CodeMsg());
            throw new BusiException(11001, joinpayResp.getRb_CodeMsg());
        }

        //支付成功的处理
        if (JoinpayConstants.RESPCODE_100.equalsIgnoreCase(joinpayResp.getRa_Status())) {
            Map<String, Object> data = new HashMap<>();
            data.put("routeCode", RouteCode.JOINPAY.getId());
            data.put("payDate", DateTimeUtil.formatTimestamp2String(joinpayResp.getRf_PayTime(), "yyyy-MM-dd"));
            data.put("payTime", DateTimeUtil.formatTimestamp2String(joinpayResp.getRf_PayTime(), "HH:mm:ss"));
            data.put("outTradeNo", joinpayResp.getR2_OrderNo());
            data.put("bankTradeNo", joinpayResp.getR6_BankTrxNo());
            data.put("price", joinpayResp.getR3_Amount());
            data.put("openId", joinpayResp.getRd_OpenId());
            data.put("returnCode", "10000");
            data.put("returnMsg", "交易成功");
            DepositProcHandler handler = (DepositProcHandler) SpringContextHolder.getBean(routeCode.toLowerCase() + ConstEC.DEPOSITPROCHANDLER);
            handler.proc(data);
        }

        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        log.info("|joinpay|订单查询|结束：{}", JSON.toJSONString(responseMsg.getMap()));
        return responseMsg;
    }

    /**
     * 退款订单
     *
     * @param requestMsg
     * @return
     * @throws Exception
     */
    @Override
    public ResponseMsg refundOrder(RequestMsg requestMsg) throws Exception {
        log.info("|joinpay|refundOrder|开始：{}", JSON.toJSONString(requestMsg.getMap()));
        String routeCode = (String) requestMsg.get("routeCode");
        String mercId = (String) requestMsg.get("mercId");
        String platform = (String) requestMsg.get("platform");
        String tradeType = (String) requestMsg.get("tradeType");
        String outTradeNo = (String) requestMsg.get("outTradeNo");
        String refundNo = (String) requestMsg.get("refundNo");
        BigDecimal price = (BigDecimal) requestMsg.get("price");

        // 根据路由获取秘钥
        RouteConf routeConf = RequestMsgUtil.getRouteConf(mercId, platform, tradeType, routeCode, null);
        String bankMercId = routeConf.getBankMercId();
        String publicKey = routeConf.getPublicKey();

        TradeOrderJoinpay tradeOrderJoinpay = iTradeOrderJoinpayService.getOne(new QueryWrapper<TradeOrderJoinpay>().eq("out_trade_no", outTradeNo));
        if (!OrderStatus.SUCCESS.getId().equalsIgnoreCase(tradeOrderJoinpay.getOrderStatus())) {
            log.info("|joinpay|refundOrder|outTradeNo:{},失败：{}", outTradeNo, ApplicationYmlUtil.get(31120));
            throw new BusiException(31120);
        }
        TradeRefundJoinpay tradeRefundJoinpay = iTradeRefundJoinpayService.getOne(new QueryWrapper<TradeRefundJoinpay>().eq("refund_no", refundNo).last("limit 1"));
        if (tradeRefundJoinpay == null) {
            // 退款记录存库
            tradeRefundJoinpay = new TradeRefundJoinpay();
            BeanUtils.populate(tradeRefundJoinpay, requestMsg.getMap());
            String seq = seqIncrService.nextVal(SeqIncrType.OUT_REFUND_NO_SHEENPAY.getId(), 8, Align.LEFT);
            // 发往外部的退款订单号
            String outRefundNo = DateTimeUtil.date8() + seq;
            tradeRefundJoinpay.setRefundNo(refundNo);
            tradeRefundJoinpay.setOrderStatus(OrderStatus.REFUND.getId());
            tradeRefundJoinpay.setOutRefundNo(outRefundNo);
            tradeRefundJoinpay.setRefundDate(DateTimeUtil.date10());
            tradeRefundJoinpay.setRefundTime(DateTimeUtil.time8());
            iTradeRefundJoinpayService.save(tradeRefundJoinpay);
        }

        JoinpayRefundReq req = JoinpayRefundReq.builder().q1_version(refundVersion).p1_MerchantNo(bankMercId).build();
        req.setP2_OrderNo(outTradeNo);
        req.setP3_RefundOrderNo(tradeRefundJoinpay.getOutRefundNo());
        req.setP4_RefundAmount(price.toString());
        req.setP6_NotifyUrl(refundNotifyUrl);//成功或者失败时通知该地址
        Map<String, Object> map = BeanUtil.beanToMap(req);
        String hmac = JoinpayUtil.createLinkString(map, publicKey);
        map.put("hmac", hmac);

        long l = System.currentTimeMillis();
        String plain = JoinpayUtil.getPlain(map);
        String s = refundUrl + "?" + plain;
        log.info("|joinpay|refundOrder|outTradeNo：{}，url:{},request：{}", outTradeNo, s, JSONUtil.toJsonStr(map));
        String resp = restTemplate.postForObject(s, null, String.class);
        log.info("|joinpay|refundOrder|outTradeNo：{}，costTime:{},response：{}", outTradeNo, System.currentTimeMillis() - l, resp);
        JoinpayRefundResp joinpayRefundResp = JSONUtil.toBean(resp, JoinpayRefundResp.class);
        String returnCode = joinpayRefundResp.getRb_Code();
        String returnMsg = joinpayRefundResp.getRc_CodeMsg();

        //不是100 就是异常
        if (!JoinpayConstants.RESPCODE_100.equalsIgnoreCase(returnCode) && StrUtil.isNotBlank(returnCode)) {
            log.error("|joinpay|queryOrder|outTradeNo：{}，return_code：{}，return_msg：{}", outTradeNo, returnCode, returnMsg);
            throw new BusiException(11001, returnMsg);
        }


        ResponseMsg responseMsg = new ResponseMsg();
        switch (joinpayRefundResp.getRa_Status()) {
            case JoinpayConstants.RESPCODE_100:
                responseMsg = successHandler(joinpayRefundResp, tradeRefundJoinpay, refundNo);
                break;
            case JoinpayConstants.RESPCODE_101:
                responseMsg = failHandler(joinpayRefundResp, tradeRefundJoinpay);
                break;
        }
        log.info("|joinpay|refundOrder|结束：{}", JSON.toJSONString(responseMsg.getMap()));
        return responseMsg;
    }

    @Override
    public ResponseMsg queryRefundOrder(RequestMsg requestMsg) throws Exception {
        log.info("|joinpay|queryRefundOrder|开始，requestMsg：{}", JSON.toJSONString(requestMsg.getMap()));
        String routeCode = (String) requestMsg.get("routeCode");
        String mercId = (String) requestMsg.get("mercId");
        String platform = (String) requestMsg.get("platform");
        String tradeType = (String) requestMsg.get("tradeType");
        String outTradeNo = (String) requestMsg.get("outTradeNo");

        TradeRefundJoinpay tradeRefundJoinpay = iTradeRefundJoinpayService.getOne(new QueryWrapper<TradeRefundJoinpay>().eq("out_trade_no", outTradeNo));
        //成功就不再继续
        if (OrderStatus.SUCCESS.getId().equalsIgnoreCase(tradeRefundJoinpay.getOrderStatus())) {
            ResponseMsg msg = new ResponseMsg();
            msg.put("bankRefundNo", tradeRefundJoinpay.getBankRefundNo());
            msg.put("orderStatus", tradeRefundJoinpay.getOrderStatus());
            msg.put("refundChannel", tradeRefundJoinpay.getRefundChannel());
            msg.put("actualPrice", tradeRefundJoinpay.getActualPrice());
            msg.put("bankReturnDate", tradeRefundJoinpay.getBankReturnDate());
            msg.put("bankReturnTime", tradeRefundJoinpay.getBankReturnTime());
            msg.put("outRefundNo", tradeRefundJoinpay.getOutRefundNo());
            msg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
            msg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
            log.info("|joinpay|queryRefundOrder|完成：{}", JSON.toJSONString(msg));
            return msg;
        }

        RouteConf routeConf = RequestMsgUtil.getRouteConf(mercId, platform, tradeType, routeCode, null);
        JoinpayRefundReq req = JoinpayRefundReq.builder().p3_Version(refundVersion).p1_MerchantNo(routeConf.getBankMercId()).build();
        req.setP2_RefundOrderNo(tradeRefundJoinpay.getOutRefundNo());
        Map<String, Object> map = BeanUtil.beanToMap(req);
        String hmac = JoinpayUtil.createLinkString(map, routeConf.getPublicKey());
        map.put("hmac", hmac);

        long l = System.currentTimeMillis();
        String plain = JoinpayUtil.getPlain(map);
        String s = queryRefundUrl + "?" + plain;
        log.info("|joinpay|queryRefundOrder|outTradeNo：{}，url:{},request：{}", outTradeNo, s, JSONUtil.toJsonStr(map));
        String resp = restTemplate.postForObject(s, null, String.class);
        log.info("|joinpay|queryRefundOrder|outTradeNo：{}，costTime:{},response：{}", outTradeNo, System.currentTimeMillis() - l, resp);
        JoinpayRefundResp joinpayRefundResp = JSONUtil.toBean(resp, JoinpayRefundResp.class);
        String returnCode = joinpayRefundResp.getRb_Code();
        String returnMsg = joinpayRefundResp.getRc_CodeMsg();

        //不是100 就是异常
        if (!JoinpayConstants.RESPCODE_100.equalsIgnoreCase(returnCode) && StrUtil.isNotBlank(returnCode)) {
            log.error("|joinpay|queryRefundOrder|outTradeNo：{}，return_code：{}，return_msg：{}", outTradeNo, returnCode, returnMsg);
            throw new BusiException(11001, returnMsg);
        }

        ResponseMsg responseMsg = new ResponseMsg();
        switch (joinpayRefundResp.getRa_Status()) {
            case JoinpayConstants.RESPCODE_100:
                responseMsg = refundSuccessHandler(joinpayRefundResp, tradeRefundJoinpay);
                break;
            case JoinpayConstants.RESPCODE_101:
                responseMsg = failHandler(joinpayRefundResp, tradeRefundJoinpay);
                break;
            case JoinpayConstants.RESPCODE_102:
                responseMsg = commonHandler(tradeRefundJoinpay);
                break;
        }
        log.info("|joinpay|queryRefundOrder|结束：{}", JSON.toJSONString(responseMsg.getMap()));
        return responseMsg;
    }


    private void unifiedOrderResponseMsg(RequestMsg requestMsg, RouteConf routeConf, JoinpayResp joinpayResp, ResponseMsg responseMsg) throws Exception {
        String tradeType = (String) requestMsg.get("tradeType");

        Map<String, Object> dataMap = new HashMap<String, Object>();
        if (TradeType.JSAPI.getId().equals(tradeType)) {
            String rc_result = joinpayResp.getRc_Result();
            JSONObject jsonObject = JSONUtil.toBean(rc_result, JSONObject.class);
            //组装银行支付请求报文
            dataMap.put("appId", jsonObject.get("appId"));
            dataMap.put("signType", jsonObject.get("signType"));
            dataMap.put("package", jsonObject.get("package"));
            dataMap.put("nonceStr", jsonObject.get("nonceStr"));
            dataMap.put("timeStamp", jsonObject.get("timeStamp"));
            dataMap.put("sign", jsonObject.get("paySign"));
        }

        responseMsg.put(ConstEC.DATA, dataMap);
    }


    public ResponseMsg successHandler(JoinpayRefundResp joinpayRefundResp, TradeRefundJoinpay tradeRefundJoinpay, String refundNo) {
        ResponseMsg responseMsg = new ResponseMsg();
        String refundId = joinpayRefundResp.getR5_RefundTrxNo();
        BigDecimal actualPrice = joinpayRefundResp.getR4_RefundAmount();
        tradeRefundJoinpay.setReturnCode(joinpayRefundResp.getRb_Code());
        tradeRefundJoinpay.setReturnMsg(joinpayRefundResp.getRc_CodeMsg());
        tradeRefundJoinpay.setBankRefundNo(refundId);
        tradeRefundJoinpay.setActualPrice(actualPrice);
        tradeRefundJoinpay.setBankReturnDate(DateTimeUtil.date10());
        tradeRefundJoinpay.setBankReturnTime(DateTimeUtil.time8());
        tradeRefundJoinpay.setOrderStatus(OrderStatus.SUCCESS.getId());
        responseMsg.put("refundNo", refundNo);
        responseMsg.put("outRefundNo", joinpayRefundResp.getR3_RefundOrderNo());
        responseMsg.put("bankRefundNo", refundId);
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        //最后更新状态
        iTradeRefundJoinpayService.updateById(tradeRefundJoinpay);
        return responseMsg;
    }

    public ResponseMsg refundSuccessHandler(JoinpayRefundResp resp, TradeRefundJoinpay tradeRefundJoinpay) {
        ResponseMsg responseMsg = new ResponseMsg();
        String refundId = resp.getR4_RefundTrxNo();
        BigDecimal actualPrice = resp.getR3_RefundAmount();
        String time = resp.getR5_RefundCompleteTime();
        tradeRefundJoinpay.setReturnCode(resp.getRb_Code());
        tradeRefundJoinpay.setReturnMsg(resp.getRc_CodeMsg());
        tradeRefundJoinpay.setBankRefundNo(refundId);
        tradeRefundJoinpay.setActualPrice(actualPrice);
        tradeRefundJoinpay.setBankReturnDate(DateTimeUtil.formatDateStringToString(time, DateUtil.HOUR_PATTERN, DateUtil.DATE_PATTERN));
        tradeRefundJoinpay.setBankReturnTime(DateTimeUtil.formatDateStringToString(time, DateUtil.HOUR_PATTERN, DateUtil.TIME_PATTERN));
        tradeRefundJoinpay.setOrderStatus(OrderStatus.SUCCESS.getId());

        responseMsg.put("bankRefundNo", tradeRefundJoinpay.getBankRefundNo());
        responseMsg.put("orderStatus", tradeRefundJoinpay.getOrderStatus());
        responseMsg.put("refundChannel", tradeRefundJoinpay.getRefundChannel());
        responseMsg.put("actualPrice", tradeRefundJoinpay.getActualPrice());
        responseMsg.put("bankReturnDate", tradeRefundJoinpay.getBankReturnDate());
        responseMsg.put("bankReturnTime", tradeRefundJoinpay.getBankReturnTime());
        responseMsg.put("outRefundNo", tradeRefundJoinpay.getOutRefundNo());
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        //最后更新状态
        iTradeRefundJoinpayService.updateById(tradeRefundJoinpay);
        return responseMsg;
    }

    public ResponseMsg failHandler(JoinpayRefundResp joinpayRefundResp, TradeRefundJoinpay tradeRefundJoinpay) {
        String returnMsg = joinpayRefundResp.getRc_CodeMsg();
        ResponseMsg responseMsg = new ResponseMsg();
        tradeRefundJoinpay.setReturnCode("11001");
        tradeRefundJoinpay.setReturnMsg(returnMsg);
        tradeRefundJoinpay.setOrderStatus(OrderStatus.REFUND_WAIT.getId());
        iTradeRefundJoinpayService.updateById(tradeRefundJoinpay);
        responseMsg.put(ConstEC.RETURNCODE, 11001);
        responseMsg.put(ConstEC.RETURNMSG, returnMsg);
        //最后更新状态
        iTradeRefundJoinpayService.updateById(tradeRefundJoinpay);
        return responseMsg;
    }

    public ResponseMsg commonHandler(TradeRefundJoinpay tradeRefundJoinpay) {
        ResponseMsg responseMsg = new ResponseMsg();
        responseMsg.put("bankRefundNo", tradeRefundJoinpay.getBankRefundNo());
        responseMsg.put("orderStatus", tradeRefundJoinpay.getOrderStatus());
        responseMsg.put("refundChannel", tradeRefundJoinpay.getRefundChannel());
        responseMsg.put("actualPrice", tradeRefundJoinpay.getActualPrice());
        responseMsg.put("bankReturnDate", tradeRefundJoinpay.getBankReturnDate());
        responseMsg.put("bankReturnTime", tradeRefundJoinpay.getBankReturnTime());
        responseMsg.put("outRefundNo", tradeRefundJoinpay.getOutRefundNo());
        return responseMsg;
    }
}
