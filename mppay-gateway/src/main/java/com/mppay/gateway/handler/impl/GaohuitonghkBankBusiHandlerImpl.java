package com.mppay.gateway.handler.impl;

import cn.hutool.core.date.DateUnit;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.*;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.gaohuitong.GaohuitongConstants;
import com.mppay.core.sign.gaohuitong.GaohuitongMessgeUtil;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.core.utils.DateUtil;
import com.mppay.core.utils.HttpClientUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.dto.platform.gaohuitong.GHTPreAuthDTO;
import com.mppay.gateway.dto.platform.gaohuitong.GHTPreOrderDTO;
import com.mppay.gateway.dto.platform.gaohuitong.GHTRefundHKDTO;
import com.mppay.gateway.dto.platform.gaohuitong.GHThkNotifyDTO;
import com.mppay.gateway.handler.BankBusiHandler;
import com.mppay.gateway.handler.DepositProcHandler;
import com.mppay.gateway.handler.utils.RequestMsgUtil;
import com.mppay.service.entity.Route;
import com.mppay.service.entity.RouteConf;
import com.mppay.service.entity.TradeOrderGaohuitong;
import com.mppay.service.entity.TradeRefundGaohuitong;
import com.mppay.service.service.IRouteService;
import com.mppay.service.service.ISeqIncrService;
import com.mppay.service.service.ITradeOrderGaohuitongService;
import com.mppay.service.service.ITradeRefundGaohuitongService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.mppay.core.utils.StringUtil.getRandom;

@Service("gaohuitonghkBankBusiHandler")
@Slf4j
public class GaohuitonghkBankBusiHandlerImpl implements BankBusiHandler {

    @Value("${ght.xfhl.addressOverseaXfyinli}")
    private String addressOverseaXfyinli;
    @Value("${ght.xfhl.version}")
    private String version;
    @Value("${gaohuitong.overseas}")
    private String overseasUrl;

    @Autowired
    private IRouteService routeService;
    @Autowired
    private ISeqIncrService seqIncrService;
    @Autowired
    private ITradeOrderGaohuitongService iTradeOrderGaohuitongService;
    @Autowired
    private ITradeRefundGaohuitongService iTradeRefundGaohuitongService;

