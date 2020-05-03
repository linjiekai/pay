package com.mppay.gateway.handler.platform;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.model.OSSObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.constant.*;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.Sign;
import com.mppay.core.sign.gaohuitong.GaohuitongConstants;
import com.mppay.core.sign.gaohuitong.GaohuitongMessgeUtil;
import com.mppay.core.utils.*;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.dto.platform.gaohuitong.*;
import com.mppay.gateway.handler.PlatformBusiHandler;
import com.mppay.gateway.handler.utils.RequestMsgUtil;
import com.mppay.service.entity.*;
import com.mppay.service.service.*;
import com.mppay.service.service.common.ICipherService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


/**
 * @author: Jiekai Lin
 * @Description(描述): 高汇通业务
 * @date: 2019/9/23 11:40
 */
@Service("gaohuitongPlatformBusiHandler")
@Slf4j
public class GaohuitongPlatformBusiHandlerImpl implements PlatformBusiHandler {

    @Value("${ght.xfhl.version}")
    private String version;
    @Value("${ght.xfhl.address}")
    private String address;
    @Value("${ght.xfhl.address2}")
    private String address2;

    @Value("${gaohuitong.interfaceWeb.basicInfo}")
    private String basicInfoUrl;
    @Value("${gaohuitong.interfaceWeb.busiInfo}")
    private String busiInfo;
    @Value("${gaohuitong.interfaceWeb.qryCardInfo}")
    private String qryCardInfo;
    @Value("${gaohuitong.interfaceWeb.qryBalanceInfo}")
    private String qryBalanceInfo;
    @Value("${gaohuitong.interfaceWeb.addImageInfo}")
    private String addImageInfo;

    @Autowired
    private IRouteDictionaryService iRouteDictionaryService;
    @Autowired
    private IBankService iBankService;
    @Autowired
    private IUserAgrInfoService userAgrInfoService;
    @Autowired
    private IUserAgrBusiService userAgrBusiService;
    @Autowired
    private IUserOperService userOperService;
    @Autowired
    private ISeqIncrService iSeqIncrService;
    @Autowired
    private ISeqIncrService seqIncrService;
    @Autowired
    private IAliResourcesService aliResourcesService;
    @Autowired
    private ICipherService cipherServiceImpl;

