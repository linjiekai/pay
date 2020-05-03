package com.mppay.gateway.handler.withdr;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.*;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.Sign;
import com.mppay.core.sign.gaohuitong.GaohuitongConstants;
import com.mppay.core.sign.gaohuitong.GaohuitongMessgeUtil;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.core.utils.DateUtil;
import com.mppay.core.utils.HttpClientUtil;
import com.mppay.core.utils.XmlUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.dto.platform.gaohuitong.*;
import com.mppay.gateway.handler.DepositProcHandler;
import com.mppay.gateway.handler.PlatformBusiHandler;
import com.mppay.gateway.handler.WithdrOrderBusiHandler;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.mppay.core.utils.StringUtil.getRandom;


/**
 * 高汇通-提现业务
 */
@Service("gaohuitongWithdrOrderBusiHandler")
@Slf4j
public class GaohuitongWithdrOrderBusiHandlerImpl implements WithdrOrderBusiHandler {

    @Value("${ght.xfhl.version}")
    private String version;
    @Value("${ght.xfhl.address}")
    private String address;

    // 提现绑卡(高汇通_商户入驻_商户银行卡基础信息登记)
    @Value("${gaohuitong.interfaceWeb.bankInfo}")
    private String bankInfoUrl;
    //转账
    @Value("${gaohuitong.interfaceWeb.transfer}")
    private String transferUrl;
    //代付
    @Value("${gaohuitong.interfaceWeb.realTimeDF}")
    private String realTimeDF;
    //代付结果查询
    @Value("${gaohuitong.interfaceWeb.queryResultDF}")
    private String resultDF;

    @Autowired
    private IUserAgrInfoService userAgrInfoService;
    @Autowired
    private IUserRealNameDetailsService userRealNameDetailsService;
    @Autowired
    private IRouteDictionaryService routeDictionaryService;
    @Autowired
    private ICardBindService cardBindService;
    @Autowired
    private ISeqIncrService seqIncrService;
    @Autowired
    private IWithdrOrderService withdrOrderService;
    @Autowired
    private IWithdrOrderGaohuitongService iWithdrOrderGaohuitongService;
    @Autowired
    private PlatformBusiHandler gaohuitongPlatformBusiHandler;
    @Autowired
    private IUserAgrInfoService iUserAgrInfoService;
    @Autowired
    private ICipherService cipherServiceImpl;

