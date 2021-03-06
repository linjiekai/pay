package com.mppay.gateway.handler.quick;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.*;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.Sign;
import com.mppay.core.sign.gaohuitong.GaohuitongConstants;
import com.mppay.core.sign.gaohuitong.GaohuitongMessgeUtil;
import com.mppay.core.utils.*;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.dto.platform.gaohuitong.*;
import com.mppay.gateway.handler.DepositProcHandler;
import com.mppay.gateway.handler.QuickBusiHandler;
import com.mppay.gateway.handler.utils.RequestMsgUtil;
import com.mppay.service.entity.*;
import com.mppay.service.service.*;
import com.mppay.service.service.common.ICipherService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;

import static com.mppay.core.utils.StringUtil.getRandom;


@Service("gaohuitongQuickBusiHandler")
@Slf4j
public class GaohuitongQuickBusiHandlerImpl implements QuickBusiHandler {
    @Value("${ght.xfhl.version}")
    private String version;
    @Value("${ght.xfhl.address}")
    private String address;
    @Value("${gaohuitong.quickInter.commonSyncInter}")
    private String commonUrl;

    @Autowired
    private ISeqIncrService seqIncrService;
    @Autowired
    private IRouteDictionaryService routeDictionaryService;
    @Autowired
    private IBankService bankService;
    @Autowired
    private ITradeOrderGaohuitongService tradeOrderGaohuitongService;
    @Autowired
    private IQuickAgrService iQuickAgrService;
    @Autowired
    private ITradeOrderGaohuitongService iTradeOrderGaohuitongService;
    @Autowired
    private IRouteService routeService;
    @Autowired
    private IBankRouteService iBankRouteService;
    @Autowired
    private ICipherService cipherServiceImpl;
    @Autowired
    private IAliResourcesService aliResourcesService;

    /**
     * @param :[requestMsg, responseMsg]
     * @return :void
     * @Description(描述): 支付请求
     * @auther: Jack Lin
     * @date: 2019/9/17 17:13
     */
    @Override
    public void unifiedOrder(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        log.info("|快捷支付|高汇通|支付请求|请求参数：{}", JSON.toJSONString(requestMsg));
        //校验下加密数据
        RequestMsgUtil.validateRequestMsg(requestMsg);
        //用户操作号
        String userOperNo = (String) requestMsg.get("userOperNo");
        String agrNo = (String) requestMsg.get("agrNo");
        BigDecimal price = (BigDecimal) requestMsg.get("price");
        String productName = (String) GoodsPriceType.parasName(requestMsg.get("platform").toString());
        String mercId = (String) requestMsg.get("mercId");
        String platform = (String) requestMsg.get("platform");
        String agencyId = (String) requestMsg.get("agencyId");

        RouteConf routeConf = RequestMsgUtil.getRouteConf(mercId, platform, TradeType.QUICK.getId(), GaohuitongConstants.GHT_ROUTE, agencyId);
        if(StringUtils.isBlank(agencyId)){
            agencyId = routeConf.getBankMercId();
        }
        String terminal = routeConf.getAppId();
        Route route = routeService.getOne(new QueryWrapper<Route>().eq("route_code", GaohuitongConstants.GHT_ROUTE));
        //快捷签约协议表
        QuickAgr quickAgr = iQuickAgrService.getOne(new QueryWrapper<QuickAgr>().eq("agr_no", agrNo));
        Optional.ofNullable(quickAgr).orElseThrow(() -> new BusiException(31104));

        //创建订单流水
        TradeOrderGaohuitong gaohuitong = new TradeOrderGaohuitong();
        String seq = seqIncrService.nextVal(SeqIncrType.OUT_TRADE_NO_GAOHUITONG.getId(), SeqIncrType.OUT_TRADE_NO_GAOHUITONG.getLength(), Align.LEFT);
        String outTradeNo = DateTimeUtil.date8() + seq;
        BeanUtils.populate(gaohuitong, requestMsg.getMap());
        gaohuitong.setOutTradeNo(outTradeNo);
        gaohuitong.setTradeDate(DateTimeUtil.date10());
        gaohuitong.setTradeTime(DateTimeUtil.time8());
        gaohuitong.setOrderStatus(OrderStatus.ADVANCE.getId());
        gaohuitong.setTerminalNo(terminal);
        gaohuitong.setOrgNo(agencyId);
        gaohuitong.setOpenId(quickAgr.getBankCardNo());
        iTradeOrderGaohuitongService.save(gaohuitong);

        GHTReq<GHTOrderDTO> req = new GHTReq<>();
        GHTResp<GHTOrderDTO> resp = new GHTResp<>();
        req.setHead(buildHead(GaohuitongConstants.MESSAGE_PROPERTY_MSGTYPE_MERCHANT, GaohuitongConstants.GHT_QUICK_ORDER, outTradeNo, agencyId));
        //body
        GHTOrderDTO dto = new GHTOrderDTO();
        dto.setUserId(userOperNo);
        dto.setTerminalId(terminal);//终端号
        dto.setBindId(quickAgr.getBindAgrNo());
        dto.setCurrency("CNY");
        price = price.multiply(new BigDecimal("100")); //支付金额，单位是分
        dto.setAmount(price.setScale(0, BigDecimal.ROUND_UP).intValue() + ""); //向上取整
        dto.setProductCategory("16");
        dto.setProductName(productName);
        dto.setReckonCurrency("CNY");
        dto.setNotify_url(route.getNotifyUrl());
        req.setBody(dto);

        sendRequest(req, resp, GaohuitongConstants.GHT_QUICK_ORDER, commonUrl, routeConf, GHTOrderDTO.class, route.getCallbackUrl());
        //校验结果
        verifyResp(resp.getHead());

        GHTOrderDTO body = resp.getBody();
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        responseMsg.put("bindAgrNo", StringUtils.isNotEmpty(body.getBindId()) ? body.getBindId() : ""); //银行绑定协议号
        responseMsg.put("userId", StringUtils.isNotEmpty(body.getUserId()) ? body.getUserId() : ""); //商城用户id
        responseMsg.put("outTradeNo", outTradeNo); //请求支付流水
        log.info("|快捷支付|高汇通|支付请求|响应参数：{}", JSON.toJSONString(responseMsg));
    }