    @Override
    public void baseInfoRegister(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        log.info("|高汇通|商户注册|基础信息等级|请求参数：{}", JSON.toJSONString(requestMsg));
        //校验下加密数据
        RequestMsgUtil.validateRequestMsg(requestMsg);
        //先解密
        Long userId = (Long) requestMsg.get("userId");
        String mobile = (String) requestMsg.get("mobile");
        String bankCardName = (String) requestMsg.get("bankCardName");
        String certNo = (String) requestMsg.get("certNo");
        String bankCardNo = (String) requestMsg.get("bankCardNo");
        bankCardNo = cipherServiceImpl.decryptAES(bankCardNo);
        mobile = cipherServiceImpl.decryptAES(mobile);
        String bankCode = (String) requestMsg.get("bankCode");
        String mercId = (String) requestMsg.get("mercId");
        String platform = (String) requestMsg.get("platform");

        RouteConf routeConf = RequestMsgUtil.getRouteConf(mercId, platform, TradeType.QUICK.getId(), GaohuitongConstants.GHT_ROUTE, null);

        // 获取用户操作号：根据 userId + merc_id -> user_oper_no
        UserOper userOper = userOperService.getOne(new QueryWrapper<UserOper>()
                .eq("merc_id", requestMsg.get("mercId"))
                .eq("user_id", userId)
                .eq("status", UserOperStatus.NORMAL.getId()));
        Optional.ofNullable(userOper).orElseThrow(() -> new BusiException(11308));

        //先查有没有注册过
        UserAgrInfo userAgrInfo = userAgrInfoService.getOne(new QueryWrapper<UserAgrInfo>()
                .eq("user_no", userOper.getUserNo())
                .eq("route_code", GaohuitongConstants.GHT_ROUTE)
                .eq("org_no", routeConf.getBankMercId())
                .eq("status", UserAgrInfoStatus.NORMAL.getId()));
        if (userAgrInfo != null) {
            responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
            responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
            Map<String, Object> map = new HashMap();
            map.put("outMercId", userAgrInfo.getOutMercId());
            responseMsg.put(ConstEC.DATA, map);
            return;
        }

        RouteDictionary one = iRouteDictionaryService.getOne(new QueryWrapper<RouteDictionary>().eq("category", "bankOpen").eq("route", GaohuitongConstants.GHT_ROUTE).eq("name", bankCode));
        Optional.ofNullable(one).orElseThrow(() -> new BusiException(30002));
        Bank bank = iBankService.getOne(new QueryWrapper<Bank>().eq("bank_code", bankCode));
        Optional.ofNullable(bank).orElseThrow(() -> new BusiException(11018));

        //组装请求参数
        GHTReq<GHTBaseInfoRegisterDTO> req = new GHTReq<>();
        GHTBaseInfoRegisterDTO dto = new GHTBaseInfoRegisterDTO();
        GHTResp<GHTBaseInfoRegisterDTO> ghtBaseInfoResp = new GHTResp<>();
        GHTHeadDTO head = buildHead(GaohuitongConstants.MESSAGE_PROPERTY_MSGTYPE_MERCHANT, GaohuitongConstants.GHT_BASICINFO_TRANCODE, routeConf.getBankMercId());
        dto.setHandleType("0");
        dto.setMerchantName(bankCardName);
        dto.setShortName(bankCardName);
        dto.setCity("5810");//先写死广州
        dto.setMerchantAddress("广州市");
        dto.setServicePhone(mobile);
        dto.setMerchantType("01"); //个体户
        dto.setCategory("4900");
        dto.setCorpmanName(bankCardName);
        dto.setCorpmanId(certNo);
        dto.setCorpmanMobile(mobile);
        dto.setBankCode(one.getStrVal());
        dto.setBankName(bank.getBankName());
        dto.setBankaccountNo(bankCardNo);
        dto.setBankaccountName(bankCardName);
        dto.setAutoCus("0");
        dto.setAppid("0");
        dto.setPid("0");
        dto.setSettingSettCard("1");
        dto.setBankaccProp(0);
        dto.setCertCode(1); //证件类型
        String bankCardType = (String) requestMsg.get("bankCardType");
        switch (bankCardType) {
            case "01":
                dto.setBankaccountType(1); //借记卡
                break;
            case "02":
                dto.setBankaccountType(2); //贷记卡
                break;
        }
        req.setHead(head);
        req.setBody(dto);

        //统一处理请求
        sendRequest(req, ghtBaseInfoResp, GaohuitongConstants.GHT_BASICINFO_TRANCODE, basicInfoUrl, routeConf, GHTBaseInfoRegisterDTO.class, null, null);
        //统一处理请求结果
        GHTHeadDTO head1 = ghtBaseInfoResp.getHead();
        verifyResp(head1);

        GHTBaseInfoRegisterDTO body = ghtBaseInfoResp.getBody();
        //入驻成功，商户号存库
        UserAgrInfo info = new UserAgrInfo();
        info.setMercId(mercId);
        info.setAgrNo(iSeqIncrService.nextVal(SeqIncrType.USER_AGR_NO.getId(), SeqIncrType.USER_AGR_NO.getLength(), Align.LEFT)); //协议编号
        info.setOutMercId(body.getMerchantId()); //高汇通子商户号
        info.setUserNo(userOper.getUserNo());
        info.setUserOperNo(userOper.getUserOperNo());// 用户操作号
        info.setAgrFlag(0); //个人
        info.setStatus(1);//生效
        info.setRouteCode(routeConf.getRouteCode());//路由编号
        info.setOrgNo(routeConf.getBankMercId());//机构号
        info.setTerminalNo(routeConf.getAppId());//终端号
        String respDate = head1.getRespDate();
        info.setAgrDate(respDate.substring(0, 4) + "-" + respDate.substring(4, 6) + "-" + respDate.substring(6, 8));
        info.setAgrTime(respDate.substring(8, 10) + ":" + respDate.substring(10, 12) + ":" + respDate.substring(12, 14));
        userAgrInfoService.save(info);

        //直接把对象存到响应
        responseMsg.put("outMercId", body.getMerchantId());
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        log.info("|高汇通|商户注册|基础信息等级|响应参数：{}", JSON.toJSONString(responseMsg));
    }

