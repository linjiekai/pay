package com.mppay.gateway.handler.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.*;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.Sign;
import com.mppay.core.constant.sheenpay.SheenPayConstants;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.dto.platform.sheen.SheenRefundDTO;
import com.mppay.gateway.dto.platform.sheen.SheenRefundQueryDTO;
import com.mppay.gateway.dto.platform.sheen.SheenReq;
import com.mppay.gateway.dto.platform.sheen.SheenResp;
import com.mppay.gateway.handler.BankBusiHandler;
import com.mppay.gateway.handler.DepositProcHandler;
import com.mppay.gateway.handler.utils.RequestMsgUtil;
import com.mppay.service.entity.Route;
import com.mppay.service.entity.RouteConf;
import com.mppay.service.entity.TradeOrderSheenPay;
import com.mppay.service.entity.TradeRefundSheenpay;
import com.mppay.service.service.IRouteService;
import com.mppay.service.service.ISeqIncrService;
import com.mppay.service.service.ITradeOrderSheenPayService;
import com.mppay.service.service.ITradeRefundSheenpayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service("sheenpayhkBankBusiHandler")
@Slf4j
public class SheenpayhkBankBusiHandlerImpl implements BankBusiHandler {
    @Value("${sheenpay.host}")
    private String host;
    @Value("${sheenpay.wxapp.appid}")
    private String wxappAppid;
    @Value("${sheenpay.wxapplet.appid}")
    private String wxappletAppid;

    @Autowired
    private IRouteService routeService;
    @Autowired
    private ISeqIncrService seqIncrService;
    @Autowired
    private ITradeOrderSheenPayService iTradeOrderSheenPayService;
    @Autowired
    private ITradeRefundSheenpayService iTradeRefundSheenpayService;