    /**
     * 快捷支付确认【高汇通：确认支付】
     *
     * @param requestMsg  userOperNo    用户操作号
     *                    smsOrderNo    (原订单号)
     *                    smsCode   短信验证码
     *                    sysCnl    移动终端设备类型
     *                    deviceId  移动终端设备的唯一标识
     *                    clientIp  客户端ip
     *                    childMerchantId   (子商户号，如果为一户一码模式则必填)
     *                    mercId    商城商户号
     *                    platform  平台编号 名品猫:MPMALL，合伙人：PTMALL ....
     *                    tradeType 交易类型 JSAPI：公众号或小程序支付 APP：app支付 NATIVE：扫码支付 MICROPAY：刷卡支付 MWEB：H5支付
     * @param responseMsg bindId    绑卡ID
     *                    amount    交易金额（以分为单位）
     *                    bindValid 绑卡有效期（格式为yyyyMMddHHmmss）
     *                    bankCardNo 银行卡号
     *                    bankCode  银行编号
     * @throws Exception
     */
    @Override
    public void confirmOrder(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        log.info("|快捷支付|高汇通|快捷支付确认|请求参数：{}", JSON.toJSONString(requestMsg));
        //校验下加密数据
        RequestMsgUtil.validateRequestMsg(requestMsg);
        String userOperNo = (String) requestMsg.get("userOperNo");
        String outTradeNo = (String) requestMsg.get("outTradeNo");
        String smsCode = (String) requestMsg.get("smsCode");
        String clientIp = (String) requestMsg.get("clientIp");
        String mercId = (String) requestMsg.get("mercId");
        String platform = (String) requestMsg.get("platform");
        String agencyId = null;
        if (requestMsg.get("agencyId") != null) {
            agencyId = (String) requestMsg.get("agencyId");
        }

        // 路由信息获取
        RouteConf routeConf = RequestMsgUtil.getRouteConf(mercId, platform, TradeType.QUICK.getId(), GaohuitongConstants.GHT_ROUTE, agencyId);
        TradeOrderGaohuitong tradeOrderGaohuitong = tradeOrderGaohuitongService.getOne(new QueryWrapper<TradeOrderGaohuitong>()
                .eq("out_trade_no", outTradeNo)
                .eq("order_status", OrderStatus.ADVANCE.getId()));
        if (null == tradeOrderGaohuitong) {
            log.error("|快捷支付|高汇通|快捷支付确认|支付订单号OutTradeNo[{}]状态[{}],异常返回", tradeOrderGaohuitong.getOutTradeNo(), tradeOrderGaohuitong.getOrderStatus());
            throw new BusiException(11003);
        }
        Route route = routeService.getOne(new QueryWrapper<Route>().eq("route_code", GaohuitongConstants.GHT_ROUTE));

        // 请求报文封装
        GHTReq<GHTConfirmOrderDTO> req = new GHTReq<>();
        GHTResp<GHTConfirmOrderDTO> resp = new GHTResp<>();
        GHTConfirmOrderDTO confirmOrderDTO = new GHTConfirmOrderDTO();
        confirmOrderDTO.setUserId(userOperNo);
        String reqMsgId = DateTimeUtil.date8() + seqIncrService.nextVal(SeqIncrType.REQUEST_ID_GAOHUITONG.getId(), SeqIncrType.REQUEST_ID_GAOHUITONG.getLength(), Align.LEFT);
        confirmOrderDTO.setOriReqMsgId(outTradeNo);
        confirmOrderDTO.setValidateCode(smsCode);
        confirmOrderDTO.setDeviceType("1");
        confirmOrderDTO.setDeviceId(UUID.randomUUID().toString().replace("-", "").toUpperCase());
        confirmOrderDTO.setUserIP(clientIp);
        req.setHead(buildHead(GaohuitongConstants.MESSAGE_PROPERTY_MSGTYPE_MERCHANT, GaohuitongConstants.GHT_QUICK_CONFIRM_ORDER, reqMsgId, routeConf.getBankMercId()));
        req.setBody(confirmOrderDTO);

        // 发送请求
        sendRequest(req, resp, GaohuitongConstants.GHT_QUICK_CONFIRM_ORDER, commonUrl, routeConf, GHTConfirmOrderDTO.class, route.getCallbackUrl());
        GHTHeadDTO head = resp.getHead();
        verifyResp(head);

        // 响应处理
        GHTConfirmOrderDTO respBody = resp.getBody();
        // 更新trade_order_gaohuitong 订单状态[order_status] 为:等待支付W
        tradeOrderGaohuitong.setOrderStatus(OrderStatus.WAIT_PAY.getId());
        tradeOrderGaohuitongService.updateById(tradeOrderGaohuitong);

        // 资金业务处理
        //分转元，保留两位小数点
        BigDecimal price = new BigDecimal(respBody.getAmount());
        price = price.multiply(new BigDecimal(0.01)).setScale(2, BigDecimal.ROUND_HALF_UP);
        Map<String, Object> data = new HashMap<>();
        Date paydate = new Date();
        data.put("routeCode", RouteCode.GAOHUITONG.getId());
        data.put("appId", routeConf.getAppId());
        data.put("tradeType", routeConf.getTradeType());
        data.put("openId", tradeOrderGaohuitong.getOpenId());
        data.put("payDate", DateTimeUtil.formatTimestamp2String(paydate, "yyyy-MM-dd"));
        data.put("payTime", DateTimeUtil.formatTimestamp2String(paydate, "HH:mm:ss"));
        data.put("outTradeNo", outTradeNo);
        data.put("bankTradeNo", head.getPayMsgId());
        data.put("fundBank", respBody.getBankCode());
        data.put("price", price);
        data.put("returnCode", "10000");
        data.put("returnMsg", "交易成功");
        DepositProcHandler handler = (DepositProcHandler) SpringContextHolder.getBean(RouteCode.GAOHUITONG.getId().toLowerCase() + ConstEC.DEPOSITPROCHANDLER);
        handler.proc(data);

        responseMsg.put("bindId", respBody.getBindId());
        responseMsg.put("amount", respBody.getAmount());
        responseMsg.put("bindValid", respBody.getBindValid());
        responseMsg.put("bankCardNo", respBody.getBankCardNo());
        responseMsg.put("bankCode", respBody.getBankCode());
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        log.info("|快捷支付|高汇通|快捷支付确认|响应参数：{}", JSON.toJSONString(responseMsg));
    }