    @Override
    public void unifiedOrder(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        String tradeType = (String) requestMsg.get("tradeType");
        String orderExpTime = (String) requestMsg.get("orderExpTime");

        if (TradeType.JSAPI.getId().equalsIgnoreCase(tradeType)) {
            preAuth(requestMsg, responseMsg);
            return;
        }

        log.info("|高汇通HK|获取海外支付权限|开始，参数：{}", JSON.toJSONString(requestMsg.getMap()));
        String routeCode = (String) requestMsg.get("routeCode");
        String mercId = (String) requestMsg.get("mercId");
        String platform = (String) requestMsg.get("platform");
        String name = (String) requestMsg.get("userOper_name");
        String mercName = (String) requestMsg.get("mercName");
        BigDecimal price = (BigDecimal) requestMsg.get("price");
        String clientIp = (String) requestMsg.get("clientIp");
        String callbackUrl = (String) requestMsg.get("callbackUrl");
        String bankCode = (String) requestMsg.get("bankCode");
        String orderNo = (String) requestMsg.get("orderNo");

        Route route = routeService.getOne(new QueryWrapper<Route>().eq("route_code", routeCode));
        RouteConf routeConf = RequestMsgUtil.getRouteConf(mercId, platform, tradeType, routeCode, null);
        TradeOrderGaohuitong tradeOrderGaohuitong = new TradeOrderGaohuitong();
        String seq = seqIncrService.nextVal(SeqIncrType.OUT_TRADE_NO_WEIXIN.getId(), SeqIncrType.OUT_TRADE_NO_WEIXIN.getLength(), Align.LEFT);
        String outTradeNo = DateTimeUtil.date8() + seq;
        requestMsg.put("notifyUrl", route.getNotifyUrl());

        BeanUtils.populate(tradeOrderGaohuitong, requestMsg.getMap());
        tradeOrderGaohuitong.setOutTradeNo(outTradeNo);
        tradeOrderGaohuitong.setTradeDate(DateTimeUtil.date10());
        tradeOrderGaohuitong.setTradeTime(DateTimeUtil.time8());
        tradeOrderGaohuitong.setBankMercId(routeConf.getBankMercId());
        tradeOrderGaohuitong.setTerminalNo(routeConf.getAppId());
        tradeOrderGaohuitong.setOrderStatus(OrderStatus.ADVANCE.getId());
        //创建订单流水
        iTradeOrderGaohuitongService.save(tradeOrderGaohuitong);
        log.info("|高汇通HK|获取海外支付权限|外部订单入库 outTradeNo：{}", outTradeNo);

        GHTPreOrderDTO dto = new GHTPreOrderDTO();
        dto.setVersion(version);
        dto.setTrade_code(GaohuitongConstants.GHTHK_PAY);
        dto.setAgencyId(routeConf.getBankMercId());
        dto.setTerminal_no(routeConf.getAppId());
        dto.setOrder_no(outTradeNo);
        dto.setAmount(price.toString());
        dto.setCurrency_type("CNY");
        dto.setSett_currency_type("CNY");
        dto.setProduct_name(mercName + "商品");
        dto.setUser_name(name);
        dto.setNotify_url(route.getNotifyUrl());
        dto.setClient_ip(clientIp);
        dto.setPay_timeout("3600"); //现因为用户经常操作太久，故设置超时1小时

        if (!StringUtils.isBlank(bankCode)) {
            if (bankCode.equalsIgnoreCase(BankCode.UPOP.getId())) {
                dto.setUser_bank_card_no(requestMsg.get("openId").toString());
            }

            if (bankCode.equalsIgnoreCase(BankCode.ALIPAY.getId()) && tradeType.equalsIgnoreCase(TradeType.MWEB.getId())) {
                dto.setBank_code("WAPALIPAY");
            }

        }
        if (!StringUtils.isBlank(callbackUrl)) {
            callbackUrl += "?orderNo=" + orderNo;
            dto.setReturn_url(callbackUrl);
        }

        String s = sendRequest(dto, GaohuitongConstants.GHTHK_PAY, overseasUrl, routeConf, null);
        GHTPreOrderDTO ghtPreOrderDTO = JSON.parseObject(s, GHTPreOrderDTO.class);
        if (!"0000".equalsIgnoreCase(ghtPreOrderDTO.getResp_code())) {
            log.info("|高汇通HK|获取海外支付权限|失败：{}", ghtPreOrderDTO.getResp_desc());
            throw new BusiException(31119);
        }
        Map<String, Object> map = new HashMap<>();
        if (TradeType.APP.getId().equalsIgnoreCase(tradeType)) {
            map.put("tokenId", ghtPreOrderDTO.getToken_id());
            map.put("routeCode", routeCode);
            map.put("bankCode", BankCode.WEIXIN.getId().equalsIgnoreCase(bankCode) ? "APPWECHAT" : "APPALIPAY");
        } else if (TradeType.MWEB.getId().equalsIgnoreCase(tradeType)) {
            if (BankCode.ALIPAY.getId().equalsIgnoreCase(bankCode)) {
                map.put("mweb_url", ghtPreOrderDTO.getMweb_url());
                map.put("callbackUrl", callbackUrl);
            } else if (BankCode.UPOP.getId().equalsIgnoreCase(bankCode)) {
                map.put("tokenId", ghtPreOrderDTO.getToken_id());
                map.put("version", version);
                map.put("bank_code", "UPOP");
                map.put("access_type", "0");
            }

        }
        //更新状态为W
        tradeOrderGaohuitong.setOrderStatus(OrderStatus.WAIT_PAY.getId());
        iTradeOrderGaohuitongService.updateById(tradeOrderGaohuitong);

        responseMsg.put(ConstEC.DATA, map);
        responseMsg.put("outTradeNo", outTradeNo);
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        log.info("|高汇通HK|获取海外支付权限|完成：{}", JSON.toJSONString(responseMsg.getMap()));
    }