    @Override
    public void initiateBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        log.info("|高汇通|商户注册|开通业务|请求参数：{}", JSON.toJSONString(requestMsg));
        String userNo = String.valueOf(requestMsg.get("userNo"));
        String mercId = (String) requestMsg.get("mercId");
        String platform = (String) requestMsg.get("platform");

        UserAgrInfo one = userAgrInfoService.getOne(new QueryWrapper<UserAgrInfo>()
                .eq("user_no", userNo)
                .eq("route_code", GaohuitongConstants.GHT_ROUTE)
                .eq("status", UserAgrInfoStatus.NORMAL.getId())
                .last("limit 1"));
        Optional.ofNullable(one).orElseThrow(() -> new BusiException(30001));

        List<UserAgrBusi> userAgrBusiList = userAgrBusiService.list(new QueryWrapper<UserAgrBusi>()
                .eq("agr_no", one.getAgrNo())
                .eq("route_code", GaohuitongConstants.GHT_ROUTE)
                .eq("status", UserAgrBusiStatus.NORMAL.getId()));
        List<String> busiTypes = userAgrBusiList.stream().map(UserAgrBusi::getBusiType).collect(Collectors.toList());
        RouteConf routeConf = RequestMsgUtil.getRouteConf(mercId, platform, TradeType.QUICK.getId(), GaohuitongConstants.GHT_ROUTE, null);

        //组装报文
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sBuilder.append("<merchant>");
        sBuilder.append("<head>");
        sBuilder.append("<version>" + version + "</version>");
        sBuilder.append("<agencyId>" + routeConf.getBankMercId() + "</agencyId>");// 机构标识
        sBuilder.append("<msgType>01</msgType>");
        sBuilder.append("<tranCode>100003</tranCode>");
        sBuilder.append("<reqMsgId>" + iSeqIncrService.nextVal(SeqIncrType.REQUEST_ID_GAOHUITONG.getId(), SeqIncrType.REQUEST_ID_GAOHUITONG.getLength(), Align.LEFT) + "</reqMsgId>");// 请求交易流水号
        sBuilder.append("<reqDate>" + DateUtil.dateFormat(new Date(), DateUtil.DATEFORMAT_9) + "</reqDate>");// 请求日期时间
        sBuilder.append("</head>");
        sBuilder.append("<body>");
        sBuilder.append("<merchantId>" + one.getOutMercId() + "</merchantId>");
        sBuilder.append("<handleType>0</handleType>");
        sBuilder.append("<cycleValue>1</cycleValue>");
        List<RouteDictionary> routeDictionaries = iRouteDictionaryService.list(new QueryWrapper<RouteDictionary>()
                .eq("route", GaohuitongConstants.GHT_ROUTE.toUpperCase())
                .eq("category", "businessCode"));
        boolean hasB00302 = false;
        //删除已经开通过的业务，避免重复开通
        for (Iterator<RouteDictionary> i = routeDictionaries.iterator(); i.hasNext(); ) {
            RouteDictionary next = i.next();
            if (busiTypes.contains(next.getName())) {
                i.remove();
                continue;
            }
            //代付业务单独处理下
            if (GaohuitongConstants.GHT_BUSINESS_PAY.equalsIgnoreCase(next.getName())) {
                sBuilder.append("<busiList>");
                sBuilder.append("<busiCode>" + next.getName() + "</busiCode>");
                sBuilder.append("<futureRateType>2</futureRateType>"); //费率类型 ,笔
                sBuilder.append("<futureRateValue>" + next.getStrVal() + "</futureRateValue>"); //费率
                sBuilder.append("<futureMinAmount>" + next.getLongVal().longValue() + "</futureMinAmount>"); //保底
                sBuilder.append("</busiList>");
                hasB00302 = true;
                i.remove();
            }
        }
        //为0说明都开通过了，直接返回
        if (routeDictionaries.size() == 0) {
            responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
            responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
            return;
        }
        for (RouteDictionary r : routeDictionaries) {
            sBuilder.append("<busiList>");
            sBuilder.append("<busiCode>" + r.getName() + "</busiCode>");
            sBuilder.append("<futureRateType>1</futureRateType>"); //费率类型
            sBuilder.append("<futureRateValue>" + r.getStrVal() + "</futureRateValue>"); //费率
            sBuilder.append("<futureMinAmount>" + r.getLongVal().longValue() + "</futureMinAmount>"); //保底
            sBuilder.append("</busiList>");
        }
        sBuilder.append("</body>");
        sBuilder.append("</merchant>");
        String reqMsg = sBuilder.toString();
        //统一处理请求

