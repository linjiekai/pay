package com.mppay.gateway.handler.impl;

import java.util.*;

import cn.hutool.json.JSONUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.RealType;
import com.mppay.core.constant.RealedStatus;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.Sign;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.core.utils.DateUtil;
import com.mppay.core.utils.HttpClientUtil;
import com.mppay.core.utils.HttpUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.service.entity.UserOper;
import com.mppay.service.entity.UserRealName;
import com.mppay.service.entity.UserRealNameDetails;
import com.mppay.service.service.IAliResourcesService;
import com.mppay.service.service.IDictionaryService;
import com.mppay.service.service.IUserOperService;
import com.mppay.service.service.IUserRealNameDetailsService;
import com.mppay.service.service.IUserRealNameService;
import com.mppay.service.service.common.ICipherService;
import com.mppay.service.vo.ali.AliIdImagesVO;
import com.mppay.service.vo.ali.AliResult;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户实名认证
 */
@Service("userRealNameHandler")
@Slf4j
public class UserRealNameHandlerImpl extends BaseBusiHandlerImpl implements BaseBusiHandler {

    @Autowired
    private IUserRealNameDetailsService userRealNameDetailsService;

    @Autowired
    private IUserRealNameService userRealNameService;

    @Autowired
    private IDictionaryService dictionaryService;
    @Autowired
    private IAliResourcesService aliResourcesService;
    @Autowired
    private IUserOperService iUserOperService;
    @Value("${shop.service.hostname}")
    private String host;
    @Value("${shop.service.interiorOssResouces}")
    private String ossResouces;
    @Value("${shop.service.stsToken}")
    private String stsToken;
    @Value("#{${mppay-key}}")
    private Map<String, String> mppayKey;
    @Autowired
    private ICipherService cipherServiceImpl;