    @Override
    public void unifiedOrder(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        log.info("|sheenpayhk|统一支付|开始：{}", JSON.toJSONString(requestMsg.getMap()));
        String routeCode = (String) requestMsg.get("routeCode");
        String mercId = (String) requestMsg.get("mercId");
        String platform = (String) requestMsg.get("platform");
        BigDecimal price = (BigDecimal) requestMsg.get("price");
        String tradeType = (String) requestMsg.get("tradeType");
        String openId = (String) requestMsg.get("openId");
        String userOperNo = (String) requestMsg.get("userOperNo");
        String tradeNo = (String) requestMsg.get("tradeNo");
        // 路由配置
        RouteConf routeConf = RequestMsgUtil.getRouteConf(mercId, platform, tradeType, routeCode, null);
        String seq = seqIncrService.nextVal(SeqIncrType.OUT_TRADE_NO_SHEENPAY.getId(), SeqIncrType.OUT_TRADE_NO_SHEENPAY.getLength(), Align.LEFT);
        String outTradeNo = DateTimeUtil.date8() + seq;
        String partnerCode = routeConf.getBankMercId();
        String credentialCode = routeConf.getPublicKey();
        //创建订单流水
        TradeOrderSheenPay sheenPay = new TradeOrderSheenPay();
        sheenPay.setOutTradeNo(outTradeNo);
        sheenPay.setTradeDate(DateTimeUtil.date10());
        sheenPay.setTradeTime(DateTimeUtil.time8());
        sheenPay.setBankMercId(partnerCode);
        sheenPay.setTerminalNo(credentialCode);
        sheenPay.setOrderStatus(OrderStatus.ADVANCE.getId());
        sheenPay.setRouteCode(routeCode);
        sheenPay.setTradeType(tradeType);
        sheenPay.setPlatform(platform);
        sheenPay.setPrice(price);
        sheenPay.setTradeNo(tradeNo);
        iTradeOrderSheenPayService.save(sheenPay);

        String apiUrl = SheepayhkApiUrlType.getApiUrl(requestMsg.get("sysCnl").toString(), partnerCode);

        if (StringUtils.isBlank(apiUrl)) {
            throw new BusiException(31124);
        }

        String url = host + "/api/v1.0/" + apiUrl + outTradeNo + "?" + queryParams(partnerCode, credentialCode);

        SheenReq req = new SheenReq();
        req.setCurrency("CNY");
        req.setPrice(price.multiply(new BigDecimal("100")));
        req.setDescription(PlatformType.parasByCode(platform).getName() + "商品");
        req.setChannel("Wechat");
        req.setOperator(userOperNo);
        Route one = routeService.getOne(new QueryWrapper<Route>().eq("route_code", RouteCode.SHEENPAYHK.getId()).last("limit 1"));
        req.setNotify_url(one.getNotifyUrl());

        String bankCode = requestMsg.get("bankCode").toString();
        if (bankCode.equals(BankCode.WEIXIN.getId()) && tradeType.equals(TradeType.APP.getId())) {
            req.setAppid(wxappAppid);
        } else if (bankCode.equals(BankCode.WEIXIN.getId()) && requestMsg.get("sysCnl").toString().equals("WX-APPLET")) {
            req.setAppid(wxappletAppid);
            req.setCustomer_id(openId);
        }

        String s = JSONUtil.toJsonStr(req);
        log.info("|sheenpayhk|统一支付|outTradeNo：{}，url：{},jsonbody:{}", outTradeNo, url, s);
        HttpRequest request = HttpUtil.createRequest(Method.PUT, url).body(s);
        request.contentType("application/json");
        HttpResponse execute = request.execute();
        String body = execute.body();
        log.info("|sheenpayhk|统一支付|outTradeNo：{}，Response：{}", outTradeNo, body);
        if (StrUtil.isBlank(body)) {
            throw new BusiException(13110);
        }
        SheenResp sheenResp = null;
        try {
            sheenResp = JSONUtil.toBean(body, SheenResp.class);
        } catch (Exception e) {
            throw new BusiException(13111);
        }

        String result_code = sheenResp.getResult_code();
        if (!SheenPayConstants.RESULT_CODE_SUCCESS.equalsIgnoreCase(result_code)) {
            log.error("|sheenpayhk|统一支付|outTradeNo：{}，return_code：{}，return_msg：{}", outTradeNo, sheenResp.getReturn_code(), sheenResp.getReturn_msg());
            throw new BusiException(11001, sheenResp.getReturn_msg());
        }

        //订单更新为W
        sheenPay.setOrderStatus(OrderStatus.WAIT_PAY.getId());
        iTradeOrderSheenPayService.updateById(sheenPay);

        unifiedOrderResponseMsg(requestMsg, routeConf, sheenResp, responseMsg);

        responseMsg.put("outTradeNo", outTradeNo);
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        log.info("|sheenpayhk|统一支付|结束：{}", JSON.toJSONString(responseMsg.getMap()));
    }

    private String queryParams(String partnerCode, String credentialCode) throws Exception {
        long time = System.currentTimeMillis();
        String nonceStr = RandomUtil.randomString(15);
        String validStr = partnerCode + "&" + time + "&" + nonceStr + "&" + credentialCode;
        String sign = Sign.signSHA256ToHex(validStr).toLowerCase();
        return "time=" + time + "&nonce_str=" + nonceStr + "&sign=" + sign;
    }