    /**
     * 支付短信 【高汇通：支付短验发送】
     *
     * @param requestMsg  userOperNo    用户操作号
     *                    smsOrderNo    (原订单号)
     *                    childMerchantId   (子商户号，如果为一户一码模式则必填)
     *                    mercId    商城商户号
     *                    platform  平台编号 名品猫:MPMALL，合伙人：PTMALL ....
     *                    tradeType 交易类型 JSAPI：公众号或小程序支付 APP：app支付 NATIVE：扫码支付 MICROPAY：刷卡支付 MWEB：H5支付
     * @param responseMsg
     * @throws Exception
     */
    @Override
    public void smsOrder(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        log.info("|快捷支付|高汇通|支付短信|请求参数：{}",JSON.toJSONString(requestMsg));
        //校验下加密数据
        RequestMsgUtil.validateRequestMsg(requestMsg);
        String mercId = (String) requestMsg.get("mercId");
        String platform = (String) requestMsg.get("platform");
        String userOperNo = (String) requestMsg.get("userOperNo");
        String outTradeNo = (String) requestMsg.get("outTradeNo");
        String agencyId = null;
        if (requestMsg.get("agencyId") != null) {
            agencyId = (String) requestMsg.get("agencyId");
        }
        //路由配置
        RouteConf routeConf = RequestMsgUtil.getRouteConf(mercId, platform, TradeType.QUICK.getId(), GaohuitongConstants.GHT_ROUTE, agencyId);
        Route route = routeService.getOne(new QueryWrapper<Route>().eq("route_code", GaohuitongConstants.GHT_ROUTE));

        // 请求报文封装
        GHTReq<GHTSmsOrderDTO> req = new GHTReq<>();
        GHTResp<GHTSmsOrderDTO> resp = new GHTResp<>();
        GHTSmsOrderDTO smsOrderDTO = new GHTSmsOrderDTO();
        smsOrderDTO.setUserId(userOperNo);
        smsOrderDTO.setOriReqMsgId(outTradeNo);
        String reqMsgId = DateTimeUtil.date8() + seqIncrService.nextVal(SeqIncrType.REQUEST_ID_GAOHUITONG.getId(), SeqIncrType.REQUEST_ID_GAOHUITONG.getLength(), Align.LEFT);
        req.setHead(buildHead(GaohuitongConstants.MESSAGE_PROPERTY_MSGTYPE_MERCHANT, GaohuitongConstants.GHT_QUICK_SMS_ORDER, reqMsgId, routeConf.getBankMercId()));
        req.setBody(smsOrderDTO);

        // 发送请求
        log.info("|快捷支付|高汇通|支付短信|请求高汇通，请求参数：{}", JSON.toJSONString(req));
        sendRequest(req, resp, GaohuitongConstants.GHT_QUICK_SMS_ORDER, commonUrl, routeConf, GHTSmsOrderDTO.class, route.getCallbackUrl());
        log.info("|快捷支付|高汇通|支付短信|请求高汇通成功，响应参数：{}", JSON.toJSONString(resp));

        // 响应处理
        GHTHeadDTO respHead = resp.getHead();
        String respCode = respHead.getRespCode();
        String respMsg = respHead.getRespMsg();
        if (!GaohuitongConstants.RETURN_CODE_SUCCESS.equals(respCode)) {
            log.error("|快捷支付|高汇通|支付短信|交易失败,高汇通响应码:{},高汇通响应信息:{}", respCode, respMsg);
            throw new BusiException(11001);
        }
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        responseMsg.setAttr("smsRequestId", reqMsgId);
        log.info("|快捷支付|高汇通|支付短信|响应参数：{}", JSON.toJSONString(responseMsg));
    }

