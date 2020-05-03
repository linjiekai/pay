package com.mppay.service.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mppay.core.constant.BankCardType;
import com.mppay.core.constant.CardBindStatus;
import com.mppay.core.constant.CardType;
import com.mppay.core.constant.UserRealNameStatus;
import com.mppay.core.exception.BusiException;
import com.mppay.core.utils.ResponseUtil;
import com.mppay.service.entity.UserRealName;
import com.mppay.service.service.IUserRealNameService;
import com.mppay.service.service.common.ICipherService;
import com.mppay.service.vo.CardBindLastBindVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mppay.service.entity.CardBind;
import com.mppay.service.mapper.CardBindMapper;
import com.mppay.service.service.ICardBindService;
import com.mppay.service.vo.CardBindVO;

/**
 * <p>
 * 绑卡表 服务实现类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-05-29
 */
@Service
@Slf4j
public class CardBindServiceImpl extends ServiceImpl<CardBindMapper, CardBind> implements ICardBindService {

    @Autowired
    private ICipherService iCipherService;
    @Autowired
    private IUserRealNameService userRealNameService;

    @Override
    public List<CardBindVO> withdrBankList(Map<String, Object> map) {
        return baseMapper.withdrBankList(map);
    }

    @Override
    public List<CardBindVO> cardBindList(Map<String, Object> map) {
        return baseMapper.cardBindList(map);
    }

    /**
     * 提现绑卡信息
     *
     * @param map
     * @return
     */
    @Override
    public Object withdrCardBindList(Map<String, Object> map) throws Exception {
        log.info("|查询提现银行卡信息|接收到请求报文:{}|", map);
        Integer page = map.get("page") == null ? 1 : Integer.parseInt(map.get("page").toString());
        Integer limit = map.get("limit") == null ? 10 : Integer.parseInt(map.get("limit").toString());
        Integer userId = (Integer) map.get("userId");
        String mercId = (String) map.get("mercId");
        String mobile = (String) map.get("mobile");
        Page<CardBind> pageCond = new Page<>(page, limit);
        QueryWrapper<CardBind> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", CardBindStatus.BINDING.getId());
        queryWrapper.eq("bank_card_type", BankCardType.DEBIT.getId());
        queryWrapper.eq("merc_id", mercId);
        if (null != userId) {
            queryWrapper.eq("user_id", userId);
        }
        if (StringUtils.isNotBlank(mobile)) {
            String mobileEncrypt;
            try {
                mobileEncrypt =iCipherService.encryptAES(mobile);
            } catch (Exception e) {
                log.error("|查询提现银行卡信息|手机号加密失败|");
                throw new BusiException(31114);
            }
            queryWrapper.eq("mobile", mobileEncrypt);
        }
        queryWrapper.orderByDesc("create_time");
        IPage<CardBind> cardBindIPage = page(pageCond, queryWrapper);
        List<Map<String, Object>> items = new ArrayList<>();
        List<CardBind> cardBinds = cardBindIPage.getRecords();

        if (cardBinds != null && cardBinds.size() > 0) {
            // 获取实名认证信息
            List<UserRealName> realNames = new ArrayList<>();
            cardBinds.forEach(cardBind -> {
                UserRealName userRealName = new UserRealName();
                userRealName.setName(cardBind.getBankCardName());
                userRealName.setCardType(cardBind.getCardType());
                userRealName.setCardNo(cardBind.getCardNo());
                userRealName.setStatus(UserRealNameStatus.REAL.getId());
                realNames.add(userRealName);
            });
            List<UserRealName> userRealNames = userRealNameService.listByNameAndCarcNoAndCardType(realNames);

            cardBinds.forEach(cardBind -> {
                Map<String, Object> item = new HashMap<>();
                item.put("userId", cardBind.getUserId());
                item.put("bankCardImgFront", cardBind.getBankCardImgFront());
                item.put("updateTime", cardBind.getUpdateTime());
                String mobileCipher = cardBind.getMobile();
                String cardNoCipher = cardBind.getCardNo();
                String bankCardNoCipher = cardBind.getBankCardNo();
                try {

                    String mobileAbbr = iCipherService.decryptAES(mobileCipher);
                    String cardNoAbbr = iCipherService.decryptAES(cardNoCipher);
                    String bankCardNoAbbr = iCipherService.decryptAES(bankCardNoCipher);
                    mobileAbbr = mobileAbbr.substring(0, 3) + "****" + mobileAbbr.substring(mobileAbbr.length() - 4);
                    cardNoAbbr = cardNoAbbr.substring(0, cardNoAbbr.length() - 4) + "****";
                    bankCardNoAbbr = bankCardNoAbbr.substring(0, bankCardNoAbbr.length() - 4) + "****";
                    item.put("mobile", mobileAbbr);
                    item.put("cardNo", cardNoAbbr);
                    item.put("bankCardNo", bankCardNoAbbr);
                } catch (Exception e) {
                    log.error("|查询提现银行卡信息|解密失败,手机号密文:{},身份证密文:{},银行卡密文:{}", mobileCipher, cardNoCipher, bankCardNoCipher);
                    throw new BusiException(31102);
                }
                if (userRealNames != null && userRealNames.size() > 0) {
                    userRealNames.forEach(userRealTemp -> {
                        if (cardBind.getBankCardName().equals(userRealTemp.getName()) &&
                                cardBind.getCardNo().equals(userRealTemp.getCardNo()) &&
                                cardBind.getCardType().equals(userRealTemp.getCardType())) {
                            item.put("imgFront", userRealTemp.getImgFront());
                            item.put("imgBack", userRealTemp.getImgBack());
                        }
                    });
                }
                items.add(item);
            });
        }
        Map<String, Object> data = new HashMap<>();
        data.put("total", cardBindIPage.getTotal());
        data.put("items", items);
        return ResponseUtil.ok(data);
    }