    @Override
    public ResponseMsg getOutMercInfo(RequestMsg requestMsg) throws Exception {

        UserAgrInfo userAgrInfo = iUserAgrInfoService.getOne(new QueryWrapper<UserAgrInfo>()
                .eq("route_code", GaohuitongConstants.GHT_ROUTE)
                .eq("user_oper_no", requestMsg.get("userOperNo"))
                .eq("status", UserAgrInfoStatus.NORMAL.getId()));
        if (null == userAgrInfo) {
            log.error("用户协议信息不存在" + requestMsg.toString());
            throw new BusiException(15003);
        }

        ResponseMsg responseMsg = new ResponseMsg();
        responseMsg.put("bankMercId", userAgrInfo.getOutMercId());
        responseMsg.put("orgNo", userAgrInfo.getOrgNo());
        responseMsg.put("terminalNo", userAgrInfo.getTerminalNo());
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
        log.info("|高汇通|商户银行卡信息登记|参数：{}",JSON.toJSONString(requestMsg));
        //校验下数据
        RequestMsgUtil.validateRequestMsg(requestMsg);
        ResponseMsg responseMsg = new ResponseMsg();
        //先查下有没有绑过卡
        gaohuitongPlatformBusiHandler.queryCardBind(requestMsg, responseMsg);
        if (responseMsg.get(ConstEC.RETURNCODE) != null && ConstEC.SUCCESS_10000.equalsIgnoreCase((String) responseMsg.get(ConstEC.RETURNCODE))) {
            String authResult = (String) responseMsg.get("authResult");
            //1才是绑卡成功的
            if ("1".equalsIgnoreCase(authResult)) {
                return responseMsg;
            }
        }

        String mobile = (String) requestMsg.get("mobile");
        mobile = cipherServiceImpl.decryptAES(mobile);
        String bankCardNo = (String) requestMsg.get("bankCardNo");
        bankCardNo = cipherServiceImpl.decryptAES(bankCardNo);
        String bankCardType = (String) requestMsg.get("bankCardType");
        // 实名校验
        Integer cardType = (Integer) requestMsg.get("cardType");
        String bankCardName = (String) requestMsg.get("bankCardName");

        // 商户号获取
        UserAgrInfo userAgrInfo = userAgrInfoService.getOne(new QueryWrapper<UserAgrInfo>()
                .eq("route_code", GaohuitongConstants.GHT_ROUTE)
                .eq("user_oper_no", requestMsg.get("userOperNo"))
                .eq("status", UserAgrInfoStatus.NORMAL.getId()));
        Optional.ofNullable(userAgrInfo).orElseThrow(() -> new BusiException(30001));
        String outMercId = userAgrInfo.getOutMercId();
        String agencyId = userAgrInfo.getOrgNo();

        // bankCode 转换为 高汇通专用 bankCode
        String bankCode = (String) requestMsg.get(GaohuitongConstants.ROUTE_DICTIONARY_CATEGORY_BANKCODE);
        RouteDictionary routeDictionary = routeDictionaryService.getOne(new QueryWrapper<RouteDictionary>()
                .eq("route", GaohuitongConstants.GHT_ROUTE)
                .eq("category", GaohuitongConstants.ROUTE_DICTIONARY_CATEGORY_BANKOPEN)
                .eq("name", bankCode));

        // 封装请求报文 head + body
        GHTHeadDTO headDTO = buildHead(agencyId, GaohuitongConstants.MESSAGE_PROPERTY_MSGTYPE_MERCHANT, GaohuitongConstants.GHT_BANKINFO_TRANCODE);
        GHTBankInfoRegisterDTO bankInfoRegisterDTO = new GHTBankInfoRegisterDTO();

        bankInfoRegisterDTO.setMerchantId(outMercId);
        bankInfoRegisterDTO.setMobileNo(mobile);
        bankInfoRegisterDTO.setMobileNo2(mobile);
        bankInfoRegisterDTO.setHandleType("0"); // 操作类型，0：新增 1：删除 2：修改
        bankInfoRegisterDTO.setBankCode(routeDictionary.getStrVal());
        bankInfoRegisterDTO.setBankaccProp(GaohuitongConstants.MESSAGE_PROPERTY_BANKACCPROP_PERSONAL);
        bankInfoRegisterDTO.setName(bankCardName);
        bankInfoRegisterDTO.setBankaccountNo(bankCardNo);
        bankInfoRegisterDTO.setBankaccountType(Integer.valueOf(bankCardType));
        bankInfoRegisterDTO.setCertCode(cardType);
        bankInfoRegisterDTO.setCertNo((String) requestMsg.get("certNo"));

        GHTReq<GHTBankInfoRegisterDTO> registerDTOGHTReq = new GHTReq<>();
        GHTResp<GHTBankInfoRegisterDTO> ghtResp = new GHTResp();
        registerDTOGHTReq.setHead(headDTO);
        registerDTOGHTReq.setBody(bankInfoRegisterDTO);

        // 获取路由关联信息
        RouteConf routeConf = RequestMsgUtil.getRouteConf((String) requestMsg.get("mercId"), (String) requestMsg.get("platform"), TradeType.QUICK.getId(), GaohuitongConstants.GHT_ROUTE, agencyId);
        //请求等级银行卡
        sendRequest(registerDTOGHTReq, ghtResp, GaohuitongConstants.GHT_BANKINFO_TRANCODE, bankInfoUrl, routeConf, GHTBankInfoRegisterDTO.class);
        verifyResp(ghtResp.getHead());
        //登记完查下卡实名状态
        gaohuitongPlatformBusiHandler.queryCardBind(requestMsg, responseMsg);
        String authResult = (String) responseMsg.get("authResult");
        if (!"1".equalsIgnoreCase(authResult)) {
            log.info("|高汇通|商户银行卡信息登记|银行卡实名状态为：{}",authResult);
            throw new BusiException(31105);
        }

        // 根据 card_bind.id 更新卡绑定状态：已绑定
        Long cardBindId = (Long) requestMsg.get("cardBindId");
        CardBind cardBind = cardBindService.getById(cardBindId);
        if (cardBind == null) {
            log.error("|高汇通|商户银行卡信息登记|卡绑定数据不存在|");
            throw new BusiException(15003);
        }
        cardBind.setStatus(CardBindStatus.BINDING.getId());
        cardBindService.updateById(cardBind);

        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        log.info("|高汇通|商户银行卡信息登记|响应：{}",JSON.toJSONString(responseMsg));
        return responseMsg;
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
        return null;
    }