    @Override
    public ResponseMsg queryOrder(RequestMsg requestMsg) throws Exception {
        log.info("|sheenpayhk|订单查询|开始：{}", JSON.toJSONString(requestMsg.getMap()));
        ResponseMsg responseMsg = new ResponseMsg();
        String routeCode = (String) requestMsg.get("routeCode");
        String outTradeNo = (String) requestMsg.get("outTradeNo");
        String platform = (String) requestMsg.get("platform");
        String tradeType = (String) requestMsg.get("tradeType");
        String mercId = (String) requestMsg.get("mercId");

        RouteConf routeConf = RequestMsgUtil.getRouteConf(mercId, platform, tradeType, routeCode, null);
        String partnerCode = routeConf.getBankMercId();
        String credentialCode = routeConf.getPublicKey();

        TradeOrderSheenPay tradeOrderSheenPay = iTradeOrderSheenPayService.getOne(new QueryWrapper<TradeOrderSheenPay>().eq("out_trade_no", outTradeNo));
        if (null == tradeOrderSheenPay || OrderStatus.FAIL.getId().equals(tradeOrderSheenPay.getOrderStatus())) {
            responseMsg.put(ConstEC.RETURNCODE, "11001");
            responseMsg.put(ConstEC.RETURNMSG, ApplicationYmlUtil.get("11001"));
            return responseMsg;
        }
        String url = host + "/api/v1.0/gateway/partners/" + partnerCode + "/orders/" + outTradeNo + "?" + queryParams(partnerCode, credentialCode);
        HttpRequest request = HttpUtil.createRequest(Method.GET, url);
        HttpResponse execute = request.execute();
        String body = execute.body();
        log.info("|sheenpayhk|订单查询|outTradeNo：{}，Response：{}", outTradeNo, body);
        if (StrUtil.isBlank(body)) {
            throw new BusiException(13110);
        }
        SheenResp sheenResp = JSONUtil.toBean(body, SheenResp.class);

        //支付成功的处理   TODO 状态处理
        if ("PAY_SUCCESS".equalsIgnoreCase(sheenResp.getResult_code())) {
            Integer realFeeInt = sheenResp.getReal_fee();
            BigDecimal realFee = BigDecimal.valueOf(realFeeInt).divide(new BigDecimal(100));
            Date payTime = DateTimeUtil.formatStringToDate(sheenResp.getPay_time(), "yyyy-MM-dd HH:mm:ss");
            Map<String, Object> data = new HashMap<>();
            data.put("routeCode", RouteCode.SHEENPAYHK.getId());
            data.put("payDate", DateTimeUtil.formatTimestamp2String(payTime, "yyyy-MM-dd"));
            data.put("payTime", DateTimeUtil.formatTimestamp2String(payTime, "HH:mm:ss"));
            data.put("outTradeNo", sheenResp.getPartner_order_id());
            data.put("bankTradeNo", sheenResp.getOrder_id());
            data.put("price", realFee);
            data.put("fundBank", sheenResp.getChannel());
            data.put("returnCode", "10000");
            data.put("returnMsg", "交易成功");
            DepositProcHandler handler = (DepositProcHandler) SpringContextHolder.getBean(routeCode.toLowerCase() + ConstEC.DEPOSITPROCHANDLER);
            handler.proc(data);
        }

        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        log.info("|sheenpayhk|订单查询|结束：{}", JSON.toJSONString(responseMsg.getMap()));
        return responseMsg;
    }

