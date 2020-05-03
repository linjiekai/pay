package com.mppay.gateway.handler.withdr;

import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.mppay.service.entity.*;
import com.mppay.service.service.*;
import com.mppay.service.service.common.ICipherService;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.Align;
import com.mppay.core.constant.CardBindStatus;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.OrderStatus;
import com.mppay.core.constant.RouteCode;
import com.mppay.core.constant.SeqIncrType;
import com.mppay.core.constant.TradeCode;
import com.mppay.core.constant.WithdrOrderStatus;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.AESCoder;
import com.mppay.core.sign.Sign;
import com.mppay.core.sign.alipay.AlipaySignature;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.core.utils.HttpClientUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.DepositProcHandler;
import com.mppay.gateway.handler.WithdrOrderBusiHandler;

import lombok.extern.slf4j.Slf4j;

@Service("alipayWithdrOrderBusiHandler")
@Slf4j
public class AlipayWithdrOrderBusiHandlerImpl implements WithdrOrderBusiHandler {

    @Autowired
    private ISeqIncrService seqIncrService;

    @Autowired
    private IWithdrOrderAlipayService withdrOrderAlipayService;

    @Autowired
    private IRouteConfService routeConfService;

    @Autowired
    private IBankRouteService bankRouteService;

    @Autowired
    private ICardBindService cardBindService;
    
    @Autowired
	private ICipherService iCipherService;

    @Autowired
    private IWithdrOrderService withdrOrderService;