    @Override
    public ResponseMsg queryOrder(RequestMsg requestMsg) throws Exception {
        log.info("|高汇通HK|订单查询|开始，参数：{}", JSON.toJSONString(requestMsg));
        ResponseMsg responseMsg = new ResponseMsg();
        String routeCode = (String) requestMsg.get("routeCode");
        String mercId = (String) requestMsg.get("mercId");
        String platform = (String) requestMsg.get("platform");
        String tradeType = (String) requestMsg.get("tradeType");

        TradeOrderGaohuitong tradeOrderWeixin = iTradeOrderGaohuitongService.getOne(new QueryWrapper<TradeOrderGaohuitong>().eq("out_trade_no", requestMsg.get("outTradeNo")));

        if (null == tradeOrderWeixin || OrderStatus.FAIL.getId().equals(tradeOrderWeixin.getOrderStatus())) {
            responseMsg.put(ConstEC.RETURNCODE, "11001");
            responseMsg.put(ConstEC.RETURNMSG, ApplicationYmlUtil.get("11001"));
            return responseMsg;
        }
        RouteConf routeConf = RequestMsgUtil.getRouteConf(mercId, platform, tradeType, routeCode, null);
        GHTPreOrderDTO dto = new GHTPreOrderDTO();
        dto.setVersion(version);
        dto.setTrade_code(GaohuitongConstants.GHTHK_SEARCH);
        dto.setAgencyId(routeConf.getBankMercId());
        dto.setTerminal_no(routeConf.getAppId());
        dto.setOrder_no(tradeOrderWeixin.getOutTradeNo());

        String s = sendRequest(dto, GaohuitongConstants.GHTHK_SEARCH, overseasUrl, routeConf, null);
        GHThkNotifyDTO ghtPreOrderDTO = JSON.parseObject(s, GHThkNotifyDTO.class);
        if (!"0000".equalsIgnoreCase(ghtPreOrderDTO.getResp_code())) {
            log.info("|高汇通HK|订单查询|失败：{}", ghtPreOrderDTO.getResp_desc());
            throw new BusiException(31119);
        }
        //支付成功的处理
        if ("1".equalsIgnoreCase(ghtPreOrderDTO.getPay_result())) {
            Date payTime = DateTimeUtil.formatStringToDate(ghtPreOrderDTO.getPay_time(), "yyyyMMddHHmmss");
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("routeCode", RouteCode.GAOHUITONGHK.getId());
            data.put("payDate", DateTimeUtil.formatTimestamp2String(payTime, "yyyy-MM-dd"));
            data.put("payTime", DateTimeUtil.formatTimestamp2String(payTime, "HH:mm:ss"));
            data.put("outTradeNo", ghtPreOrderDTO.getOrder_no());
            data.put("bankTradeNo", ghtPreOrderDTO.getPay_no());
            data.put("price", ghtPreOrderDTO.getAmount());
            data.put("fundBank", ghtPreOrderDTO.getBank_code());
            if (!StringUtils.isBlank(ghtPreOrderDTO.getUser_bank_card_no())) {
            	data.put("openId", ghtPreOrderDTO.getUser_bank_card_no());
            }
            
            data.put("returnCode", "10000");
            data.put("returnMsg", "交易成功");
            DepositProcHandler handler = (DepositProcHandler) SpringContextHolder.getBean(routeCode.toLowerCase() + ConstEC.DEPOSITPROCHANDLER);
            handler.proc(data);
        }

        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        log.info("|高汇通HK|订单查询|完成：{}", JSON.toJSONString(responseMsg));
        return responseMsg;
    }

