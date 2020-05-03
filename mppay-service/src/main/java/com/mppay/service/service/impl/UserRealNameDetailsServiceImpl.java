package com.mppay.service.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.utils.ResponseUtil;
import com.mppay.service.service.common.ICipherService;
import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mppay.service.entity.UserRealNameDetails;
import com.mppay.service.mapper.UserRealNameDetailsMapper;
import com.mppay.service.service.IUserRealNameDetailsService;

/**
 * <p>
 * 用户实名认证信息明细表 服务实现类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-14
 */
@Service
public class UserRealNameDetailsServiceImpl extends ServiceImpl<UserRealNameDetailsMapper, UserRealNameDetails> implements IUserRealNameDetailsService {

    @Autowired
    private ICipherService cipherServiceImpl;

    @Override
    public IPage<UserRealNameDetails> page(Page<UserRealNameDetails> ipage, Map<String, Object> params) {
        return baseMapper.page(ipage, params);
    }

    /**
     * 根据条件分页查询
     *
     * @param msgMap
     * @return
     */
    @Override
    public Object pageByCondition(Map<String, Object> msgMap) {
        Integer page = msgMap.get("page") == null ? 1 : Integer.parseInt(msgMap.get("page").toString());
        Integer limit = msgMap.get("limit") == null ? 10 : Integer.parseInt(msgMap.get("limit").toString());
        Page<UserRealNameDetails> pageCond = new Page<>(page, limit);
        String mercId = (String) msgMap.get("mercId");
        Long userId = msgMap.get("userId") == null ? null : Long.parseLong(msgMap.get("userId").toString());
        String cardNo = (String) msgMap.get("cardNo");
        Integer realSource = (Integer) msgMap.get("realSource");
        QueryWrapper<UserRealNameDetails> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("user_id", "nickname", "name", "gender", "card_type",
                "card_no", "card_no_abbr", "status", "birthday", "address_code",
                "last_code", "real_date", "real_time", "real_source", "sys_cnl", "img_front", "img_back");
        queryWrapper.eq("merc_id", mercId);
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        if (StringUtils.isNotBlank(cardNo)) {
            String cardNoCipher = cipherServiceImpl.encryptAES(cardNo);
            queryWrapper.eq("card_no", cardNoCipher);
        }
        if (realSource != null) {
            queryWrapper.eq("real_source", realSource);
        }
        queryWrapper.orderByDesc(" create_time ");
        IPage<UserRealNameDetails> detailsIPage = page(pageCond, queryWrapper);

        // 银行卡解密处理
        List<UserRealNameDetails> records = detailsIPage.getRecords();
        records.forEach(record -> {
            String cardNoCipherTemp = record.getCardNo();
            if (StringUtils.isNotBlank(cardNoCipherTemp)) {
                String cardNoPlain = cipherServiceImpl.decryptAES(cardNoCipherTemp);
                record.setCardNo(cardNoPlain);
            }
        });

        Map<String, Object> data = new HashMap<>();
        data.put("total", detailsIPage.getTotal());
        data.put("items", records);
        return ResponseUtil.ok(data);
    }

}