    @Value("${alipay.gatewayurl}")
    private String gatewayurl;

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
        //阿里没有
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
        //阿里没有
        return null;
    }


    /**
     * 提现申请
     *
     * @param requestMsg
     * @return
     * @throws Exception
     */
    @Override
    public ResponseMsg withdrOrder(RequestMsg requestMsg) throws Exception {
        ResponseMsg responseMsg = new ResponseMsg();

        String withdrOrderNo = (String) requestMsg.get("withdrOrderNo");

        if (StringUtils.isBlank(withdrOrderNo)) {
            log.error("响应alipay订单提现请求报文：{}", JSON.toJSONString(responseMsg));
            return null;
        }

        WithdrOrderAlipay withdrOrderAlipay = withdrOrderAlipayService.getOne(new QueryWrapper<WithdrOrderAlipay>().eq("withdr_order_no", withdrOrderNo));

        String outTradeNo = null;

        //组装银行统一下单请求报文
        Map<String, Object> contentMap = new HashMap<String, Object>();

		String bankCardNo = null;
        if (null != withdrOrderAlipay) {
            if (withdrOrderAlipay.getOrderStatus().equals(OrderStatus.SUCCESS.getId())) {
                log.info("订单提现状态已成功[{}]", withdrOrderAlipay.getOrderStatus());

                responseMsg.put("outTradeNo", withdrOrderAlipay.getOutTradeNo());
                responseMsg.put("bankWithdrNo", withdrOrderAlipay.getBankWithdrNo());
                responseMsg.put("orderStatus", withdrOrderAlipay.getOrderStatus());
                responseMsg.put("routeCode", withdrOrderAlipay.getRouteCode());
                responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
                responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
                return responseMsg;
            }
            bankCardNo = iCipherService.decryptAES(withdrOrderAlipay.getBankCardNo());
            contentMap.put("out_biz_no", withdrOrderAlipay.getOutTradeNo());
            contentMap.put("payee_type", "ALIPAY_USERID");
            contentMap.put("payee_account", bankCardNo);
            contentMap.put("amount", withdrOrderAlipay.getPrice());
            contentMap.put("remark", "用户转账");

            //提现流水存在就从提现流水获取路由编号
            requestMsg.put("routeCode", withdrOrderAlipay.getRouteCode());
        } else {
            withdrOrderAlipay = new WithdrOrderAlipay();
            BeanUtils.populate(withdrOrderAlipay, requestMsg.getMap());

            bankCardNo = iCipherService.decryptAES(withdrOrderAlipay.getBankCardNo());
            outTradeNo = DateTimeUtil.date8() + seqIncrService.nextVal(SeqIncrType.OUT_WITHDR_NO_ALIPAY.getId(), 10, Align.LEFT);

            withdrOrderAlipay.setOutTradeNo(outTradeNo);

            withdrOrderAlipay.setOrderDate(DateTimeUtil.date10());
            withdrOrderAlipay.setOrderTime(DateTimeUtil.time8());

            withdrOrderAlipayService.save(withdrOrderAlipay);

            //组装银行统一下单请求报文
            contentMap.put("out_biz_no", outTradeNo);
            contentMap.put("payee_type", "ALIPAY_USERID");
            contentMap.put("payee_account", bankCardNo);
            contentMap.put("amount", withdrOrderAlipay.getPrice());
            contentMap.put("remark", "用户转账");

            //获取路由关联信息
            BankRoute bankRoute = bankRouteService.getOne(new QueryWrapper<BankRoute>().eq("bank_code", requestMsg.get("bankCode")).eq("trade_code", TradeCode.WITHDRAW.getId())
                    .eq("bank_card_type", requestMsg.get("bankCardType"))
                    .eq("merc_id", requestMsg.get("mercId"))
            );

            requestMsg.put("routeCode", bankRoute.getRouteCode());
        }

        //获取用户绑卡信息
        WithdrOrder withdrOrder = withdrOrderService.getOne(new QueryWrapper<WithdrOrder>().eq("withdr_order_no", withdrOrderNo));
        CardBind cardBind = cardBindService.getOne(new QueryWrapper<CardBind>().eq("agr_no", withdrOrder.getAgrNo()));

        //获取路由关联信息
        RouteConf routeConf = routeConfService.getOne(new QueryWrapper<RouteConf>()
                .eq("merc_id", requestMsg.get("mercId"))
                .eq("route_code", requestMsg.get("routeCode"))
                .eq("trade_type", cardBind.getTradeType())
                .eq("platform", cardBind.getPlatform())
        );


        String content = JSONObject.toJSONString(contentMap);

        //组装请求报文
        Map<String, Object> bankMap = new HashMap<String, Object>();
        bankMap.put("app_id", routeConf.getAppId());
        bankMap.put("method", "alipay.fund.trans.toaccount.transfer");
        bankMap.put("charset", ConstEC.ENCODE_UTF8);
        bankMap.put("sign_type", "RSA2");
        bankMap.put("timestamp", DateTimeUtil.formatTimestamp2String(new Date(), "yyy-MM-dd HH:mm:ss"));
        bankMap.put("version", "1.0");
        bankMap.put("biz_content", content);

        //签名 加密
        String plain = Sign.getPlain(bankMap, true);
        String sign = AlipaySignature.rsa256Sign(plain, routeConf.getPrivateKey(), ConstEC.ENCODE_UTF8);
        bankMap.put("sign", sign);

        String paramStr = Sign.getPlainURLEncoder(bankMap, ConstEC.ENCODE_UTF8);
        content = "biz_content=" + URLEncoder.encode(content, ConstEC.ENCODE_UTF8);

        log.info("请求支付宝提现订单接口报文：{}", content);
        //请求支付宝接口
        String responseStr = HttpClientUtil.httpsRequest(gatewayurl + "?" + paramStr, "POST", content);
        log.info("响应支付宝提现订单接口报文：{}", responseStr);

        //解析结果
        Map<String, Object> resultMap = verify(responseStr, "alipay_fund_trans_toaccount_transfer_response", routeConf);

        //响应为空时
        if (null == resultMap.get("code")) {
            responseMsg.put(ConstEC.RETURNCODE, "11001");
            responseMsg.put(ConstEC.RETURNMSG, ApplicationYmlUtil.get("11001"));
            return responseMsg;
        }

        //失败结果时
        if (!ConstEC.SUCCESS_10000.equals(resultMap.get("code"))) {
            String returnCode = (String) resultMap.get("sub_code");
            String returnMsg = (String) resultMap.get("sub_msg");
            withdrOrderAlipay.setReturnCode(returnCode);
            withdrOrderAlipay.setReturnMsg(returnMsg);

            withdrOrderAlipayService.updateById(withdrOrderAlipay);
            responseMsg.put(ConstEC.RETURNCODE, resultMap.get("code"));
            responseMsg.put(ConstEC.RETURNMSG, resultMap.get("sub_msg"));
            return responseMsg;
        }

        //成功是更新订单状态
        withdrOrderAlipay.setBankWithdrNo(resultMap.get("order_id").toString());
        withdrOrderAlipay.setOrderStatus(WithdrOrderStatus.BANK_WAIT.getId());

        withdrOrderAlipayService.updateById(withdrOrderAlipay);

        responseMsg.put("outTradeNo", withdrOrderAlipay.getOutTradeNo());
        responseMsg.put("bankWithdrNo", withdrOrderAlipay.getBankWithdrNo());
        responseMsg.put("orderStatus", withdrOrderAlipay.getOrderStatus());
        responseMsg.put("routeCode", requestMsg.get("routeCode"));
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        return responseMsg;
    }

    @Override
    public ResponseMsg queryWithdrOrder(RequestMsg requestMsg) throws Exception {
        WithdrOrderAlipay withdrOrderAlipay = withdrOrderAlipayService.getOne(
                new QueryWrapper<WithdrOrderAlipay>().eq("out_trade_no", requestMsg.get("outTradeNo")));

        ResponseMsg responseMsg = new ResponseMsg();
        if (OrderStatus.SUCCESS.getId().equals(withdrOrderAlipay.getOrderStatus())) {
            responseMsg.put("orderStatus", withdrOrderAlipay.getOrderStatus());
            responseMsg.put("outTradeNo", withdrOrderAlipay.getOutTradeNo());
            responseMsg.put("bankWithdrNo", withdrOrderAlipay.getBankWithdrNo());
            responseMsg.put("bankWithdrDate", withdrOrderAlipay.getBankWithdrDate());
            responseMsg.put("bankWithdrTime", withdrOrderAlipay.getBankWithdrTime());
            responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
            responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);

            return responseMsg;
        }

        RouteConf routeConf = routeConfService.getOne(new QueryWrapper<RouteConf>()
                .eq("merc_id", requestMsg.get("mercId"))
                .eq("route_code", requestMsg.get("routeCode"))
                .eq("trade_type", requestMsg.get("tradeType"))
                .eq("platform", requestMsg.get("platform"))
        );
        //组装银行请求报文
        Map<String, Object> contentMap = new HashMap<String, Object>();
        contentMap.put("out_biz_no", withdrOrderAlipay.getOutTradeNo());
        contentMap.put("order_id", withdrOrderAlipay.getBankWithdrNo());

        String content = JSONObject.toJSONString(contentMap);

        Map<String, Object> bankMap = new HashMap<String, Object>();

        bankMap.put("app_id", routeConf.getAppId());
        bankMap.put("method", "alipay.fund.trans.order.query");
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

        //请求接口
        String responseStr = HttpClientUtil.httpsRequest(gatewayurl + "?" + paramStr, "POST", content);

        //返回报文验签
        Map<String, Object> resultMap = verify(responseStr, "alipay_fund_trans_order_query_response", routeConf);

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

        String status = (String) resultMap.get("status");

        if (null != status && status.equals(ConstEC.SUCCESS) && withdrOrderAlipay.getBankWithdrNo().equals(resultMap.get("order_id"))) {
            log.info("响应支付宝查询提现订单接口报文[{}]", responseStr);

            Date payTime = null;

            if (null != resultMap.get("pay_date")) {
                payTime = DateTimeUtil.formatStringToDate(resultMap.get("pay_date").toString(), "yyyy-MM-dd HH:mm:ss");
            } else {
                payTime = new Date();
            }

            Map<String, Object> data = new HashMap<String, Object>();
            data.put("bankWithdrDate", DateTimeUtil.formatTimestamp2String(payTime, "yyyy-MM-dd"));
            data.put("bankWithdrTime", DateTimeUtil.formatTimestamp2String(payTime, "HH:mm:ss"));
            data.put("orderStatus", WithdrOrderStatus.SUCCESS.getId());
            data.put("outTradeNo", withdrOrderAlipay.getOutTradeNo());
            data.put("bankWithdrNo", resultMap.get("order_id").toString());
            data.put("returnCode", "10000");
            data.put("returnMsg", "交易成功");

            DepositProcHandler handler = (DepositProcHandler) SpringContextHolder.getBean(RouteCode.ALIPAY.getId().toLowerCase() + ConstEC.DEPOSITPROCHANDLER);
            handler.procWithdr(data);

//			withdrOrderAlipay.setBankWithdrDate(DateTimeUtil.formatTimestamp2String(payTime, "yyyy-MM-dd"));
//			withdrOrderAlipay.setBankWithdrTime(DateTimeUtil.formatTimestamp2String(payTime, "HH:mm:ss"));
//			withdrOrderAlipay.setOrderStatus(OrderStatus.SUCCESS.getId());
//			withdrOrderAlipayService.updateById(withdrOrderAlipay);
        }


//		responseMsg.put("orderStatus", withdrOrderAlipay.getOrderStatus());
//		responseMsg.put("outTradeNo", withdrOrderAlipay.getOutTradeNo());
//		responseMsg.put("bankWithdrNo", withdrOrderAlipay.getBankWithdrNo());
//		responseMsg.put("bankWithdrDate", withdrOrderAlipay.getBankWithdrDate());
//		responseMsg.put("bankWithdrTime", withdrOrderAlipay.getBankWithdrTime());
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        return responseMsg;
    }

    @Override
    public void beforeWithdrCardBind(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        //阿里的暂时无需操作
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