    @Override
    public ResponseMsg refundOrder(RequestMsg requestMsg) throws Exception {
        log.info("|sheenpayhk|退款|开始：{}", JSON.toJSONString(requestMsg.getMap()));
        ResponseMsg responseMsg = new ResponseMsg();
        String routeCode = (String) requestMsg.get("routeCode");
        String mercId = (String) requestMsg.get("mercId");
        String platform = (String) requestMsg.get("platform");
        String tradeType = (String) requestMsg.get("tradeType");
        String outTradeNo = (String) requestMsg.get("outTradeNo");
        String refundNo = (String) requestMsg.get("refundNo");
        BigDecimal price = (BigDecimal) requestMsg.get("price");

        // 根据路由获取秘钥
        RouteConf routeConf = RequestMsgUtil.getRouteConf(mercId, platform, tradeType, routeCode, null);
        String partnerCode = routeConf.getBankMercId();
        String credentialCode = routeConf.getPublicKey();

        TradeOrderSheenPay tradeOrderSheenPay = iTradeOrderSheenPayService.getOne(new QueryWrapper<TradeOrderSheenPay>().eq("out_trade_no", outTradeNo));
        // 没有支付成功就不能退款
        if (!OrderStatus.SUCCESS.getId().equalsIgnoreCase(tradeOrderSheenPay.getOrderStatus())) {
            throw new BusiException(31120);
        }
        TradeRefundSheenpay refundSheenpay = iTradeRefundSheenpayService.getOne(new QueryWrapper<TradeRefundSheenpay>().eq("refund_no", refundNo).last("limit 1"));
        if (refundSheenpay == null) {
            // 退款记录存库
            refundSheenpay = new TradeRefundSheenpay();
            BeanUtils.populate(refundSheenpay, requestMsg.getMap());
            String seq = seqIncrService.nextVal(SeqIncrType.OUT_REFUND_NO_SHEENPAY.getId(), 8, Align.LEFT);
            String outRefundNo = DateTimeUtil.date8() + seq;    // 发往外部的退款订单号
            refundSheenpay.setRefundNo(refundNo);
            refundSheenpay.setOrderStatus(OrderStatus.REFUND.getId());
            refundSheenpay.setOutRefundNo(outRefundNo);
            refundSheenpay.setRefundDate(DateTimeUtil.date10());
            refundSheenpay.setRefundTime(DateTimeUtil.time8());
            iTradeRefundSheenpayService.save(refundSheenpay);
        }

        // 调用sheenpay退款
        String outRefundNo = refundSheenpay.getOutRefundNo();
        String url = host + "/api/v1.0/gateway/partners/" + partnerCode + "/orders/" + outTradeNo + "/refunds/" + outRefundNo + "?" + queryParams(partnerCode, credentialCode);
        SheenRefundDTO sheenRefundReq = new SheenRefundDTO();
        int fee = price.multiply(new BigDecimal(100)).intValue();
        sheenRefundReq.setFee(fee);
        HttpRequest request = HttpUtil.createRequest(Method.PUT, url).body(JSONUtil.toJsonStr(sheenRefundReq));
        request.contentType("application/json");
        request.header("Accept", "application/json");
        HttpResponse execute = request.execute();
        String body = execute.body();
        SheenRefundDTO sheenRefundResp = JSONUtil.toBean(body, SheenRefundDTO.class);
        String returnCode = sheenRefundResp.getReturnCode();
        String returnMsg = sheenRefundResp.getReturnMsg();
        if (!SheenpayReturnCodeEnmu.SUCCESS.getCode().equalsIgnoreCase(returnCode)) {
            log.error("|sheenpayhk|退款查询|调用sheenpay退款查询接口异常：{}", body);
            responseMsg.put(ConstEC.RETURNCODE, 11001);
            responseMsg.put(ConstEC.RETURNMSG, returnMsg);
            refundSheenpay.setReturnCode("11001");
            refundSheenpay.setReturnMsg(returnMsg);
            iTradeRefundSheenpayService.updateById(refundSheenpay);
            return responseMsg;
        }
        log.info("|sheenpayhk|统一支付|结束：{}", JSON.toJSONString(responseMsg.getMap()));
        String resultCode = sheenRefundResp.getResultCode();
        String refundId = sheenRefundResp.getRefundId();
        int amount = sheenRefundResp.getAmount();
        BigDecimal actualPrice = BigDecimal.valueOf(amount).divide(new BigDecimal(100));
        refundSheenpay.setReturnCode(resultCode);
        refundSheenpay.setReturnMsg(returnMsg);
        refundSheenpay.setBankRefundNo(refundId);
        refundSheenpay.setActualPrice(actualPrice);
        Date bankReturnDate = new Date();
        refundSheenpay.setBankReturnDate(DateTimeUtil.date10(bankReturnDate));
        refundSheenpay.setBankReturnTime(DateTimeUtil.time8(bankReturnDate));
        iTradeRefundSheenpayService.updateById(refundSheenpay);

        responseMsg.put("refundNo", refundNo);
        responseMsg.put("outRefundNo", sheenRefundResp.getPartnerRefundId());
        responseMsg.put("bankRefundNo", sheenRefundResp.getRefundId());
        return responseMsg;
    }