    @Override
    public void doBusi(RequestMsg requestMsg, ResponseMsg responseMsg) throws Exception {
        log.info("用户实名|接收到请求报文:{}", JSONUtil.toJsonStr(requestMsg));
        UserRealNameDetails details = new UserRealNameDetails();
        BeanUtils.populate(details, requestMsg.getMap());
        String cardNo = details.getCardNo();
        String name = details.getName();

        String cardNoDe = cipherServiceImpl.decryptAES(cardNo);

        String imgFront = details.getImgFront();
        String imgBack = details.getImgBack();
        String userOperNo = (String) requestMsg.get("userOperNo");
        String realType = details.getRealType(); // 0：用户本人实名 1：订购人实名

        AliIdImagesVO idiFront = null;
        AliIdImagesVO idiBack = null;

        // 默认软实名
        Integer realed = 1;
        // 弱实名
        if (StringUtils.isNotBlank(imgFront) && StringUtils.isNotBlank(imgBack)) {
            realed = RealedStatus.WEAK_REAL.getId();
        }

        UserOper userOper = iUserOperService
                .getOne(new QueryWrapper<UserOper>().eq("user_oper_no", userOperNo).eq("status", "1"));
        Optional.ofNullable(userOper).orElseThrow(() -> new BusiException(11309));

        log.info("用户实名|强弱实名|实名用户姓名:{}", name);
        //新用户实名、实名标识小于弱实名、订购人实名，身份证相片校验
        if (userOper.getRealed() < RealedStatus.WEAK_REAL.getId()
                || RealType.BUYER.getId() == Integer.parseInt(realType)) {
            // 正面
            if (StringUtils.isNotBlank(imgFront)) {
                String url = host + ossResouces + "?iconUrl=" + imgFront;
                idiFront = aliResourcesService.idimages(url, true);
                Optional.ofNullable(idiFront).orElseThrow(() -> new BusiException(31105));
                AliResult result = idiFront.getResult();
                Optional.ofNullable(result).orElseThrow(() -> new BusiException(31105));
                Optional.ofNullable(result.getCode()).orElseThrow(() -> new BusiException(31105));
                // 证件号
                if (!cardNoDe.equalsIgnoreCase(result.getCode())) {
                    throw new BusiException(31105);
                }
                // 名字
                if (!name.equalsIgnoreCase(result.getName())) {
                    throw new BusiException(31105);
                }
            }
            // 反面
            if (StringUtils.isNotBlank(imgBack)) {
                String url = host + ossResouces + "?iconUrl=" + imgBack;
                idiBack = aliResourcesService.idimages(url, false);
                Optional.ofNullable(idiBack).orElseThrow(() -> new BusiException(31105));
                AliResult result = idiBack.getResult();
                String expiryDate = result.getExpiryDate(); // 有效期
                if (!"长期".equals(expiryDate)) {
                    Date date = DateTimeUtil.formatStringToDate(expiryDate, DateUtil.DATEFORMAT_10);
                    if (new Date().after(date)) {
                        throw new BusiException(31105);
                    }
                }
            }
        }

        UserRealName userRealName = userRealNameService
                .getOne(new QueryWrapper<UserRealName>().eq("card_no", cardNo).eq("name", name).eq("status", 1).last("limit 1"));
        // 已经实名过
        log.info("用户实名|是否已经实名|实名用户姓名:{}", name);
        if (null != userRealName) {

            //实名标识<弱实名,则更新用户实名
            if (userRealName.getRealed() < RealedStatus.WEAK_REAL.getId()) {
                userRealName.setRealed(realed);
                userRealName.setImgBack(details.getImgBack());
                userRealName.setImgFront(details.getImgFront());
                userRealNameService.updateById(userRealName);
            }

            // 保存details 操作记录表
            userRealName.setId(null);
            userRealName.setCreateTime(null);
            userRealName.setUpdateTime(null);
            org.springframework.beans.BeanUtils.copyProperties(userRealName, details);
            details.setRealType(realType);
            details.setRealDate(DateTimeUtil.date10());
            details.setRealTime(DateTimeUtil.time8());
            details.setReturnCode(ConstEC.SUCCESS_10000);
            details.setReturnMsg(ConstEC.SUCCESS_MSG);
            userRealNameDetailsService.save(details);

            if (RealType.USER.getId() == Integer.parseInt(realType) && userOper.getRealed() < realed) {
                // 更新下userOper
                userOper.setCardNo(details.getCardNo());
                userOper.setName(details.getName());
                userOper.setCardType(details.getCardType());
                userOper.setRealed(realed);
                iUserOperService.updateById(userOper);
            }

            responseMsg.put(ConstEC.DATA, details);
            responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
            responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
            return;
        }

        log.info("用户实名|未实名|实名用户姓名:{}", name);
        // 未实名的
        userRealNameDetailsService.save(details);
        // 校验身份证号
        Map<String, Object> userRealMap = aliResourcesService.realName(name, cardNoDe);
        String status = (String) userRealMap.get("status");
        String msg = (String) userRealMap.get("msg");
        // 状态不正确时抛错
        if (null == status || !"01".equals(status)) {
            details.setRealDate(DateTimeUtil.date10());
            details.setRealTime(DateTimeUtil.time8());
            details.setReturnCode(status);
            details.setReturnMsg(msg);
            userRealNameDetailsService.updateById(details);
            throw new BusiException("11306", msg == null ? ApplicationYmlUtil.get("11306") : msg);
        }

        // 转成userRealName
        userRealName = new UserRealName();
        org.springframework.beans.BeanUtils.copyProperties(details, userRealName);
        userRealName.setName(name);
        userRealName.setCardNo(cardNo);
        userRealName.setAddress(userRealMap.get("area").toString());
        userRealName.setProvince(userRealMap.get("province").toString());
        userRealName.setCity(userRealMap.get("city").toString());
        userRealName.setCountry(userRealMap.get("prefecture").toString());
        userRealName.setBirthday(userRealMap.get("birthday").toString());
        userRealName.setAddressCode(userRealMap.get("addrCode").toString());
        userRealName.setLastCode(userRealMap.get("lastCode").toString());
        userRealName.setRealDate(DateTimeUtil.date10());
        userRealName.setRealTime(DateTimeUtil.time8());
        userRealName.setStatus(1);
        userRealName.setRealed(realed);
        userRealName.setCardType(Integer.parseInt(requestMsg.get("cardType").toString()));
        String sex = userRealMap.get("sex").toString();

        if (null != sex) {
            if ("男".equals(sex)) {
                userRealName.setGender(1);
            } else if ("女".equals(sex)) {
                userRealName.setGender(2);
            }
        }
        String cardNoAbbr = cardNoDe;
        cardNoAbbr = cardNoAbbr.substring(0, 3) + "******"
                + cardNoAbbr.substring(cardNoAbbr.length() - 4, cardNoAbbr.length());
        userRealName.setCardNoAbbr(cardNoAbbr);
        org.springframework.beans.BeanUtils.copyProperties(userRealName, details);

        details.setReturnCode(ConstEC.SUCCESS_10000);
        details.setReturnMsg(ConstEC.SUCCESS_MSG);

        // 更新表数据
        userRealNameService.save(userRealName);
        userRealNameDetailsService.updateById(details);

        if (RealType.USER.getId() == Integer.parseInt(realType) && userOper.getRealed() < realed) {
            // 更新下userOper
            userOper.setCardNo(details.getCardNo());
            userOper.setCardNoAbbr(cardNoAbbr);
            userOper.setName(details.getName());
            userOper.setCardType(details.getCardType());
            userOper.setRealed(realed);
            iUserOperService.updateById(userOper);
        }

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("mercId", details.getMercId());
        data.put("platform", details.getPlatform());
        data.put("name", name);
        data.put("gender", details.getGender());
        data.put("cardType", details.getCardType());
        data.put("cardNo", cardNo);
        data.put("cardNoAbbr", details.getCardNoAbbr());
        data.put("status", details.getStatus());
        data.put("addressCode", details.getAddressCode());
        data.put("lastCode", details.getLastCode());
        data.put("imgFront", details.getImgFront());
        data.put("imgBack", details.getImgBack());
        data.put("realDate", details.getRealDate());
        data.put("realTime", details.getRealTime());

        responseMsg.put(ConstEC.DATA, data);
        responseMsg.put(ConstEC.RETURNCODE, ConstEC.SUCCESS_10000);
        responseMsg.put(ConstEC.RETURNMSG, ConstEC.SUCCESS_MSG);
    }

}