    @Override
    public ResponseMsg queryOrder(RequestMsg requestMsg) throws Exception {
        ResponseMsg responseMsg = new ResponseMsg();
        String mercId = (String) requestMsg.get("mercId");
        String platform = (String) requestMsg.get("platform");
        String tradeType = (String) requestMsg.get("tradeType");

        TradeOrderGaohuitong tradeOrderGaohuitong = tradeOrderGaohuitongService.getOne(new QueryWrapper<TradeOrderGaohuitong>().eq("out_trade_no", requestMsg.get("outTradeNo")));
        //失败、预登记都不继续
        if (null == tradeOrderGaohuitong || OrderStatus.FAIL.getId().equals(tradeOrderGaohuitong.getOrderStatus())|| OrderStatus.ADVANCE.getId().equals(tradeOrderGaohuitong.getOrderStatus())) {
            responseMsg.put(ConstEC.RETURNCODE, "11001");
            responseMsg.put(ConstEC.RETURNMSG, ApplicationYmlUtil.get("11001"));
            return responseMsg;
        }
        //路由配置
        RouteConf routeConf = RequestMsgUtil.getRouteConf(mercId, platform, tradeType, GaohuitongConstants.GHT_ROUTE, null);
        //路由信息
        Route route = routeService.getOne(new QueryWrapper<Route>().eq("route_code", GaohuitongConstants.GHT_ROUTE));

        GHTReq<GHTQueryOrderDTO> req = new GHTReq<>();
        GHTResp<GHTQueryOrderDTO> resp = new GHTResp<>();
        GHTQueryOrderDTO queryOrderDTO = new GHTQueryOrderDTO();
        queryOrderDTO.setOriReqMsgId(tradeOrderGaohuitong.getOutTradeNo());

        String reqMsgId = DateTimeUtil.date8() + seqIncrService.nextVal(SeqIncrType.REQUEST_ID_GAOHUITONG.getId(), SeqIncrType.REQUEST_ID_GAOHUITONG.getLength(), Align.LEFT);
        req.setHead(buildHead(GaohuitongConstants.MESSAGE_PROPERTY_MSGTYPE_MERCHANT, GaohuitongConstants.GHT_QUICK_QUERY_ORDER, reqMsgId, routeConf.getBankMercId()));
        req.setBody(queryOrderDTO);

        // 发送请求
        log.info("|快捷支付|高汇通|订单查询|请求高汇通，请求参数：{}", JSON.toJSONString(req));
        sendRequest(req, resp, GaohuitongConstants.GHT_QUICK_QUERY_ORDER, commonUrl, routeConf, GHTSmsOrderDTO.class, route.getCallbackUrl());
        log.info("|快捷支付|高汇通|订单查询|请求高汇通成功，响应参数：{}", JSON.toJSONString(resp));

        // 响应处理
        GHTHeadDTO respHead = resp.getHead();
        String respCode = respHead.getRespCode();
        String respMsg = respHead.getRespMsg();
        if (!GaohuitongConstants.RETURN_CODE_SUCCESS.equals(respCode)) {
            log.error("|快捷支付|高汇通|订单查询|交易失败,高汇通响应码:{},高汇通响应信息:{}", respCode, respMsg);
            throw new BusiException(11001);
        }

        queryOrderDTO = resp.getBody();
        String tradeStatus = queryOrderDTO.getOriRespType();

        if (GaohuitongConstants.RESPTYPE_S.equals(tradeStatus)) {
            Date payTime = DateTimeUtil.formatStringToDate(queryOrderDTO.getPayedDate(), "yyyyMMddHHmmss");

            //分转元，保留两位小数点
            BigDecimal price = new BigDecimal(queryOrderDTO.getOriAmount());
            price = price.multiply(new BigDecimal(0.01)).setScale(2, BigDecimal.ROUND_HALF_UP);

            Map<String, Object> data = new HashMap<String, Object>();
            data.put("routeCode", RouteCode.GAOHUITONG.getId());
            data.put("payDate", DateTimeUtil.formatTimestamp2String(payTime, "yyyy-MM-dd"));
            data.put("payTime", DateTimeUtil.formatTimestamp2String(payTime, "HH:mm:ss"));
            data.put("outTradeNo", tradeOrderGaohuitong.getOutTradeNo());
            data.put("openId", tradeOrderGaohuitong.getOpenId());
            data.put("bankTradeNo", respHead.getPayMsgId());
            data.put("price", price);
            data.put("returnCode", "10000");
            data.put("returnMsg", "交易成功");

            DepositProcHandler handler = (DepositProcHandler) SpringContextHolder.getBean(RouteCode.GAOHUITONG.getId().toLowerCase() + ConstEC.DEPOSITPROCHANDLER);
            handler.proc(data);
        }

        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        return responseMsg;
    }