        GHTReq<GHTInitiateBusiDTO> req = GHTReq.fromXml(reqMsg, GHTReq.class);
        GHTResp<GHTInitiateBusiDTO> resp = new GHTResp<>();
        sendRequest(req, resp, GaohuitongConstants.GHT_INITIATEBUSI_TRANCODE, busiInfo, routeConf, GHTInitiateBusiDTO.class, null, null);

        //统一处理请求结果
        GHTHeadDTO head1 = resp.getHead();
        verifyResp(head1);

        //开通记录存库
        List<UserAgrBusi> userAgrBusis = new LinkedList<>();
        if (hasB00302) {
            RouteDictionary one1 = iRouteDictionaryService.getOne(new QueryWrapper<RouteDictionary>()
                    .eq("route", GaohuitongConstants.GHT_ROUTE.toUpperCase())
                    .eq("category", "businessCode").eq("name", GaohuitongConstants.GHT_BUSINESS_PAY));
            routeDictionaries.add(one1);
        }
        for (RouteDictionary r : routeDictionaries) {
            UserAgrBusi userAgrBusi = new UserAgrBusi();
            userAgrBusi.setAgrNo(one.getAgrNo());
            userAgrBusi.setBusiType(r.getName());
            userAgrBusi.setStatus(UserAgrBusiStatus.NORMAL.getId());
            userAgrBusi.setRouteCode(GaohuitongConstants.GHT_ROUTE);
            userAgrBusis.add(userAgrBusi);
        }
        userAgrBusiService.saveBatch(userAgrBusis);