    @Override
    public ResponseMsg refundOrder(RequestMsg requestMsg) throws Exception {
        log.info("|高汇通HK|退款|开始，参数：{}", JSON.toJSONString(requestMsg.getMap()));
        ResponseMsg responseMsg = new ResponseMsg();
        String routeCode = (String) requestMsg.get("routeCode");
        String mercId = (String) requestMsg.get("mercId");
        String platform = (String) requestMsg.get("platform");
        String tradeType = (String) requestMsg.get("tradeType");
        String outTradeNo = (String) requestMsg.get("outTradeNo");
        String refundNo = (String) requestMsg.get("refundNo");

        RouteConf routeConf = RequestMsgUtil.getRouteConf(mercId, platform, tradeType, routeCode, null);
        TradeOrderGaohuitong tradeOrderGaohuitong = iTradeOrderGaohuitongService.getOne(new QueryWrapper<TradeOrderGaohuitong>().eq("out_trade_no", outTradeNo));
        //没有支付成功就不能退款
        if (!OrderStatus.SUCCESS.getId().equalsIgnoreCase(tradeOrderGaohuitong.getOrderStatus())) {
            log.info("|高汇通HK|退款|outTradeNo:{},失败：{}",outTradeNo, ApplicationYmlUtil.get(31120));
            throw new BusiException(31120);
        }
        TradeRefundGaohuitong gaohuitong = iTradeRefundGaohuitongService.getOne(new QueryWrapper<TradeRefundGaohuitong>().eq("refund_no", refundNo).last("limit 1"));
        if (gaohuitong == null) {
            //退款记录存库
            gaohuitong = new TradeRefundGaohuitong();
            BeanUtils.populate(gaohuitong, requestMsg.getMap());
            String seq = seqIncrService.nextVal(SeqIncrType.OUT_REFUND_NO_GAOHUITONG.getId(), 8, Align.LEFT);
            String outRefundNo = DateTimeUtil.date8() + seq; //发往外部的退款订单号
            gaohuitong.setOrderStatus(OrderStatus.REFUND.getId());
            gaohuitong.setOutRefundNo(outRefundNo);
            gaohuitong.setRefundDate(DateTimeUtil.date10());
            gaohuitong.setRefundTime(DateTimeUtil.time8());
            iTradeRefundGaohuitongService.save(gaohuitong);
        }

        //当天订单（23:00之前）使用repealPay接口
        String payDate = tradeOrderGaohuitong.getPayDate();
        if (payDate.equalsIgnoreCase(DateUtil.dateFormat(new Date(), DateUtil.DATE_PATTERN))) {
            String payTime = tradeOrderGaohuitong.getPayTime();
            Calendar calendar = Calendar.getInstance();
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 23, 00, 00);
            int i1 = DateUtil.dateCompare(calendar.getTime(), DateUtil.dateParse(payDate + " " + payTime, DateUtil.DATE_TIME_PATTERN));
            if (i1 == 1) {
                return repealPay(requestMsg, routeConf, gaohuitong, tradeOrderGaohuitong);
            }
        }
        return directRefund(requestMsg, routeConf, gaohuitong, tradeOrderGaohuitong);
    }

    //退款
    private ResponseMsg directRefund(RequestMsg requestMsg, RouteConf routeConf, TradeRefundGaohuitong gaohuitong, TradeOrderGaohuitong tradeOrderGaohuitong) throws Exception {
        log.info("|高汇通HK|快捷|退款|开始，参数：{}", JSON.toJSONString(requestMsg.getMap()));
        ResponseMsg responseMsg = new ResponseMsg();
        String refundNo = (String) requestMsg.get("refundNo");
        String outRefundNo = gaohuitong.getOutRefundNo();
        GHTRefundHKDTO dto = new GHTRefundHKDTO();
        dto.setVersion(version);
        dto.setTrade_code(GaohuitongConstants.GHTHK_REFUND);
        dto.setAgencyId(routeConf.getBankMercId());
        dto.setChild_merchant_no(routeConf.getBankMercId());
        dto.setTerminal_no(routeConf.getAppId());
        dto.setOrder_no(tradeOrderGaohuitong.getOutTradeNo());
        dto.setRefund_no(outRefundNo);
        dto.setRefund_amount(tradeOrderGaohuitong.getPrice().toString());
        dto.setCurrency_type("CNY");
        dto.setSett_currency_type("CNY");
        String s = sendRequest(dto, GaohuitongConstants.GHTHK_REFUND, overseasUrl, routeConf, null);
        GHTRefundHKDTO ghtPreOrderDTO = JSON.parseObject(s, GHTRefundHKDTO.class);
        log.info("|高汇通HK|退款|outTradeNo:{},outRefundNo:{},result :{}", gaohuitong.getOutTradeNo(), outRefundNo, s);
        if (!"0000".equalsIgnoreCase(ghtPreOrderDTO.getResp_code())) {
            log.info("|高汇通HK|退款|faild,outTradeNo:{},outRefundNo:{},refund_result：{},resp_desc:{}", gaohuitong.getOutTradeNo(), outRefundNo, ghtPreOrderDTO.getRefund_result(), ghtPreOrderDTO.getResp_desc());
            throw new BusiException(ghtPreOrderDTO.getResp_code(), ghtPreOrderDTO.getResp_desc());
        }

        gaohuitong.setBankRefundNo(ghtPreOrderDTO.getRefund_id()); //外部退款单号
        String refund_time = ghtPreOrderDTO.getRefund_time();
        String refundDate = DateUtil.dateFormat(DateUtil.dateParse(refund_time, DateUtil.DATEFORMAT_9), DateUtil.DATE_PATTERN);
        String refundTime = DateUtil.dateFormat(DateUtil.dateParse(refund_time, DateUtil.DATEFORMAT_9), DateUtil.TIME_PATTERN);
        gaohuitong.setBankReturnDate(refundDate);
        gaohuitong.setBankReturnTime(refundTime);
        //不是1 的都是失败
        if (!"1".equalsIgnoreCase(ghtPreOrderDTO.getRefund_result())) {
            log.info("|高汇通HK|退款|faild,outTradeNo:{},outRefundNo:{},refund_result：{},resp_desc:{}", gaohuitong.getOutTradeNo(), gaohuitong.getOutRefundNo(), ghtPreOrderDTO.getRefund_result(), ghtPreOrderDTO.getResp_desc());
            gaohuitong.setOrderStatus(OrderStatus.REFUND_WAIT.getId());
            gaohuitong.setReturnCode(ghtPreOrderDTO.getResp_code());
            gaohuitong.setReturnMsg("refund_result-" + ghtPreOrderDTO.getRefund_result());
            responseMsg.put(ConstEC.RETURNCODE, "11001");
            responseMsg.put(ConstEC.RETURNMSG, ApplicationYmlUtil.get("11001"));
        } else {
            log.info("|高汇通HK|退款|success ,outTradeNo:{}, outRefundNo:{},refund_result：{},resp_desc:{}", gaohuitong.getOutTradeNo(), gaohuitong.getOutRefundNo(), ghtPreOrderDTO.getRefund_result(), ghtPreOrderDTO.getResp_desc());
            gaohuitong.setOrderStatus(OrderStatus.SUCCESS.getId());
            gaohuitong.setReturnCode(ghtPreOrderDTO.getResp_code());
            gaohuitong.setReturnMsg(ghtPreOrderDTO.getResp_desc());
            responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
            responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        }
        iTradeRefundGaohuitongService.updateById(gaohuitong);

        responseMsg.put("refundNo", refundNo);
        responseMsg.put("outRefundNo", outRefundNo);
        responseMsg.put("bankRefundNo", gaohuitong.getBankRefundNo());
        log.info("|高汇通HK|退款|finish outTradeNo:{}, outRefundNo:{}，responseMsg：{}", gaohuitong.getOutTradeNo(), gaohuitong.getOutRefundNo(), JSON.toJSONString(responseMsg));
        return responseMsg;
    }

    @Override
    public ResponseMsg queryRefundOrder(RequestMsg requestMsg) throws Exception {
        log.info("|高汇通HK|退款查询|开始，参数：{}", JSON.toJSONString(requestMsg.getMap()));
        ResponseMsg responseMsg = new ResponseMsg();
        String routeCode = (String) requestMsg.get("routeCode");
        String mercId = (String) requestMsg.get("mercId");
        String platform = (String) requestMsg.get("platform");
        String tradeType = (String) requestMsg.get("tradeType");
        String outTradeNo = (String) requestMsg.get("outTradeNo");

        TradeRefundGaohuitong gaohuitong = iTradeRefundGaohuitongService.getOne(new QueryWrapper<TradeRefundGaohuitong>().eq("out_trade_no", outTradeNo));
        //成功就不再继续
        if (OrderStatus.SUCCESS.getId().equalsIgnoreCase(gaohuitong.getOrderStatus())) {
            responseMsg.put("bankRefundNo", gaohuitong.getBankRefundNo());
            responseMsg.put("orderStatus", gaohuitong.getOrderStatus());
            responseMsg.put("refundChannel", gaohuitong.getRefundChannel());
            responseMsg.put("actualPrice", gaohuitong.getActualPrice());
            responseMsg.put("bankReturnDate", gaohuitong.getBankReturnDate());
            responseMsg.put("bankReturnTime", gaohuitong.getBankReturnTime());
            responseMsg.put("outRefundNo", gaohuitong.getOutRefundNo());
            responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
            responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
            log.info("|高汇通HK|退款查询|完成：{}", JSON.toJSONString(responseMsg));
            return responseMsg;
        }

        String outRefundNo = gaohuitong.getOutRefundNo();
        RouteConf routeConf = RequestMsgUtil.getRouteConf(mercId, platform, tradeType, routeCode, null);
        GHTRefundHKDTO dto = new GHTRefundHKDTO();
        dto.setVersion(version);
        dto.setTrade_code(GaohuitongConstants.GHTHK_SEARCH_REFUND);
        dto.setAgencyId(routeConf.getBankMercId());
        dto.setTerminal_no(routeConf.getAppId());
        dto.setRefund_no(outRefundNo);

        String s = sendRequest(dto, GaohuitongConstants.GHTHK_SEARCH_REFUND, overseasUrl, routeConf, null);
        log.info("|高汇通HK|退款查询|outTradeNo:{},outRefundNo:{},result :{}", gaohuitong.getOutTradeNo(), outRefundNo, s);
        GHTRefundHKDTO ghtPreOrderDTO = JSON.parseObject(s, GHTRefundHKDTO.class);
        if (!"0000".equalsIgnoreCase(ghtPreOrderDTO.getResp_code())) {
            log.info("|高汇通HK|退款查询|faild,outTradeNo:{},outRefundNo:{},refund_result：{},resp_desc:{}", outTradeNo, outRefundNo, ghtPreOrderDTO.getRefund_result(), ghtPreOrderDTO.getResp_desc());
            throw new BusiException(ghtPreOrderDTO.getResp_code(), ghtPreOrderDTO.getResp_desc());
        }

        gaohuitong.setActualPrice(new BigDecimal(ghtPreOrderDTO.getRefund_total_amount())); //实际的退款金额
        gaohuitong.setBankRefundNo(ghtPreOrderDTO.getRefund_id()); //外部退款单号
        String refund_time = ghtPreOrderDTO.getRefund_time();
        String refundDate = DateUtil.dateFormat(DateUtil.dateParse(refund_time, DateUtil.DATEFORMAT_9), DateUtil.DATE_PATTERN);
        String refundTime = DateUtil.dateFormat(DateUtil.dateParse(refund_time, DateUtil.DATEFORMAT_9), DateUtil.TIME_PATTERN);
        gaohuitong.setBankReturnDate(refundDate);
        gaohuitong.setBankReturnTime(refundTime);
        //不是1 的都是失败
        if (!"1".equalsIgnoreCase(ghtPreOrderDTO.getRefund_result())) {
            log.info("|高汇通HK|退款查询|faild,outTradeNo:{},outRefundNo:{},refund_result：{},resp_desc:{}", gaohuitong.getOutTradeNo(), outRefundNo, ghtPreOrderDTO.getRefund_result(), ghtPreOrderDTO.getResp_desc());
            gaohuitong.setOrderStatus(OrderStatus.REFUND_WAIT.getId());
            gaohuitong.setReturnCode(ghtPreOrderDTO.getResp_code());
            gaohuitong.setReturnMsg("refund_result-" + ghtPreOrderDTO.getRefund_result());
            responseMsg.put(ConstEC.RETURNCODE, "11001");
            responseMsg.put(ConstEC.RETURNMSG, ApplicationYmlUtil.get("11001"));
        } else {
            log.info("|高汇通HK|退款查询|success ,outTradeNo:{}, outRefundNo:{},refund_result：{},resp_desc:{}", gaohuitong.getOutTradeNo(), outRefundNo, ghtPreOrderDTO.getRefund_result(), ghtPreOrderDTO.getResp_desc());
            gaohuitong.setOrderStatus(OrderStatus.SUCCESS.getId());
            gaohuitong.setReturnCode(ghtPreOrderDTO.getResp_code());
            gaohuitong.setReturnMsg(ghtPreOrderDTO.getResp_desc());
            responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
            responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        }
        iTradeRefundGaohuitongService.updateById(gaohuitong);

        responseMsg.put("bankRefundNo", gaohuitong.getBankRefundNo());
        responseMsg.put("orderStatus", gaohuitong.getOrderStatus());
        responseMsg.put("refundChannel", gaohuitong.getRefundChannel());
        responseMsg.put("actualPrice", gaohuitong.getActualPrice());
        responseMsg.put("bankReturnDate", gaohuitong.getBankReturnDate());
        responseMsg.put("bankReturnTime", gaohuitong.getBankReturnTime());
        responseMsg.put("outRefundNo", gaohuitong.getOutRefundNo());
        log.info("|高汇通HK|退款查询|finish outTradeNo:{}, outRefundNo:{}，responseMsg：{}", gaohuitong.getOutTradeNo(), outRefundNo, JSON.toJSONString(responseMsg));
        return responseMsg;
    }


    public void preAuth(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        log.info("|高汇通HK|支付请求(预授权)|开始，参数：{}", JSON.toJSONString(requestMsg.getMap()));
        String routeCode = (String) requestMsg.get("routeCode");
        String tradeType = (String) requestMsg.get("tradeType");
        String mercId = (String) requestMsg.get("mercId");
        String platform = (String) requestMsg.get("platform");
        String mercName = (String) requestMsg.get("mercName");
        BigDecimal price = (BigDecimal) requestMsg.get("price");
        String clientIp = (String) requestMsg.get("clientIp");
        String openId = (String) requestMsg.get("openId");
        String periodUnit = (String) requestMsg.get("periodUnit");
        Integer period = (Integer) requestMsg.get("period");


        Route route = routeService.getOne(new QueryWrapper<Route>().eq("route_code", routeCode));
        RouteConf routeConf = RequestMsgUtil.getRouteConf(mercId, platform, tradeType, routeCode, null);
        TradeOrderGaohuitong tradeOrderGaohuitong = new TradeOrderGaohuitong();
        String seq = seqIncrService.nextVal(SeqIncrType.OUT_TRADE_NO_WEIXIN.getId(), SeqIncrType.OUT_TRADE_NO_WEIXIN.getLength(), Align.LEFT);
        String outTradeNo = DateTimeUtil.date8() + seq;
        requestMsg.put("notifyUrl", route.getNotifyUrl());

        BeanUtils.populate(tradeOrderGaohuitong, requestMsg.getMap());

        tradeOrderGaohuitong.setOutTradeNo(outTradeNo);
        tradeOrderGaohuitong.setTradeDate(DateTimeUtil.date10());
        tradeOrderGaohuitong.setTradeTime(DateTimeUtil.time8());
        tradeOrderGaohuitong.setBankMercId(routeConf.getBankMercId());
        tradeOrderGaohuitong.setTerminalNo(routeConf.getAppId());
        tradeOrderGaohuitong.setOrderStatus(OrderStatus.ADVANCE.getId());
        //创建订单流水
        iTradeOrderGaohuitongService.save(tradeOrderGaohuitong);
        log.info("|高汇通HK|支付请求(预授权)|外部订单入库 outTradeNo：{}", outTradeNo);
        GHTPreAuthDTO dto = new GHTPreAuthDTO();
        dto.setVersion(version);
        dto.setTrade_code(GaohuitongConstants.GHTHK_PAY);
        dto.setAgencyId(routeConf.getBankMercId());
        dto.setTerminal_no(routeConf.getAppId());
        dto.setOrder_no(outTradeNo);
        dto.setAmount(price.toString());
        dto.setCurrency_type("CNY");
        dto.setSett_currency_type("CNY");
        dto.setProduct_name(mercName + "商品");
        dto.setNotify_url(route.getNotifyUrl());
        if (TradeType.JSAPI.getId().equalsIgnoreCase(tradeType)) {
            dto.setUser_bank_card_no(openId);
            dto.setBank_code("PUBLICWECHAT");
        }
        dto.setClient_ip(clientIp);
        dto.setPay_timeout("3600");

        String s = sendRequest(dto, GaohuitongConstants.GHTHK_PAY, overseasUrl, routeConf, null);
        GHTPreAuthDTO ghtPreOrderDTO = JSON.parseObject(s, GHTPreAuthDTO.class);
        if (!"0000".equalsIgnoreCase(ghtPreOrderDTO.getResp_code())) {
            log.info("|高汇通HK|支付请求(预授权)|失败：{}", ghtPreOrderDTO.getResp_desc());
            throw new BusiException(ghtPreOrderDTO.getResp_code(), ghtPreOrderDTO.getResp_desc());
        }

        //更新状态为W
        tradeOrderGaohuitong.setOrderStatus(OrderStatus.WAIT_PAY.getId());
        iTradeOrderGaohuitongService.updateById(tradeOrderGaohuitong);

        Map<String, Object> map = new HashMap<>();
        String jsPrepayInfo = ghtPreOrderDTO.getJs_prepay_info();
        JSONObject jsonObject = JSONObject.parseObject(jsPrepayInfo);
        map.put("appId", jsonObject.get("appId"));
        map.put("package", jsonObject.get("package"));
        map.put("nonceStr", jsonObject.get("nonceStr"));
        map.put("timeStamp", jsonObject.get("timeStamp"));
        map.put("signType", jsonObject.get("signType"));
        map.put("sign", jsonObject.get("paySign"));

        responseMsg.put(ConstEC.DATA, map);
        responseMsg.put("outTradeNo", outTradeNo);
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        log.info("|高汇通HK|支付请求(预授权)|完成：{}", JSON.toJSONString(responseMsg.getMap()));
    }

    /**
     * @param :[requestMsg]
     * @return :com.mppay.gateway.dto.ResponseMsg
     * @Description(描述): 支队撤销
     * @auther: Jack Lin
     * @date: 2019/12/10 15:12
     */
    public ResponseMsg repealPay(RequestMsg requestMsg, RouteConf routeConf, TradeRefundGaohuitong gaohuitong, TradeOrderGaohuitong tradeOrderGaohuitong) throws Exception {
        log.info("|高汇通HK|repealPay|开始，参数：{}", JSON.toJSONString(requestMsg.getMap()));
        ResponseMsg responseMsg = new ResponseMsg();
        String outTradeNo = (String) requestMsg.get("outTradeNo");
        String outRefundNo = gaohuitong.getOutRefundNo();
        GHTRefundHKDTO dto = new GHTRefundHKDTO();
        dto.setVersion(version);
        dto.setTrade_code(GaohuitongConstants.GHTHK_PAYC); //repealPay
        dto.setAgencyId(routeConf.getBankMercId());
        dto.setChild_merchant_no(routeConf.getBankMercId());
        dto.setTerminal_no(routeConf.getAppId());
        dto.setOrder_no(outRefundNo); //发往外部的退款单号
        dto.setOri_order_no(outTradeNo); //原外部交易订单号

        String s = sendRequest(dto, GaohuitongConstants.GHTHK_PAYC, overseasUrl, routeConf, null);
        log.info("|高汇通HK|repealPay|outTradeNo:{},outRefundNo:{},result :{}", outTradeNo, outRefundNo, s);
        GHTRefundHKDTO ghtPreOrderDTO = JSON.parseObject(s, GHTRefundHKDTO.class);
        if (!"0000".equalsIgnoreCase(ghtPreOrderDTO.getResp_code())) {
            log.info("|高汇通HK|repealPay|faild,outTradeNo:{},outRefundNo:{},refund_result：{},resp_desc:{}", outTradeNo, outRefundNo, ghtPreOrderDTO.getRefund_result(), ghtPreOrderDTO.getResp_desc());
            throw new BusiException(ghtPreOrderDTO.getResp_code(), ghtPreOrderDTO.getResp_desc());
        }

        String refund_time = ghtPreOrderDTO.getPay_time();
        String refundDate = DateUtil.dateFormat(DateUtil.dateParse(refund_time, DateUtil.DATEFORMAT_9), DateUtil.DATE_PATTERN);
        String refundTime = DateUtil.dateFormat(DateUtil.dateParse(refund_time, DateUtil.DATEFORMAT_9), DateUtil.TIME_PATTERN);
        gaohuitong.setBankReturnDate(refundDate);
        gaohuitong.setBankReturnTime(refundTime);
       /* //不是1 的都是失败
        if (!"1".equalsIgnoreCase(ghtPreOrderDTO.getPay_result())) {
            log.info("|高汇通HK|repealPay|faild,outTradeNo:{},outRefundNo:{},refund_result：{},resp_desc:{}", outTradeNo, outRefundNo, ghtPreOrderDTO.getRefund_result(), ghtPreOrderDTO.getResp_desc());
            gaohuitong.setOrderStatus(OrderStatus.REFUND_WAIT.getId());
            gaohuitong.setReturnCode(ghtPreOrderDTO.getResp_code());
            gaohuitong.setReturnMsg("repealPay_result-" + ghtPreOrderDTO.getPay_result());
            responseMsg.put(ConstEC.RETURNCODE, "11001");
            responseMsg.put(ConstEC.RETURNMSG, ApplicationYmlUtil.get("11001"));
        } else {*/
        log.info("|高汇通HK|repealPay|success ,outTradeNo:{}, outRefundNo:{},refund_result：{},resp_desc:{}", outTradeNo, outRefundNo, ghtPreOrderDTO.getRefund_result(), ghtPreOrderDTO.getResp_desc());
        gaohuitong.setOrderStatus(OrderStatus.SUCCESS.getId());
        gaohuitong.setReturnCode(ghtPreOrderDTO.getResp_code());
        gaohuitong.setReturnMsg(ghtPreOrderDTO.getResp_desc());
        gaohuitong.setRefundType(RefundType.REPEAL.getId());
        gaohuitong.setBankRefundNo(ghtPreOrderDTO.getPay_no());
        gaohuitong.setActualPrice(gaohuitong.getApplyPrice());
        iTradeRefundGaohuitongService.updateById(gaohuitong);

        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        responseMsg.put("refundNo", requestMsg.get("refundNo"));
        responseMsg.put("outRefundNo", outRefundNo);
        responseMsg.put("bankRefundNo", ghtPreOrderDTO.getPay_no());
        log.info("|高汇通HK|repealPay|finish outTradeNo:{}, outRefundNo:{}，responseMsg：{}", gaohuitong.getOutTradeNo(), gaohuitong.getOutRefundNo(), JSON.toJSONString(responseMsg));
        return responseMsg;
    }


    /**
     * @Description(描述): 统一处理请求
     * @auther: Jack Lin
     * @date: 2019/9/7 17:31
     */
    public String sendRequest(Object obj, String tranCode, String url, RouteConf routeConf, String callbackUrl) throws Exception {
        String reqMsg = JSON.toJSONString(obj);
        //AES key
        String keyStr = getRandom(16);
        PublicKey publicKey = GaohuitongMessgeUtil.getPublicKey(routeConf.getPublicKeyPath());
        PrivateKey privateKey = GaohuitongMessgeUtil.getPrivateKey(routeConf.getKeyPath());
        //组装请求参数
        Map<String, Object> msgMap = GaohuitongMessgeUtil.requestHandle(routeConf.getBankMercId(), reqMsg, keyStr, publicKey, privateKey, null, callbackUrl);
        String s1 = addressOverseaXfyinli + url;
        long l = System.currentTimeMillis();
        log.info(" call_ght： url：{}，request:{}", s1, JSON.toJSONString(msgMap));
        log.info("call_ght： tranCode：{}， json：{}", tranCode, reqMsg);
        String response = HttpClientUtil.sendPostJson(s1, msgMap, null);//json方式提交参数
        if (StringUtils.isEmpty(response)) {
            throw new BusiException(13110);
        }
        log.info("call_ght：解密前，url：{}，costTime：{}ms，response:{}", s1, System.currentTimeMillis() - l, response);
        //解析响应
        Map map = JSON.parseObject(response, Map.class);
        String s = GaohuitongMessgeUtil.responseHandle(map, keyStr, publicKey, privateKey);

        log.info("call_ght：解密后，url：{}，costTime：{}ms，response:{}", s1, System.currentTimeMillis() - l, s);
        if (StringUtils.isEmpty(s)) {
            throw new BusiException(13110);
        }
        return s;
    }


}