    @Override
    public ResponseMsg refundOrder(RequestMsg requestMsg) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseMsg queryRefundOrder(RequestMsg requestMsg) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 签约短信 【高汇通：绑卡短信请求接口】
     *
     * @param requestMsg  userOperNo    用户操作号(商户用户标识)
     *                    childMerchantId   (子商户号，如果为一户一码模式则必填)
     *                    smsOrderNo    (原订单号)
     *                    mercId    商城商户号
     *                    platform  平台编号 名品猫:MPMALL，合伙人：PTMALL ....
     *                    tradeType 交易类型 JSAPI：公众号或小程序支付 APP：app支付 NATIVE：扫码支付 MICROPAY：刷卡支付 MWEB：H5支付
     * @param responseMsg bindOrderNo (平台绑卡订单号)
     *                    smsSignOrderNo 签约短信请求流水号
     * @throws Exception
     */
    @Override
    public void smsSign(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        // 请求报文封装
        log.info("|快捷支付|高汇通|签约短信|请求参数：{}", JSON.toJSONString(requestMsg));
        String userOperNo = (String) requestMsg.get("userOperNo");
        String mercId = (String) requestMsg.get("mercId");
        String platform = (String) requestMsg.get("platform");

        //路由配置
        RouteConf routeConf = RequestMsgUtil.getRouteConf(mercId, platform, TradeType.QUICK.getId(), GaohuitongConstants.GHT_ROUTE, null);
        //路由信息
        Route route = routeService.getOne(new QueryWrapper<Route>().eq("route_code", GaohuitongConstants.GHT_ROUTE));

        GHTReq<GHTSmsSignDTO> req = new GHTReq<>();
        GHTResp<GHTSmsSignDTO> resp = new GHTResp<>();
        GHTSmsSignDTO smsSignDTO = new GHTSmsSignDTO();
        smsSignDTO.setUserId(userOperNo);
        smsSignDTO.setTerminalId(routeConf.getAppId());
        smsSignDTO.setOriReqMsgId((String) requestMsg.get("smsOrderNo"));
        String reqMsgId = DateTimeUtil.date8() + seqIncrService.nextVal(SeqIncrType.REQUEST_ID_GAOHUITONG.getId(), SeqIncrType.REQUEST_ID_GAOHUITONG.getLength(), Align.LEFT);
        req.setHead(buildHead(GaohuitongConstants.MESSAGE_PROPERTY_MSGTYPE_MERCHANT, GaohuitongConstants.GHT_QUICK_SMS_SIGN, reqMsgId, routeConf.getBankMercId()));
        req.setBody(smsSignDTO);
        // 发送请求
        sendRequest(req, resp, GaohuitongConstants.GHT_QUICK_SMS_SIGN, commonUrl, routeConf, GHTSmsSignDTO.class, route.getCallbackUrl());
        verifyResp(resp.getHead());
        // 响应处理
        GHTSmsSignDTO respBody = resp.getBody();
        String bindOrderNo = respBody.getBindOrderNo();
        responseMsg.setAttr("bindOrderNo", bindOrderNo);
        responseMsg.setAttr("smsRequestId", reqMsgId);
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        log.info("|快捷支付|高汇通|签约短信|响应参数：{}", JSON.toJSONString(responseMsg));
    }

