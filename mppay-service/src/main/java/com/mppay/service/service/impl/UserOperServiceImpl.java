package com.mppay.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.constant.*;
import com.mppay.core.exception.BusiException;
import com.mppay.service.entity.CardBind;
import com.mppay.service.entity.UserAgrInfo;
import com.mppay.service.entity.UserOper;
import com.mppay.service.mapper.UserOperMapper;
import com.mppay.service.service.ICardBindService;
import com.mppay.service.service.IUserAgrInfoService;
import com.mppay.service.service.IUserOperService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户操作基础信息表 服务实现类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-05-23
 */
@Slf4j
@Service
public class UserOperServiceImpl extends ServiceImpl<UserOperMapper, UserOper> implements IUserOperService {

    @Autowired
    private IUserAgrInfoService userAgrInfoService;
    @Autowired
    private ICardBindService cardBindService;

    /**
     * 取消实名
     *
     * @param reqMap userId 用户id
     *               mercId 商户号
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelRealname(Map<String, Object> reqMap) {
        // 1. 修改 UserOper 实名状态为:未实名
        UserOper userOper = getOne(new QueryWrapper<UserOper>()
                .eq("user_id", reqMap.get("userId"))
                .eq("merc_id", reqMap.get("mercId"))
                .eq("status", UserOperStatus.NORMAL.getId()));
        if (userOper == null) {
            log.error("|取消实名|异常|无用户操作基础信息,mercId:{},userId:{}", reqMap.get("mercId"), reqMap.get("userId"));
            throw new BusiException(11001);
        }
        userOper.setRealed(RealedStatus.UNREAL.getId());
        userOper.setCardType(CardType.ID_CARD.getId());
        updateById(userOper);

        // 2. 将用户协议基础信息表[user_agr_info]状态修改为:解约
        String userOperNo = userOper.getUserOperNo();
        List<UserAgrInfo> userAgrInfos = userAgrInfoService.list(new QueryWrapper<UserAgrInfo>()
                .eq("user_oper_no", userOperNo)
                .eq("status", UserAgrInfoStatus.NORMAL.getId()));
        if (userAgrInfos != null && userAgrInfos.size() > 0) {
            userAgrInfos.forEach(userAgrInfo -> {
                userAgrInfo.setStatus(UserAgrInfoStatus.CANCEL.getId());
            });
            userAgrInfoService.updateBatchById(userAgrInfos);
        }

        // 3. card_bind 状态修改为:解绑
        List<CardBind> cardBinds = cardBindService.list(new QueryWrapper<CardBind>()
                .eq("user_oper_no", userOperNo)
                .eq("status", CardBindStatus.BINDING.getId()));
        if (cardBinds != null && cardBinds.size() > 0) {
            cardBinds.forEach(cardBind -> {
                cardBind.setStatus(CardBindStatus.UNBINDING.getId());
            });
            cardBindService.updateBatchById(cardBinds);
        }

    }
}