    /**
     * 转账 ，机构转子商户
     * 代付
     *
     * @param requestMsg
     * @return
     */
    @Override
    public ResponseMsg withdrOrder(RequestMsg requestMsg) throws Exception {
        //校验下数据
        RequestMsgUtil.validateRequestMsg(requestMsg);

        ResponseMsg responseMsg = new ResponseMsg();
        String withdrOrderNo = (String) requestMsg.get("withdrOrderNo"); // 提现订单
        String bankCode = (String) requestMsg.get("bankCode");
        String routeCode = (String) requestMsg.get("routeCode");
        String bankCardNo = (String) requestMsg.get("bankCardNo");
        String userOperNo = (String) requestMsg.get("userOperNo");
        String mercId = (String) requestMsg.get("mercId");
        String bankCardName = (String) requestMsg.get("bankCardName");
        String bankMercId = (String) requestMsg.get("bankMercId");

        //先创建流水
        WithdrOrderGaohuitong orderGaohuitong = iWithdrOrderGaohuitongService.getOne(new QueryWrapper<WithdrOrderGaohuitong>().eq("withdr_order_no", withdrOrderNo).notIn("order_status", WithdrOrderStatus.FAIL.getId()));
        if (null == orderGaohuitong) {
            orderGaohuitong = new WithdrOrderGaohuitong();
            BeanUtils.populate(orderGaohuitong, requestMsg.getMap());
            String outTradeNo = DateTimeUtil.date8() + seqIncrService.nextVal(SeqIncrType.OUT_WITHDR_NO_GAOHUITONG.getId(), SeqIncrType.OUT_WITHDR_NO_GAOHUITONG.getLength(), Align.LEFT);
            orderGaohuitong.setOutTradeNo(outTradeNo);
            orderGaohuitong.setOrderDate(DateTimeUtil.date10());
            orderGaohuitong.setOrderTime(DateTimeUtil.time8());
            //提现订单加1元手续费
            orderGaohuitong.setPrice(orderGaohuitong.getPrice());
            orderGaohuitong.setServicePrice(new BigDecimal(1));
            iWithdrOrderGaohuitongService.save(orderGaohuitong);
        }

        //成功订单
        if (orderGaohuitong.getOrderStatus().equals(OrderStatus.SUCCESS.getId())) {
            log.info("订单 {} 提现状态已成功：{}", orderGaohuitong.getWithdrOrderNo(), orderGaohuitong.getOrderStatus());
            responseMsg.put("outTradeNo", orderGaohuitong.getOutTradeNo());
            responseMsg.put("bankWithdrNo", orderGaohuitong.getBankWithdrNo());
            responseMsg.put("orderStatus", orderGaohuitong.getOrderStatus());
            responseMsg.put("routeCode", orderGaohuitong.getRouteCode());
            responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
            responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
            return responseMsg;
        }
        //获取绑卡信息
        WithdrOrder withdrOrder = withdrOrderService.getOne(new QueryWrapper<WithdrOrder>().eq("withdr_order_no", withdrOrderNo));
        CardBind cardBind = cardBindService.getOne(new QueryWrapper<CardBind>().eq("agr_no", withdrOrder.getAgrNo()).eq("merc_id", mercId).eq("status",1));
        Optional.ofNullable(cardBind).orElseThrow(() -> new BusiException(31104));

        //获取路由信息
        RouteConf routeConf = RequestMsgUtil.getRouteConf(mercId, cardBind.getPlatform(), TradeType.QUICK.getId(), GaohuitongConstants.GHT_ROUTE, null);
        RouteDictionary routeDictionary = routeDictionaryService.getOne(new QueryWrapper<RouteDictionary>()
                .eq("category", "bankOpen")
                .eq("route", routeCode.toUpperCase())
                .eq("name", bankCode.toUpperCase())
        );
        UserRealNameDetails userRealNameDetails = userRealNameDetailsService.getOne(new QueryWrapper<UserRealNameDetails>()
                .eq("user_oper_no", userOperNo)
                .eq("card_type", "1")
                .eq("status", "1")
                .last("limit 1"));
        Optional.ofNullable(userRealNameDetails).orElseThrow(() -> new BusiException(11307));

        //幸福引力的提现单已经改为另外渠道提现，这里直接成功
        if (PlatformType.XFYLMALL.getId().equalsIgnoreCase(mercId) || PlatformType.ZBMALL.getId().equalsIgnoreCase(mercId)) {
            orderGaohuitong.setOrderStatus(WithdrOrderStatus.BANK_WAIT.getId());
            //小订单
            iWithdrOrderGaohuitongService.updateById(orderGaohuitong);
        }else{
            //1.转账
            if (WithdrOrderStatus.WAIT.getId().equalsIgnoreCase(orderGaohuitong.getOrderStatus())) {
                ResponseMsg transfer = transfer(bankMercId, orderGaohuitong, routeConf, 3);
                //失败返回去，下次继续转账
                if (!ConstEC.SUCCESS_10000.equals((String) transfer.get(ConstEC.RETURNCODE))) {
                    return transfer;
                }
                //转账成功，状态改为WP
                orderGaohuitong.setBankWithdrNo((String) transfer.get("payMsgId")); //平台支付流水号
                orderGaohuitong.setOrderStatus(WithdrOrderStatus.WAIT_PAY.getId());
                boolean b = iWithdrOrderGaohuitongService.updateById(orderGaohuitong);
                if (!b) {
                    //不成功人工干预
                    log.error("更新提现订单状态失败，outTradeNo：{}，withdrOrderNo：{}", orderGaohuitong.getOutTradeNo(), orderGaohuitong.getWithdrOrderNo());
                    throw new BusiException(11123);
                }
            }
            //2.代付
            if (WithdrOrderStatus.WAIT_PAY.getId().equalsIgnoreCase(orderGaohuitong.getOrderStatus())) {
                ResponseMsg payByDF = payByDF(bankMercId, bankCardName, bankCardNo, routeDictionary, orderGaohuitong, userRealNameDetails, routeConf);
                //失败放回去，下次继续代付
                if (!ConstEC.SUCCESS_10000.equals((String) payByDF.get(ConstEC.RETURNCODE))) {
                    return payByDF;
                }
            }

        }
        responseMsg.put("outTradeNo", orderGaohuitong.getOutTradeNo());
        responseMsg.put("bankWithdrNo", orderGaohuitong.getBankWithdrNo());
        responseMsg.put("orderStatus", orderGaohuitong.getOrderStatus());
        responseMsg.put("routeCode", orderGaohuitong.getRouteCode());
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        return responseMsg;
    }


