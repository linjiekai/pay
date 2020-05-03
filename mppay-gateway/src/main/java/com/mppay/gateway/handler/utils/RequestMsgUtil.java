package com.mppay.gateway.handler.utils;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.BankCardType;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.gaohuitong.GaohuitongConstants;
import com.mppay.core.utils.RegularUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.service.entity.RouteConf;
import com.mppay.service.entity.UserAgrInfo;
import com.mppay.service.service.IRouteConfService;
import com.mppay.service.service.IUserAgrInfoService;
import com.mppay.service.service.common.ICipherService;
import com.mppay.service.service.common.impl.CipherServiceImpl;
import com.mppay.service.service.impl.RouteConfServiceImpl;
import com.mppay.service.service.impl.UserAgrInfoServiceImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestMsgUtil {

    /**
     * @param :[requestMsg]
     * @return :void
     * @Description(描述): 校验下特殊参数
     * @auther: Jack Lin
     * @date: 2019/9/20 11:06
     */
    public static void validateRequestMsg(RequestMsg requestMsg) throws Exception {
        ICipherService cipherServiceImpl = (ICipherService) SpringContextHolder.getBean(CipherServiceImpl.class);
        String mobile = (String) requestMsg.get("mobile");
        String bankCardNo = (String) requestMsg.get("bankCardNo");
        String cardNo = (String) requestMsg.get("cardNo");
        String validDate = (String) requestMsg.get("validDate");
        String cvn2 = (String) requestMsg.get("cvn2");
        String bankCardType = (String) requestMsg.get("bankCardType");
        //匹配正则
        if (StringUtils.isNotEmpty(mobile)) {
            try {
                String decrypt = cipherServiceImpl.decryptAES(mobile);
                if (!RegularUtil.matches(decrypt, RegularUtil.REGULAR_MOBILE)) {
                    log.error("手机号格式不合法:{}",decrypt);
                    throw new BusiException(31109);
                }
            } catch (Exception e) {
                log.error("手机号解密失败：{}",mobile);
                throw new BusiException(31105);
            }
        }
        if (StringUtils.isNotEmpty(bankCardNo)) {
            try {
                String decrypt = cipherServiceImpl.decryptAES(bankCardNo);
                if (!BankCardType.THIRD.getId().equalsIgnoreCase(bankCardType) && !RegularUtil.matches(decrypt, RegularUtil.REGULAR_BANKCARDNO)) {
                    log.error("银行卡格式不合法:{}",decrypt);
                    throw new BusiException(31108);
                }
            } catch (Exception e) {
                log.error("银行卡解密失败：{}", bankCardNo);
                throw new BusiException(31105);
            }
        }
        if (StringUtils.isNotEmpty(cardNo)) {
            try {
                String decrypt = cipherServiceImpl.decryptAES(cardNo);
                if (!RegularUtil.matches(decrypt, RegularUtil.REGULAR_ID_CARD)) {
                    log.error("身份证格式不合法:{}",decrypt);
                    throw new BusiException(31107);
                }
            } catch (Exception e) {
                log.error("身份证解密失败:{}",cardNo);
                throw new BusiException(31105);
            }
        }

        if (StringUtils.isNotEmpty(bankCardType) && BankCardType.CREDIT.getId().equalsIgnoreCase(bankCardType)) {
            try {
                if (StringUtils.isNotEmpty(validDate)) {
                    String decrypt = cipherServiceImpl.decryptAES(validDate);
                    if (!RegularUtil.matches(decrypt, RegularUtil.REGULAR_VALIDDATE)) {
                        log.error("贷记卡有效期格式不合法:{}",decrypt);
                        throw new BusiException(31106);
                    }
                }
            } catch (Exception e) {
                log.error("贷记卡有效期解密失败:{}",validDate);
                throw new BusiException(31105);
            }
            try {
                if (StringUtils.isNotEmpty(cvn2)) {
                    String decrypt = cipherServiceImpl.decryptAES(cvn2);
                    if (!RegularUtil.matches(decrypt, RegularUtil.REGULAR_CVN2)) {
                        log.error("安全码格式不合法:{}",decrypt);
                        throw new BusiException(31110);
                    }
                }
            } catch (Exception e) {
                log.error("安全码解密失败:{}",cvn2);
                throw new BusiException(31105);
            }
        }
    }

    /**
     * @param :[mercId, agencyId, platform, tradeType]
     * @return :com.mppay.service.entity.RouteConf
     * @Description(描述): 获取路由信息
     * @auther: Jack Lin
     * @date: 2019/9/24 21:12
     */
    public static RouteConf getRouteConf(String mercId, String platform, String tradeType,String routeCode,String agencyId) throws Exception {
        IRouteConfService routeConfService = (IRouteConfService) SpringContextHolder.getBean(RouteConfServiceImpl.class);
        QueryWrapper<RouteConf> queryWrapper = new QueryWrapper<>();
        Optional.ofNullable(mercId).ifPresent(s->queryWrapper.eq("merc_id", s));
        Optional.ofNullable(platform).ifPresent(s->queryWrapper.eq("platform", s));
        Optional.ofNullable(tradeType).ifPresent(s->queryWrapper.eq("trade_type", s));
        Optional.ofNullable(routeCode).ifPresent(s->queryWrapper.eq("route_code", s));
        Optional.ofNullable(agencyId).ifPresent(s->queryWrapper.eq("bank_merc_id", s));
        queryWrapper.last(" limit 1");
        RouteConf one = routeConfService.getOne(queryWrapper);
        Optional.ofNullable(one).orElseThrow(() -> new BusiException(30002));
        return one;
    }

    /**
     * @Description(描述): 获取商户信息
     * @auther: Jack Lin
     * @param :[mercId, userOperNo]
     * @return :com.mppay.service.entity.UserAgrInfo
     * @date: 2019/10/23 15:12
     */
    public static UserAgrInfo getuserAgrInfo(String mercId,  String userOperNo) throws Exception {
    	IUserAgrInfoService userAgrInfoService = (IUserAgrInfoService) SpringContextHolder.getBean(UserAgrInfoServiceImpl.class);
        UserAgrInfo one = userAgrInfoService.getOne(new QueryWrapper<UserAgrInfo>()
                .eq("route_code", GaohuitongConstants.GHT_ROUTE)
                .eq("status", "1")
                .eq("user_oper_no", userOperNo)
                .eq("merc_id", mercId)
                .last(" limit 1"));
        Optional.ofNullable(one).orElseThrow(() -> new BusiException(11901));
        return one;
    }

}