    @Override
    public ResponseMsg queryRefundOrder(RequestMsg requestMsg) throws Exception {
        log.info("|sheenpayhk|退款查询|开始，参数：{}", JSON.toJSONString(requestMsg.getMap()));
        ResponseMsg responseMsg = new ResponseMsg();
        String routeCode = (String) requestMsg.get("routeCode");
        String mercId = (String) requestMsg.get("mercId");
        String platform = (String) requestMsg.get("platform");
        String tradeType = (String) requestMsg.get("tradeType");
        String outTradeNo = (String) requestMsg.get("outTradeNo");
        String outRefundNo = (String) requestMsg.get("outRefundNo");

        // 根据路由获取秘钥
        RouteConf routeConf = RequestMsgUtil.getRouteConf(mercId, platform, tradeType, routeCode, null);
        String partnerCode = routeConf.getBankMercId();
        String credentialCode = routeConf.getPublicKey();

        TradeRefundSheenpay tradeRefundSheenpay = iTradeRefundSheenpayService.getOne(new QueryWrapper<TradeRefundSheenpay>().eq("out_trade_no", outTradeNo));
        // 退款订单状态为成功，直接返回
        if (OrderStatus.SUCCESS.getId().equalsIgnoreCase(tradeRefundSheenpay.getOrderStatus())) {
            responseMsg.put("bankRefundNo", tradeRefundSheenpay.getBankRefundNo());
            responseMsg.put("orderStatus", tradeRefundSheenpay.getOrderStatus());
            responseMsg.put("refundChannel", tradeRefundSheenpay.getRefundChannel());
            responseMsg.put("actualPrice", tradeRefundSheenpay.getActualPrice());
            responseMsg.put("bankReturnDate", tradeRefundSheenpay.getBankReturnDate());
            responseMsg.put("bankReturnTime", tradeRefundSheenpay.getBankReturnTime());
            responseMsg.put("outRefundNo", tradeRefundSheenpay.getOutRefundNo());
            responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
            responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
            log.info("|sheenpayhk|退款查询|完成：{}", JSON.toJSONString(responseMsg));
            return responseMsg;
        }
        // 调用sheenpay查询
        // TODO url配置
        String url = host + "/api/v1.0/gateway/partners/" + partnerCode + "/orders/" + outTradeNo + "/refunds/" + outRefundNo + "?" + queryParams(partnerCode, credentialCode);
        HttpRequest request = HttpUtil.createRequest(Method.GET, url);
        request.contentType("application/json");
        request.header("Accept", "application/json");
        HttpResponse execute = request.execute();
        String body = execute.body();
        SheenRefundQueryDTO queryResp = JSONUtil.toBean(body, SheenRefundQueryDTO.class);
        String returnCode = queryResp.getReturnCode();
        String returnMsg = queryResp.getReturnMsg();
        if (!SheenpayReturnCodeEnmu.SUCCESS.getCode().equalsIgnoreCase(returnCode)) {
            log.error("|sheenpayhk|退款查询|调用sheenpay退款查询接口异常：{}", body);
            throw new BusiException(11001, returnMsg);
        }
        String resultCode = queryResp.getResultCode();
        // 更新退款订单状态
        switch (resultCode) {
            case "WAITING":
                // 正在提交
                break;
            case "SUCCESS":
                // 提交成功
                tradeRefundSheenpay.setOrderStatus(OrderStatus.REFUND_WAIT.getId());
                tradeRefundSheenpay.setReturnCode(resultCode);
                tradeRefundSheenpay.setReturnMsg("等待退款");
                iTradeRefundSheenpayService.updateById(tradeRefundSheenpay);

                responseMsg.put("orderStatus", OrderStatus.REFUND_WAIT.getId());
                responseMsg.put(ConstEC.RETURNCODE, resultCode);
                responseMsg.put(ConstEC.RETURNMSG, "等待退款");
                break;
            case "FINISHED":
                // 退款成功
                int amount = queryResp.getAmount();
                String refundId = queryResp.getRefundId();  // TODO 确认字段是否是为 BankRefundNo
                Date bankReturnDate = new Date();
                BigDecimal actualPrice = BigDecimal.valueOf(amount).divide(new BigDecimal(100));
                tradeRefundSheenpay.setActualPrice(actualPrice);
                tradeRefundSheenpay.setOrderStatus(OrderStatus.SUCCESS.getId());
                tradeRefundSheenpay.setReturnCode(resultCode);
                tradeRefundSheenpay.setReturnCode("退款成功");
                tradeRefundSheenpay.setBankReturnDate(DateTimeUtil.date10(bankReturnDate));
                tradeRefundSheenpay.setBankReturnTime(DateTimeUtil.time8(bankReturnDate));
                tradeRefundSheenpay.setBankRefundNo(refundId);
                iTradeRefundSheenpayService.updateById(tradeRefundSheenpay);

                responseMsg.put("orderStatus", tradeRefundSheenpay.getOrderStatus());
                responseMsg.put("refundChannel", tradeRefundSheenpay.getRefundChannel());
                responseMsg.put("actualPrice", tradeRefundSheenpay.getActualPrice());
                responseMsg.put("bankReturnDate", tradeRefundSheenpay.getBankReturnDate());
                responseMsg.put("bankReturnTime", tradeRefundSheenpay.getBankReturnTime());
                responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
                responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
                break;
            case "CREATE_FAILED":
            case "FAILED":
            case "CHANGE":
                // 退款无法到账，需要人工介入
                tradeRefundSheenpay.setOrderStatus(OrderStatus.FAIL.getId());
                tradeRefundSheenpay.setReturnCode(resultCode);
                tradeRefundSheenpay.setReturnMsg("退款失败");
                iTradeRefundSheenpayService.updateById(tradeRefundSheenpay);

                responseMsg.put("orderStatus", OrderStatus.FAIL.getId());
                responseMsg.put(ConstEC.RETURNCODE, resultCode);
                responseMsg.put(ConstEC.RETURNMSG, "退款失败");
                break;
            default:
                break;
        }
        return responseMsg;
    }