    /**
     * 代付结果查询
     *
     * @param requestMsg
     * @return
     */
    @Override
    public ResponseMsg queryWithdrOrder(RequestMsg requestMsg) throws Exception {
        //校验下数据
        RequestMsgUtil.validateRequestMsg(requestMsg);

        String bankCode = (String) requestMsg.get("bankCode");
        String bankCardNo = (String) requestMsg.get("bankCardNo");
        String userOperNo = (String) requestMsg.get("userOperNo");
        String mercId = (String) requestMsg.get("mercId");
        String bankMercId = (String) requestMsg.get("bankMercId");
        String withdrOrderNo = (String) requestMsg.get("withdrOrderNo");

        ResponseMsg responseMsg = new ResponseMsg();
        WithdrOrderGaohuitong orderGaohuitong = iWithdrOrderGaohuitongService.getOne(new QueryWrapper<WithdrOrderGaohuitong>().eq("withdr_order_no", withdrOrderNo).notIn("order_status", WithdrOrderStatus.FAIL.getId()));
        if (OrderStatus.SUCCESS.getId().equalsIgnoreCase(orderGaohuitong.getOrderStatus())) {
            //大订单没更新的更新下
            WithdrOrder withdrOrder = withdrOrderService.getOne(new QueryWrapper<WithdrOrder>().eq("withdr_order_no", withdrOrderNo));
            if (!OrderStatus.SUCCESS.getId().equalsIgnoreCase(withdrOrder.getOrderStatus())) {
                withdrOrder.setOrderStatus(OrderStatus.SUCCESS.getId());
                withdrOrderService.updateById(withdrOrder);
            }
            responseMsg.put("orderStatus", orderGaohuitong.getOrderStatus());
            responseMsg.put("outTradeNo", orderGaohuitong.getOutTradeNo());
            responseMsg.put("bankWithdrNo", orderGaohuitong.getBankWithdrNo());
            responseMsg.put("routeCode", orderGaohuitong.getRouteCode());
            responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
            responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
            return responseMsg;
        }
        
        Date payTime = new Date();
        if (PlatformType.XFYLMALL.getId().equalsIgnoreCase(mercId) || PlatformType.ZBMALL.getId().equalsIgnoreCase(mercId)) {
        	Map<String, Object> data = new HashMap<String, Object>();
            data.put("bankWithdrDate", DateTimeUtil.formatTimestamp2String(payTime, "yyyy-MM-dd"));
            data.put("bankWithdrTime", DateTimeUtil.formatTimestamp2String(payTime, "HH:mm:ss"));
            data.put("orderStatus", WithdrOrderStatus.SUCCESS.getId());
            data.put("outTradeNo", orderGaohuitong.getOutTradeNo());
            data.put("bankWithdrNo", orderGaohuitong.getBankWithdrNo());
            data.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
            data.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        } else {
        	//获取绑卡信息
            CardBind cardBind = cardBindService.getOne(new QueryWrapper<CardBind>()
                    .eq("bank_card_no", bankCardNo)
                    .eq("bank_code", bankCode)
                    .eq("status", CardBindStatus.BINDING.getId())
                    .eq("user_oper_no", userOperNo)
                    .eq("merc_id", mercId)
            );
            Optional.ofNullable(cardBind).orElseThrow(() -> new BusiException(31104));
            //获取路由信息
            RouteConf routeConf = RequestMsgUtil.getRouteConf(mercId, cardBind.getPlatform(), TradeType.QUICK.getId(), GaohuitongConstants.GHT_ROUTE, null);

            GHTReq<GHTResultDFDTO> req = new GHTReq<>();
            GHTResp<GHTResultDFDTO> resp = new GHTResp<>();
            GHTResultDFDTO dto = new GHTResultDFDTO();
            req.setHead(buildHead(routeConf.getBankMercId(), GaohuitongConstants.MESSAGE_PROPERTY_MSGTYPE_MERCHANT, GaohuitongConstants.GHT_RESULTDF_OTHER));
            dto.setUser_id(bankMercId);
            dto.setQuery_sn(orderGaohuitong.getBankWithdrNo());
            req.setBody(dto);
            sendRequest(req, resp, GaohuitongConstants.GHT_RESULTDF_OTHER, resultDF, routeConf, GHTResultDFDTO.class);
            GHTHeadDTO head = resp.getHead();
            log.info("call_ght 提现订单查询结果：{}，withdrOrderNo：{}，outTradeNo：{}", head.getRespMsg(), orderGaohuitong.getBankWithdrNo(), orderGaohuitong.getOutTradeNo());
            //处理中,放回去重新扫
            if (GaohuitongConstants.RESPTYPE_R.equalsIgnoreCase(head.getRespType())) {
                orderGaohuitong.setReturnCode(head.getRespCode());
                orderGaohuitong.setReturnMsg(head.getRespMsg());
                iWithdrOrderGaohuitongService.updateById(orderGaohuitong);
                responseMsg.put(ConstEC.RETURNCODE, head.getRespCode());
                responseMsg.put(ConstEC.RETURNMSG, head.getRespMsg());
                return responseMsg;
            }
            
            payTime = DateTimeUtil.formatStringToDate(head.getReqDate(), DateUtil.DATEFORMAT_9);
            //失败, 改为WP继续走代付流程
            if (GaohuitongConstants.RESPTYPE_E.equalsIgnoreCase(head.getRespType())) {
                orderGaohuitong.setReturnCode(head.getRespCode());
                orderGaohuitong.setReturnMsg(head.getRespMsg());
                orderGaohuitong.setOrderStatus(WithdrOrderStatus.WAIT_PAY.getId());
                iWithdrOrderGaohuitongService.updateById(orderGaohuitong);
                responseMsg.put(ConstEC.RETURNCODE, head.getRespCode());
                responseMsg.put(ConstEC.RETURNMSG, head.getRespMsg());
                return responseMsg;
            }
        }

        //成功了需要处理下
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("bankWithdrDate", DateTimeUtil.formatTimestamp2String(payTime, "yyyy-MM-dd"));
        data.put("bankWithdrTime", DateTimeUtil.formatTimestamp2String(payTime, "HH:mm:ss"));
        data.put("orderStatus", WithdrOrderStatus.SUCCESS.getId());
        data.put("outTradeNo", orderGaohuitong.getOutTradeNo());
        data.put("bankWithdrNo", orderGaohuitong.getBankWithdrNo());
        data.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        data.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        DepositProcHandler handler = (DepositProcHandler) SpringContextHolder.getBean(RouteCode.GAOHUITONG.getId().toLowerCase() + ConstEC.DEPOSITPROCHANDLER);
       //更新订单状态等处理
        handler.procWithdr(data);

        responseMsg.put("outTradeNo", orderGaohuitong.getOutTradeNo());
        responseMsg.put("bankWithdrNo", orderGaohuitong.getBankWithdrNo());
        responseMsg.put("orderStatus", orderGaohuitong.getOrderStatus());
        responseMsg.put("routeCode", orderGaohuitong.getRouteCode());
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        return responseMsg;
    }