    /**
     * @param :[requestMsg, responseMsg]
     * @return :void
     * @Description(描述): 鉴权绑卡
     * @auther: Jack Lin
     * @date: 2019/9/11 15:37
     */
    @Override
    public void quickSign(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        log.info("|快捷支付|高汇通|鉴权绑卡|请求参数：{}", JSON.toJSONString(requestMsg));
        //校验下加密数据
        RequestMsgUtil.validateRequestMsg(requestMsg);

        String userOperNo = (String) requestMsg.get("userOperNo");
        String mobile = cipherServiceImpl.decryptAES((String) requestMsg.get("mobile"));
        String bankCardNo = cipherServiceImpl.decryptAES((String) requestMsg.get("bankCardNo"));
        String bankCardType = (String) requestMsg.get("bankCardType");
        String accountName = (String) requestMsg.get("bankCardName");
        String cardNo = cipherServiceImpl.decryptAES((String) requestMsg.get("cardNo"));
        String cardType = (String) requestMsg.get("cardType");
        String mercId = (String) requestMsg.get("mercId");
        String platform = (String) requestMsg.get("platform");

        //过滤非身份证
        if(CardType.ID_CARD.getId() != Integer.parseInt(cardType)){
            log.error("quickSign 快捷签约 只支持身份证, cardType：{} ", cardType);
            throw new BusiException(31118);
        }
        //实名验证下
        Map<String, Object> map = aliResourcesService.realName(accountName, cardNo);
        String status = (String) map.get("status");
        if (null == status || !"01".equals(status)) {
            log.error("quickSign 实名验证失败, name：{},cardNo：{}", accountName, cardNo);
            throw new BusiException(31105);
        }
        RouteDictionary routeDictionary = routeDictionaryService.getOne(new QueryWrapper<RouteDictionary>()
                .eq("route", GaohuitongConstants.GHT_ROUTE)
                .eq("name", CardType.ID_CARD.getId())
                .eq("category", GaohuitongConstants.ROUTE_DICTIONARY_CATEGORY_CERTCODE)
                .last("limit 1"));
        Optional.ofNullable(routeDictionary).orElseThrow(() -> new BusiException(11019));
        //路由配置
        RouteConf routeConf = RequestMsgUtil.getRouteConf(mercId, platform, TradeType.QUICK.getId(), GaohuitongConstants.GHT_ROUTE, null);
        //路由信息
        Route route = routeService.getOne(new QueryWrapper<Route>().eq("route_code", GaohuitongConstants.GHT_ROUTE));

        GHTReq<GHTQuickBindDTO> req = new GHTReq<>();
        GHTResp<GHTQuickBindDTO> resp = new GHTResp<>();
        GHTQuickBindDTO dto = new GHTQuickBindDTO();
        dto.setUserId(userOperNo);
        dto.setAccountName(accountName);
        dto.setBankCardNo(bankCardNo);
        dto.setBankCardType(bankCardType);
        dto.setMobilePhone(mobile);
        dto.setTerminalId(routeConf.getAppId());//终端号
        dto.setCertificateNo(cardNo);//证件
        dto.setCertificateType(routeDictionary.getStrVal());//证件类型

        //信用卡需要
        if (BankCardType.CREDIT.getId().equalsIgnoreCase(bankCardType)) {
            String validDate = cipherServiceImpl.decryptAES((String) requestMsg.get("validDate"));
            String cvn2 = cipherServiceImpl.decryptAES((String) requestMsg.get("cvn2"));
            dto.setValid(validDate);
            dto.setCvn2(cvn2);
        }
        req.setHead(buildHead(GaohuitongConstants.MESSAGE_PROPERTY_MSGTYPE_MERCHANT, GaohuitongConstants.GHT_QUICK_SIGN_BIND, (String) requestMsg.get("smsOrderNo"), routeConf.getBankMercId()));
        req.setBody(dto);
        //发送请求
        sendRequest(req, resp, GaohuitongConstants.GHT_QUICK_SIGN_BIND, commonUrl, routeConf, GHTQuickBindDTO.class, route.getCallbackUrl());
        //校验结果
        verifyResp(resp.getHead());
        GHTQuickBindDTO body = resp.getBody();

        responseMsg.put("needSms", "N");//默认不发短信
        if (GaohuitongConstants.RESPTYPE_R.equalsIgnoreCase(resp.getHead().getRespType())) {
            responseMsg.put("needSms", body.getNeedSms());
        }
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        responseMsg.put("bindAgrNo", StringUtils.isNotEmpty(body.getBindId()) ? body.getBindId() : ""); //银行绑定协议号
        log.info("|快捷支付|高汇通|鉴权绑卡|响应参数：{}", JSON.toJSONString(responseMsg));
    }


    /**
     * 快捷签约确认 【绑卡信息确认接口】
     *
     * @param requestMsg  userOperNo 用户操作号
     *                    bindOrderNo 请求绑卡短信时返回的平台绑卡订单号
     *                    smsSignOrderNo 商户请求绑卡短信时的商户订单号
     *                    smsCode   确认短信验证码
     *                    childMerchantId 子商户号
     *                    mercId    商城商户号
     *                    platform  平台编号 名品猫:MPMALL，合伙人：PTMALL ....
     *                    tradeType 交易类型 APP：app支付 NATIVE：扫码支付 MICROPAY：刷卡支付 MWEB：H5支付 ...
     * @param responseMsg bindId    绑卡ID
     *                    confirmSignOrderNo    [快捷签约确认]请求流水
     * @throws Exception
     */
    @Override
    public void confirmSign(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        log.info("|快捷支付|高汇通|快捷签约确认|请求参数：{}", JSON.toJSONString(requestMsg));
        String userOperNo = (String) requestMsg.get("userOperNo");
        String mercId = (String) requestMsg.get("mercId");
        String platform = (String) requestMsg.get("platform");
        String bindOrderNo = (String) requestMsg.get("bindOrderNo");
        String smsRequestId = (String) requestMsg.get("smsRequestId");
        String smsCode = (String) requestMsg.get("smsCode");

        //路由配置
        RouteConf routeConf = RequestMsgUtil.getRouteConf(mercId, platform, TradeType.QUICK.getId(), GaohuitongConstants.GHT_ROUTE, null);
        //路由信息
        Route route = routeService.getOne(new QueryWrapper<Route>().eq("route_code", GaohuitongConstants.GHT_ROUTE));

        // 请求报文封装
        GHTReq<GHTConfirmSignDTO> req = new GHTReq<>();
        GHTResp<GHTConfirmSignDTO> resp = new GHTResp<>();
        GHTConfirmSignDTO confirmSignDTO = new GHTConfirmSignDTO();
        confirmSignDTO.setUserId(userOperNo);
        confirmSignDTO.setBindOrderNo(bindOrderNo);
        confirmSignDTO.setOriReqMsgId(smsRequestId);
        confirmSignDTO.setValidateCode(smsCode);
        String reqMsgId = DateTimeUtil.date8() + seqIncrService.nextVal(SeqIncrType.REQUEST_ID_GAOHUITONG.getId(), SeqIncrType.REQUEST_ID_GAOHUITONG.getLength(), Align.LEFT);
        req.setHead(buildHead(GaohuitongConstants.MESSAGE_PROPERTY_MSGTYPE_MERCHANT, GaohuitongConstants.GHT_QUICK_CONFIRM_SIGN, reqMsgId, routeConf.getBankMercId()));
        req.setBody(confirmSignDTO);

        // 发送请求
        sendRequest(req, resp, GaohuitongConstants.GHT_QUICK_CONFIRM_SIGN, commonUrl, routeConf, GHTConfirmSignDTO.class, route.getCallbackUrl());
        // 响应处理
        GHTHeadDTO respHead = resp.getHead();
        GHTConfirmSignDTO respBody = resp.getBody();
        String respCode = respHead.getRespCode();
        String respMsg = respHead.getRespMsg();
        if (!GaohuitongConstants.RETURN_CODE_SUCCESS.equals(respCode)) {
            log.error("|快捷支付|高汇通|快捷签约确认|交易失败,高汇通响应码:{},高汇通响应信息:{}", respCode, respMsg);
            throw new BusiException(11001);
        }
        String bindId = respBody.getBindId();
        responseMsg.setAttr("bindAgrNo", bindId);
        responseMsg.setAttr("confirmSignOrderNo", reqMsgId);
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        log.info("|快捷支付|高汇通|快捷签约确认|响应参数：{}", JSON.toJSONString(responseMsg));
    }