    private void unifiedOrderResponseMsg(RequestMsg requestMsg, RouteConf routeConf, SheenResp sheenResp, ResponseMsg responseMsg) throws Exception {
        String tradeType = (String) requestMsg.get("tradeType");
        String partnerCode = routeConf.getBankMercId();
        String credentialCode = routeConf.getPublicKey();

        Map<String, Object> dataMap = new HashMap<String, Object>();
        if (TradeType.APP.getId().equals(tradeType)) {
            //组装银行支付请求报文
            dataMap.put("appid", sheenResp.getSdk_params().get("appid"));
            dataMap.put("partnerid", sheenResp.getSdk_params().get("partnerid"));
            dataMap.put("prepayid", sheenResp.getSdk_params().get("prepayid"));
            dataMap.put("package", sheenResp.getSdk_params().get("package"));
            dataMap.put("noncestr", sheenResp.getSdk_params().get("noncestr"));
            dataMap.put("timestamp", sheenResp.getSdk_params().get("timestamp"));
            dataMap.put("sign", sheenResp.getSdk_params().get("sign"));
        } else if (TradeType.JSAPI.getId().equals(tradeType)) {
            //公众号的需要这个
            if(requestMsg.get("sysCnl").toString().equals("WX-PUBLIC")){
                String pay_url = sheenResp.getPay_url()+ "?" + queryParams(partnerCode, credentialCode);
                String callbackUrl = (String) requestMsg.get("callbackUrl");
                if (!StringUtils.isBlank(callbackUrl)) {
                    pay_url += "&redirect=" + callbackUrl;
                }
                dataMap.put("payUrl", pay_url);
            }else {
                //组装银行支付请求报文
                dataMap.put("appId", sheenResp.getSdk_params().get("appId"));
                dataMap.put("signType", sheenResp.getSdk_params().get("signType"));
                dataMap.put("package", sheenResp.getSdk_params().get("package"));
                dataMap.put("nonceStr",sheenResp.getSdk_params().get("nonceStr"));
                dataMap.put("timeStamp", sheenResp.getSdk_params().get("timeStamp"));
                dataMap.put("sign", sheenResp.getSdk_params().get("paySign"));
            }
        } else if (TradeType.MWEB.getId().equals(tradeType)) {

            String pay_url = sheenResp.getPay_url() + "?" + queryParams(partnerCode, credentialCode);
            dataMap.put("payUrl", pay_url);
        }

        dataMap.put("routeCode", routeConf.getRouteCode());
        responseMsg.put(ConstEC.DATA, dataMap);
    }


}