    @Override
    public void beforeWithdrCardBind(RequestMsg requestMsg,ResponseMsg responseMsg) throws Exception {
        //高汇通需要这三步操作
        PlatformBusiHandler handler = SpringContextHolder.getBean("gaohuitongPlatformBusiHandler");
        log.info("|提现绑卡确认|调用第三方支付|请求报文：{}|", JSON.toJSONString(requestMsg));
        // 商户基础信息登记
        handler.baseInfoRegister(requestMsg, responseMsg);
        if (!ConstEC.SUCCESS_10000.equals(responseMsg.get(ConstEC.RETURNCODE))) {
            log.error("|提现绑卡确认|商户基础信息登记|失败|" + JSON.toJSONString(requestMsg));
            throw new BusiException(15009);
        }
        //上传图片
        handler.addImageInfo(requestMsg, responseMsg);
        if (!ConstEC.SUCCESS_10000.equals(responseMsg.get(ConstEC.RETURNCODE))) {
            log.error("|提现绑卡确认|上传图片|失败|" + JSON.toJSONString(requestMsg));
            throw new BusiException(15013);
        }
        // 开通支付平台业务
        handler.initiateBusi(requestMsg, responseMsg);
        if (!ConstEC.SUCCESS_10000.equals(responseMsg.get(ConstEC.RETURNCODE))) {
            log.error("|提现绑卡确认|开通支付平台业务|失败|" + JSON.toJSONString(requestMsg));
            throw new BusiException(15010);
        }
    }