    /**
     * 根据用户id查询,用户最近的绑卡信息
     *
     * @param map
     * @return
     */
    @Override
    public List<CardBindLastBindVO> lastBindByUserIds(Map<String, Object> map) {
        log.info("|根据用户id查询最后的绑卡信息|接收到请求报文:{}", map);
        List<Integer> userIds = (List<Integer>) map.get("userIds");
        if (userIds == null || userIds.size() < 1) {
            return new ArrayList<>();
        }
        String mercId = (String) map.get("mercId");
        map.put("status", CardBindStatus.BINDING.getId());
        List<CardBind> cardBinds = baseMapper.lastBindByUserIds(CardBindStatus.BINDING.getId(), BankCardType.DEBIT.getId(), mercId, userIds);
        List<CardBindLastBindVO> lastBindVOS = new ArrayList<>();
        CardBindLastBindVO lastBindVO;
        for (CardBind cardBind : cardBinds) {
            lastBindVO = new CardBindLastBindVO();
            BeanUtil.copyProperties(cardBind, lastBindVO);
            lastBindVO.setCardTypeName(CardType.getNameById(lastBindVO.getCardType()));
            lastBindVOS.add(lastBindVO);
        }
        return lastBindVOS;
    }

    /**
     * 根据协议号查询绑定信息
     *
     * @param cardBind
     * @return
     */
    @Override
    public CardBind getCardBindByAgrNo(CardBind cardBind) {
        String agrNo = cardBind.getAgrNo();
        try {
            agrNo = iCipherService.decryptAES(agrNo);
        } catch(Exception e) {
            log.error("|获取卡绑定信息|协议号解密失败,原协议号:{}|", agrNo);
            e.printStackTrace();
            throw new BusiException("协议号解密失败");
        }
        return this.getOne(new QueryWrapper<CardBind>()
                .eq("merc_id",cardBind.getMercId())
                .eq("user_id", cardBind.getUserId())
                .eq("agr_no", agrNo)
                .eq("status", CardBindStatus.BINDING.getId()));
    }

}