    @Override
    public void quickCancel(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void smsCancel(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public ResponseMsg querySign(RequestMsg requestMsg) throws Exception {
        return null;
    }

    /**
     * 查询银行卡信息 【高汇通：银行卡信息查询】
     *
     * @param requestMsg  bankCardNo  银行卡号
     *                    mercId    商城商户号
     *                    platform  平台编号 名品猫:MPMALL，合伙人：PTMALL ....
     *                    tradeType 交易类型 APP：app支付 NATIVE：扫码支付 MICROPAY：刷卡支付 MWEB：H5支付 ...
     * @param responseMsg
     * @throws Exception
     */
    @Override
    public void queryCardBind(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        log.info("|快捷支付|高汇通|银行卡信息查询|请求参数：{}", JSON.toJSONString(requestMsg));
        String mercId = (String) requestMsg.get("mercId");
        String platform = (String) requestMsg.get("platform");
        String bankCardNo = (String) requestMsg.get("bankCardNo");
        String tradeCode = (String) requestMsg.get("tradeCode");

        //路由配置
        RouteConf routeConf = RequestMsgUtil.getRouteConf(mercId, platform, TradeType.QUICK.getId(), GaohuitongConstants.GHT_ROUTE, null);
        //路由信息
        Route route = routeService.getOne(new QueryWrapper<Route>().eq("route_code", GaohuitongConstants.GHT_ROUTE));


        // 请求报文封装
        GHTReq<GHTQueryCardBindDTO> req = new GHTReq<>();
        GHTResp<GHTQueryCardBindDTO> resp = new GHTResp<>();
        GHTQueryCardBindDTO queryCardBindDTO = new GHTQueryCardBindDTO();
        queryCardBindDTO.setBankCardNo(bankCardNo);
        String reqMsgId = DateTimeUtil.date8() + seqIncrService.nextVal(SeqIncrType.REQUEST_ID_GAOHUITONG.getId(), SeqIncrType.REQUEST_ID_GAOHUITONG.getLength(), Align.LEFT);
        req.setHead(buildHead(GaohuitongConstants.MESSAGE_PROPERTY_MSGTYPE_MERCHANT, GaohuitongConstants.GHT_QUICK_QUERY_CARD_BIND, reqMsgId, routeConf.getBankMercId()));
        req.setBody(queryCardBindDTO);

        // 发送请求
        sendRequest(req, resp, GaohuitongConstants.GHT_QUICK_QUERY_CARD_BIND, commonUrl, routeConf, GHTQueryCardBindDTO.class, route.getCallbackUrl());
        verifyResp(resp.getHead());
        // 响应处理
        GHTQueryCardBindDTO respBody = resp.getBody();
        String ghtBankCardType = respBody.getBankCardType();
        String ghtBankCode = respBody.getBankCode();

        if (null != ghtBankCardType && tradeCode.equals(TradeCode.WITHDRAW.getId()) && !BankCardType.DEBIT.getId().equals(ghtBankCardType) ) {
        	throw new BusiException(11126, ApplicationYmlUtil.get("11126"));
        }

        // 先查数据字典
        RouteDictionary routeDictionary = routeDictionaryService.getOne(new QueryWrapper<RouteDictionary>()
                .eq("route", GaohuitongConstants.GHT_ROUTE)
                .eq("category", GaohuitongConstants.ROUTE_DICTIONARY_CATEGORY_BANKCODE)
                .eq("str_val", ghtBankCode));
        Optional.ofNullable(routeDictionary).orElseThrow(() -> {
            log.error("|快捷支付|高汇通|银行卡信息查询|RouteDictionary未配置的bankCode关联关系", respBody.toString());
            return new BusiException(31111, ApplicationYmlUtil.get("31111").replace("$", respBody.getBankName()));
        });
        
        //再查银行路由
        BankRoute bankRoute = iBankRouteService.getOne(new QueryWrapper<BankRoute>()
                .eq("trade_code", tradeCode)
                .eq("route_code", GaohuitongConstants.GHT_ROUTE)
                .eq("bank_code", routeDictionary.getName())
                .eq("bank_card_type", respBody.getBankCardType())
                .last("limit 1")
        );
        Optional.ofNullable(bankRoute).orElseThrow(() -> {
            log.error("|快捷支付|高汇通|银行卡信息查询|BankRoute未配置的bankCode关联关系,tradeCode:{},bankCode:{}", tradeCode, routeDictionary.getName());
            return new BusiException(31116);
        });

        //查银行表
        Bank bank = bankService.getOne(new QueryWrapper<Bank>().eq("bank_code", routeDictionary.getName()));
        Optional.ofNullable(bank).orElseThrow(() -> {
            log.error("|快捷支付|高汇通|银行卡信息查询|Bank无对应的bankCode:{}", routeDictionary.getName());
            return new BusiException(15012);
        });

        responseMsg.setAttr("bankCode", bank.getBankCode());
        responseMsg.setAttr("bankName", bank.getBankName());
        responseMsg.setAttr("bankAbbr", bank.getBankAbbr());
        responseMsg.setAttr("bankCardType", ghtBankCardType);
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        log.info("|快捷支付|高汇通|银行卡信息查询|响应参数：{}", JSON.toJSONString(responseMsg));
    }

    /**
     * @Description(描述): 统一处理请求
     * @auther: Jack Lin
     * @date: 2019/9/7 17:31
     */
    public GHTResp sendRequest(GHTReq req, GHTResp resp, String tranCode, String url, RouteConf routeConf, Class clazz, String callbackUrl) throws Exception {
        String reqMsg = req.toXml(false);

        //AES key
        String keyStr = getRandom(16);
        PublicKey publicKey = GaohuitongMessgeUtil.getPublicKey(routeConf.getPublicKeyPath());
        PrivateKey privateKey = GaohuitongMessgeUtil.getPrivateKey(routeConf.getKeyPath());
        //组装请求参数
        Map<String, Object> msgMap = GaohuitongMessgeUtil.requestHandle(routeConf.getBankMercId(), reqMsg, keyStr, publicKey, privateKey, tranCode, callbackUrl);
        String reqMsgStr = Sign.getPlainURLEncoder(msgMap, ConstEC.ENCODE_UTF8);
        String s1 = address + url + "?" + reqMsgStr;
        long l = System.currentTimeMillis();
        log.info(" call_ght： url：{}，request:{}", url, JSON.toJSONString(msgMap));
        log.info("call_ght： tranCode：{}， xml：{}",  tranCode, reqMsg);
        String response = HttpClientUtil.httpsRequest(s1, HttpClientUtil.HTTP_REQUESTMETHOD_POST, null);
        if (StringUtils.isEmpty(response)) {
            throw new BusiException(13110);
        }
        //解析响应
        Map map = GaohuitongMessgeUtil.parseMap(response);
        String s = GaohuitongMessgeUtil.responseHandle(map, keyStr, publicKey, privateKey);

        log.info("call_ght：url：{}，costTime：{}ms， response:{}", s1, System.currentTimeMillis() - l, s);
        if (StringUtils.isEmpty(s)) {
            throw new BusiException(13110);
        }
        JSONObject jsonObject = XmlUtil.xml2Json(s);
        JSONObject body = jsonObject.getJSONObject("body");
        JSONObject head = jsonObject.getJSONObject("head");
        Optional.ofNullable(head).ifPresent(h -> {
            resp.setHead(JSON.parseObject(h.toJSONString(), GHTHeadDTO.class));
        });
        Optional.ofNullable(body).ifPresent(h -> {
            resp.setBody(JSON.parseObject(h.toJSONString(), clazz));
        });

        String s2 = resp.toXml(false);
        log.info("call_ght ：tranCode：{}，response:{}", tranCode, s2);
        return resp;
    }

    public GHTHeadDTO buildHead(String msgType, String tranCode, String reqMsgId, String agencyId) throws Exception {
        GHTHeadDTO dto = new GHTHeadDTO();
        dto.setVersion(version);
        dto.setAgencyId(agencyId);
        dto.setMsgType(msgType);
        dto.setTranCode(tranCode);
        dto.setReqMsgId(reqMsgId);
        dto.setReqDate(DateUtil.dateFormat(new Date(), DateUtil.DATEFORMAT_9));
        return dto;
    }

    /**
     * 统一校验下结果
     *
     * @param headDTO
     * @throws Exception
     */
    public void verifyResp(GHTHeadDTO headDTO) throws Exception {
        Optional.ofNullable(headDTO).ifPresent(s -> {
            //暂时改为自己的错误码
            if(GaohuitongConstants.ERROR_10000X.equalsIgnoreCase(s.getRespCode())){
                s.setRespCode("99998");
            }
            if (GaohuitongConstants.RETURN_CODE_100007.equals(s.getRespCode())) {
                throw new BusiException(31023);
            }
            if (GaohuitongConstants.RETURN_CODE_100008.equals(s.getRespCode())) {
                throw new BusiException(31117);
            }
            if (!(GaohuitongConstants.RETURN_CODE_SUCCESS.equals(s.getRespCode()) || GaohuitongConstants.RETURN_MSG_SUCCESS.equalsIgnoreCase(s.getRespMsg()))) {
                log.error("call_ght：快捷： 失败:{}", s.getRespMsg());
                throw new BusiException(s.getRespCode(),s.getRespMsg());
            }
        });
    }

}