        GHTInitiateBusiDTO body = resp.getBody();
        //设置响应
        responseMsg.put("merchantId", body.getMerchantId());
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        log.info("|高汇通|商户注册|开通业务|响应参数：{}", JSON.toJSONString(requestMsg));
    }


    public GHTHeadDTO buildHead(String msgType, String tranCode, String agencyId) throws Exception {
        GHTHeadDTO dto = new GHTHeadDTO();
        dto.setVersion(version);
        dto.setAgencyId(agencyId);
        dto.setMsgType(msgType);
        dto.setTranCode(tranCode);
        String reqMsgId = DateTimeUtil.date8() + seqIncrService.nextVal(SeqIncrType.REQUEST_ID_GAOHUITONG.getId(), SeqIncrType.REQUEST_ID_GAOHUITONG.getLength(), Align.LEFT);
        dto.setReqMsgId(reqMsgId);
        dto.setReqDate(DateUtil.dateFormat(new Date(), DateUtil.DATEFORMAT_9));
        return dto;
    }

    /**
     * 提现银行卡查询
     *
     * @param requestMsg
     * @throws Exception
     */
    @Override
    public void queryCardBind(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        log.info("|高汇通|提现银行查询|请求参数：{}", JSON.toJSONString(requestMsg));
        //校验下加密数据
        RequestMsgUtil.validateRequestMsg(requestMsg);
        String userNo = (String) requestMsg.get("userNo");
        String mercId = (String) requestMsg.get("mercId");
        String platform = (String) requestMsg.get("platform");

        UserAgrInfo userAgrInfo = userAgrInfoService.getOne(new QueryWrapper<UserAgrInfo>()
                .eq("user_no", userNo)
                .eq("route_code", GaohuitongConstants.GHT_ROUTE)
                .eq("status", UserAgrInfoStatus.NORMAL.getId())
                .last("limit 1"));
        Optional.ofNullable(userAgrInfo).orElseThrow(() -> new BusiException(30001));
        RouteConf routeConf = RequestMsgUtil.getRouteConf(mercId, platform, TradeType.QUICK.getId(), GaohuitongConstants.GHT_ROUTE, null);

        GHTReq<GHTqryCardInfoDTO> req = new GHTReq<>();
        GHTResp<GHTqryCardInfoDTO> resp = new GHTResp<>();
        GHTqryCardInfoDTO dto = new GHTqryCardInfoDTO();
        dto.setMerchantId(userAgrInfo.getOutMercId());
        if (StringUtils.isNotBlank((String) requestMsg.get("bankCardNo"))) {
            String bankCardNo = (String) requestMsg.get("bankCardNo");
            String bankCardNoDe = cipherServiceImpl.decryptAES(bankCardNo); //银行卡明文
            dto.setBankaccountNo(bankCardNoDe);
        }
        req.setBody(dto);
        req.setHead(buildHead(GaohuitongConstants.MESSAGE_PROPERTY_MSGTYPE_MERCHANT, GaohuitongConstants.GHT_QRYCARDINFO, routeConf.getBankMercId()));
        //统一处理请求

        sendRequest(req, resp, GaohuitongConstants.GHT_QRYCARDINFO, qryCardInfo, routeConf, GHTqryCardInfoDTO.class, address2, null);
        verifyResp(resp.getHead());

        GHTqryCardInfoDTO respBody = resp.getBody();
        GHTBankaccounListDTO bankaccounListDTO = respBody.getBankaccounList();
        if (null == bankaccounListDTO) {
            return;
        }

        RouteDictionary one = iRouteDictionaryService.getOne(new QueryWrapper<RouteDictionary>()
                .eq("route", GaohuitongConstants.GHT_ROUTE.toUpperCase())
                .eq("category", GaohuitongConstants.ROUTE_DICTIONARY_CATEGORY_BANKOPEN)
                .eq("str_val", bankaccounListDTO.getBankCode()));
        String bankCode = one.getName();
        Bank bank = iBankService.getOne(new QueryWrapper<Bank>().eq("bank_code", bankCode));

        //设置响应
        responseMsg.put("merchantId", respBody.getMerchantId());
        responseMsg.put("bankCode", bankCode);
        responseMsg.put("bankName", bank.getBankName());
        responseMsg.put("bankAbbr", bank.getBankAbbr());
        responseMsg.put("bankCardType", 0 + bankaccounListDTO.getBankaccountType());
        responseMsg.put("authResult", respBody.getBankaccounList().getAuthResult());
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        log.info("|高汇通|提现银行查询|响应参数：{}", JSON.toJSONString(responseMsg));
    }

    @Override
    public void queryBalanceInfo(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {

        GHTReq<GHTBalanceInfoDTO> req = new GHTReq<>();
        GHTResp<GHTBalanceInfoDTO> resp = new GHTResp<>();
        GHTBalanceInfoDTO balanceInfoDTO = new GHTBalanceInfoDTO();
        Object merchantId = requestMsg.get("merchantId");
        if (merchantId != null) {
            balanceInfoDTO.setMerchantId((String) merchantId);
        }
        RouteConf routeConf = RequestMsgUtil.getRouteConf((String) requestMsg.get("mercId"), (String) requestMsg.get("platform"), TradeType.QUICK.getId(), GaohuitongConstants.GHT_ROUTE, null);
        req.setBody(balanceInfoDTO);
        req.setHead(buildHead(GaohuitongConstants.MESSAGE_PROPERTY_MSGTYPE_MERCHANT, GaohuitongConstants.GHT_QRYBALANCEINFO, routeConf.getBankMercId()));
        //统一处理请求

        sendRequest(req, resp, GaohuitongConstants.GHT_QRYBALANCEINFO, qryBalanceInfo, routeConf, GHTBalanceInfoDTO.class, null, null);
        verifyResp(resp.getHead());

        //设置响应
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        Map<String, Object> map = new HashMap<>();
        map.put("balanceAmount", resp.getBody().getBalanceAmount());
        map.put("freezeAmount", resp.getBody().getFreezeAmount());
        responseMsg.put(ConstEC.DATA, map);
    }

    @Override
    public void addImageInfo(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        log.info("|高汇通|添加照片|请求参数：{}", JSON.toJSONString(requestMsg));
        //校验下加密数据
        RequestMsgUtil.validateRequestMsg(requestMsg);
        String userOperNo = (String) requestMsg.get("userOperNo");
        String bankCardImgFront = (String) requestMsg.get("bankCardImgFront");
        String imgFront = (String) requestMsg.get("imgFront");
        String imgBack = (String) requestMsg.get("imgBack");
        String mercId = (String) requestMsg.get("mercId");
        String platform = (String) requestMsg.get("platform");

        UserAgrInfo userAgrInfo = userAgrInfoService.getOne(new QueryWrapper<UserAgrInfo>()
                .eq("user_oper_no", userOperNo)
                .eq("route_code", GaohuitongConstants.GHT_ROUTE)
                .eq("status", UserAgrInfoStatus.NORMAL.getId())
                .last(" limit 1"));
        Optional.ofNullable(userAgrInfo).orElseThrow(() -> new BusiException(30001));

        RouteConf routeConf = RequestMsgUtil.getRouteConf(mercId, platform, TradeType.QUICK.getId(), GaohuitongConstants.GHT_ROUTE, null);
        GHTReq<GHTImageInfoDTO> req = new GHTReq<>();
        GHTResp<GHTImageInfoDTO> resp = new GHTResp<>();
        GHTImageInfoDTO dto = new GHTImageInfoDTO();
        req.setHead(buildHead(GaohuitongConstants.MESSAGE_PROPERTY_MSGTYPE_MERCHANT, GaohuitongConstants.GHT_ADDIMAGEINFO, routeConf.getBankMercId()));
        dto.setMerchantId(userAgrInfo.getOutMercId());

        ThreadPoolExecutor threadPoolExecutor = null;
        try {
            threadPoolExecutor = new ThreadPoolExecutor(3, 3,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
            List<Future<Integer>> integerList = new ArrayList<>();
            Future<Integer> bankCardimgfront = threadPoolExecutor.submit(() -> {
                GHTResp<GHTImageInfoDTO> ghtImageInfoDTOGHTResp = sendImage(userAgrInfo.getBankCardImgFront(), bankCardImgFront, dto, req, resp, routeConf, "01");
                if (ghtImageInfoDTOGHTResp.getHead()!=null) {
                    if (GaohuitongConstants.RETURN_CODE_SUCCESS.equalsIgnoreCase(ghtImageInfoDTOGHTResp.getHead().getRespCode())) {
                        userAgrInfo.setBankCardImgFront(bankCardImgFront);
                        log.info("call_ght, 上传: 银行卡 {} , success", bankCardImgFront);
                    } else {
                        log.info("call_ght, 上传: 银行卡 {} , 失败：{}", bankCardImgFront, resp.getHead().getRespMsg());
                    }
                }
                return 1;
            });
            integerList.add(bankCardimgfront);
            Future<Integer> imgF = threadPoolExecutor.submit(() -> {
                GHTResp<GHTImageInfoDTO> ghtImageInfoDTOGHTResp = sendImage(userAgrInfo.getIdCardImgFront(), imgFront, dto, req, resp, routeConf, "02");
                if (ghtImageInfoDTOGHTResp.getHead()!=null) {
                    if (GaohuitongConstants.RETURN_CODE_SUCCESS.equalsIgnoreCase(ghtImageInfoDTOGHTResp.getHead().getRespCode())) {
                        userAgrInfo.setIdCardImgFront(imgFront);
                        log.info("call_ght, 上传: 身份证正面 {} , success", imgFront);
                    } else {
                        log.info("call_ght, 上传: 身份证正面 {} , 失败：{}", imgFront, resp.getHead().getRespMsg());
                    }
                }
                return 1;
            });
            integerList.add(imgF);
            Future<Integer> imgB = threadPoolExecutor.submit(() -> {
                GHTResp<GHTImageInfoDTO> ghtImageInfoDTOGHTResp = sendImage(userAgrInfo.getIdCardImgBack(), imgBack, dto, req, resp, routeConf, "03");
                if (ghtImageInfoDTOGHTResp.getHead()!=null) {
                    if (GaohuitongConstants.RETURN_CODE_SUCCESS.equalsIgnoreCase(ghtImageInfoDTOGHTResp.getHead().getRespCode())) {
                        userAgrInfo.setIdCardImgBack(imgBack);
                        log.info("call_ght, 上传: 身份证反面 {} , success", imgBack);
                    } else {
                        log.info("call_ght, 上传: 身份证反面 {} , 失败：{}", imgBack, resp.getHead().getRespMsg());
                    }
                }
                return 1;
            });
            integerList.add(imgB);
            for (Future<Integer> item : integerList) {
                Integer integer = item.get();
            }
        } catch (Exception e) {
            log.error("call_ght, 上传图片异常 , 失败：{}", e);
        } finally {
            if (threadPoolExecutor != null) {
                threadPoolExecutor.shutdown();
            }
        }
        //更新下数据
        userAgrInfoService.updateById(userAgrInfo);
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
        log.info("|高汇通|添加照片|响应参数：{}", JSON.toJSONString(responseMsg));
    }

    public GHTResp<GHTImageInfoDTO> sendImage(String image, String imageUrl, GHTImageInfoDTO dto, GHTReq<GHTImageInfoDTO> req, GHTResp<GHTImageInfoDTO> resp, RouteConf routeConf, String picType) throws Exception {
        if (StrUtil.isBlank(image)) {
            Map<String, String> map = aliResourcesService.getStsToken(imageUrl);
            //获取图片流
            OSSObject object = aliResourcesService.getOssResouces(map);
            Map<String, InputStream> inputStreams = new HashMap<>();
            inputStreams.put("image", object.getObjectContent());
            dto.setPicType(picType);
            req.setBody(dto);
            sendRequest(req, resp, GaohuitongConstants.GHT_ADDIMAGEINFO, addImageInfo, routeConf, GHTImageInfoDTO.class, null, inputStreams);
        }
        return resp;
    }


    /**
     * @Description(描述): 统一处理请求+
     * @auther: Jack Lin
     * @date: 2019/9/7 17:31
     */
    public GHTResp sendRequest(GHTReq req, GHTResp resp, String tranCode, String url, RouteConf routeConf, Class clazz, String otherHost, Map<String, InputStream> inputStreams) throws Exception {
        long l = System.currentTimeMillis();
        String reqMsg = req.toXml(false);
        //AES key
        String keyStr = StringUtil.getRandom(16);
        PublicKey publicKey = GaohuitongMessgeUtil.getPublicKey(routeConf.getPublicKeyPath());
        PrivateKey privateKey = GaohuitongMessgeUtil.getPrivateKey(routeConf.getKeyPath());
        //组装请求参数
        Map<String, Object> msgMap = GaohuitongMessgeUtil.requestHandle(routeConf.getBankMercId(), reqMsg, keyStr, publicKey, privateKey, tranCode, null);
        String reqMsgStr = Sign.getPlainURLEncoder(msgMap, ConstEC.ENCODE_UTF8);
        String s1 = address + url;
        if (StrUtil.isNotBlank(otherHost)) {
            s1 = otherHost + url;
        }
        log.info(" call_ght： url：{}，request:{}", url, JSON.toJSONString(msgMap));
        log.info("call_ght： tranCode：{}， xml：{}", tranCode, reqMsg);
        String response = null;
        if (inputStreams != null) {
            response = HttpClientUtil.excuteHttpRequest(s1, inputStreams, msgMap, new HashMap());
        } else {
            response = HttpClientUtil.httpsRequest(address + url + "?" + reqMsgStr, HttpClientUtil.HTTP_REQUESTMETHOD_POST, null);
        }
        //解析响应
        Map map = GaohuitongMessgeUtil.parseMap(response);
        String s = GaohuitongMessgeUtil.responseHandle(map, keyStr, publicKey, privateKey);
        log.info("call_ght：url：{}，耗时：{}ms， response:{}", s1, System.currentTimeMillis() - l, s);
        if (StringUtils.isEmpty(s)) {
            throw new BusiException(13110);
        }
        JSONObject jsonObject = XmlUtil.xml2Json(s);
        JSONObject body = jsonObject.getJSONObject("body");
        JSONObject head = jsonObject.getJSONObject("head");
        resp.setHead(JSON.parseObject(head.toJSONString(), GHTHeadDTO.class));
        resp.setBody(JSON.parseObject(body.toJSONString(), clazz));
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
            if (GaohuitongConstants.ERROR_10000X.equalsIgnoreCase(s.getRespCode())) {
                s.setRespCode("99998");
            }
            if (!GaohuitongConstants.RETURN_CODE_SUCCESS.equals(s.getRespCode())) {
                log.error("调用高汇通：失败:{}", s.getRespMsg());
                throw new BusiException(s.getRespCode(), s.getRespMsg());
            }
        });
    }

}