    /**
     * 获取-高汇通请求报文-通用报文Head
     *
     * @param msgType  报文类型
     *                 商户相关报文：01
     *                 支付平台相关报文：02
     * @param tranCode 交易服务码
     * @return
     * @throws Exception
     */
    private GHTHeadDTO buildHead(String agencyId, String msgType, String tranCode) throws Exception {
        GHTHeadDTO headDTO = new GHTHeadDTO();
        headDTO.setVersion(version);
        headDTO.setAgencyId(agencyId);
        headDTO.setMsgType(msgType);
        headDTO.setTranCode(tranCode);
        String reqMsgId = DateTimeUtil.date8() + seqIncrService.nextVal(SeqIncrType.REQUEST_ID_GAOHUITONG.getId(), 8, Align.LEFT);
        headDTO.setReqMsgId(reqMsgId);
        headDTO.setReqDate(DateTimeUtil.date14(new Date()));
        return headDTO;
    }

    /**
     * @Description(描述): 统一处理请求
     * @auther: Jack Lin
     * @date: 2019/9/7 17:31
     */
    public GHTResp sendRequest(GHTReq req, GHTResp resp, String tranCode, String url, RouteConf routeConf, Class clazz) throws Exception {
        long l = System.currentTimeMillis();
        String reqMsg = req.toXml(false);
        //AES key
        String keyStr = getRandom(16);
        PublicKey publicKey = GaohuitongMessgeUtil.getPublicKey(routeConf.getPublicKeyPath());
        PrivateKey privateKey = GaohuitongMessgeUtil.getPrivateKey(routeConf.getKeyPath());
        //组装请求参数
        Map<String, Object> msgMap = GaohuitongMessgeUtil.requestHandle(routeConf.getBankMercId(), reqMsg, keyStr, publicKey, privateKey, tranCode, null);
        String reqMsgStr = Sign.getPlainURLEncoder(msgMap, ConstEC.ENCODE_UTF8);
        String s1 = address + url + "?" + reqMsgStr;
        log.info(" call_ght： url：{}，request:{}", url, JSON.toJSONString(msgMap));
        log.info("call_ght： tranCode：{}， xml：{}", tranCode, reqMsg);
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
            if (!GaohuitongConstants.RETURN_CODE_SUCCESS.equals(s.getRespCode())) {
                //R不处理
                if (GaohuitongConstants.RESPTYPE_R.equalsIgnoreCase(s.getRespType())) {
                    return;
                }
                log.error("call_ght: error：{}", s.getRespMsg());
                throw new BusiException(s.getRespCode(),s.getRespMsg());
            }
        });
    }

    private ResponseMsg payByDF(String inMerchantId, String bankCardName, String bankCardNo, RouteDictionary routeDictionary, WithdrOrderGaohuitong orderGaohuitong, UserRealNameDetails userRealNameDetails, RouteConf routeConf) throws Exception {
        log.info("call_ght 代付开始，withdrOrderNo：{}", orderGaohuitong.getWithdrOrderNo());
        ResponseMsg responseMsg = new ResponseMsg();
        //代付
        GHTReq<GHTPayDTO> reqPay = new GHTReq<>();
        GHTResp<GHTPayDTO> respPay = new GHTResp<>();
        try {
            GHTPayDTO ghtPayDTO = new GHTPayDTO();
            ghtPayDTO.setBusiness_code(GaohuitongConstants.GHT_BUSINESS_PAY);
            ghtPayDTO.setUser_id(inMerchantId);//机构代付直接填 机构号
            ghtPayDTO.setDF_type("0"); //机构代付
            ghtPayDTO.setBank_code(routeDictionary.getStrVal());
            ghtPayDTO.setAccount_no(cipherServiceImpl.decryptAES(bankCardNo));
            ghtPayDTO.setAccount_name(bankCardName);
            ghtPayDTO.setAmount(orderGaohuitong.getPrice().multiply(new BigDecimal("100")).setScale(0, BigDecimal.ROUND_UP).intValue() + ""); //分，整数
            ghtPayDTO.setTerminal_no(routeConf.getAppId());
            ghtPayDTO.setID(cipherServiceImpl.decryptAES(userRealNameDetails.getCardNo())); //身份证
            reqPay.setBody(ghtPayDTO);
            reqPay.setHead(buildHead(routeConf.getBankMercId(), GaohuitongConstants.MESSAGE_PROPERTY_MSGTYPE_MERCHANT, GaohuitongConstants.GHT_PAY_OTHER));
            sendRequest(reqPay, respPay, GaohuitongConstants.GHT_PAY_OTHER, realTimeDF, routeConf, GHTPayDTO.class);
            verifyResp(respPay.getHead());
        } catch (Exception e) {
            GHTHeadDTO head = respPay.getHead();
            orderGaohuitong.setReturnCode(head.getRespCode());
            orderGaohuitong.setReturnMsg(head.getRespMsg());
            iWithdrOrderGaohuitongService.updateById(orderGaohuitong);
            log.info("call_ght 代付 失败，withdrOrderNo：{}，respMsg: {}", orderGaohuitong.getWithdrOrderNo(), head.getRespMsg());
            responseMsg.put(ConstEC.RETURNCODE, head.getRespCode());
            responseMsg.put(ConstEC.RETURNMSG, head.getRespMsg());
            return responseMsg;
        }
        //代付后，状态改成BW
        orderGaohuitong.setBankWithdrNo(respPay.getHead().getReqMsgId()); //代付的要存这个
        orderGaohuitong.setOrderStatus(WithdrOrderStatus.BANK_WAIT.getId());
        boolean b = iWithdrOrderGaohuitongService.updateById(orderGaohuitong);
        if (!b) {
            //不成功人工干预
            log.error("更新提现订单状态失败，outTradeNo：{}，withdrOrderNo：{}", orderGaohuitong.getOutTradeNo(), orderGaohuitong.getWithdrOrderNo());
            throw new BusiException(11123);
        }
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        log.info("call_ght 代付成功，withdrOrderNo：{}", orderGaohuitong.getWithdrOrderNo());
        return responseMsg;
    }

    private ResponseMsg transfer(String inMerchantId, WithdrOrderGaohuitong orderGaohuitong, RouteConf routeConf, int type) throws Exception {
        log.info("call_ght 转账开始，withdrOrderNo：{}", orderGaohuitong.getWithdrOrderNo());
        ResponseMsg responseMsg = new ResponseMsg();
        GHTReq<GHTTransferDTO> req = new GHTReq<>();
        GHTResp<GHTTransferDTO> resp = new GHTResp<>();
        try {
            GHTTransferDTO transferDTO = new GHTTransferDTO();
            switch (type) {
                case 1:
                    transferDTO.setTransferType("01"); //子商户转机构
                    transferDTO.setOutMerchantId(inMerchantId);
                    break;
                case 3:
                    transferDTO.setTransferType("03"); //机构转子商户
                    transferDTO.setInMerchantId(inMerchantId);
                    break;
            }
            transferDTO.setTerminalNo(routeConf.getAppId());
            transferDTO.setAmount(orderGaohuitong.getPrice().add(orderGaohuitong.getServicePrice()).toString());
            req.setBody(transferDTO);
            req.setHead(buildHead(routeConf.getBankMercId(), GaohuitongConstants.MESSAGE_PROPERTY_MSGTYPE_MERCHANT, GaohuitongConstants.GHT_TRANSFER));
            sendRequest(req, resp, GaohuitongConstants.GHT_TRANSFER, transferUrl, routeConf, GHTOrderDTO.class);
            verifyResp(resp.getHead());
        } catch (Exception e) {
            GHTHeadDTO head = resp.getHead();
            orderGaohuitong.setReturnCode(head.getRespCode());
            orderGaohuitong.setReturnMsg(head.getRespMsg());
            iWithdrOrderGaohuitongService.updateById(orderGaohuitong);
            log.info("call_ght 转账失败，withdrOrderNo：{}，respMsg: {}", orderGaohuitong.getWithdrOrderNo(), head.getRespMsg());
            responseMsg.put(ConstEC.RETURNCODE, head.getRespCode());
            responseMsg.put(ConstEC.RETURNMSG, head.getRespMsg());
            return responseMsg;
        }
        responseMsg.put("payMsgId", resp.getHead().getPayMsgId());
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        log.info("call_ght 转账成功，withdrOrderNo：{}", orderGaohuitong.getWithdrOrderNo());
        return responseMsg;
    }
}
