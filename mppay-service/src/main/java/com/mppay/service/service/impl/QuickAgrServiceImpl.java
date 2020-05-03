package com.mppay.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mppay.core.constant.QuickAgrStatus;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.AESCoder;
import com.mppay.core.utils.ResponseUtil;
import com.mppay.service.entity.QuickAgr;
import com.mppay.service.mapper.QuickAgrMapper;
import com.mppay.service.service.IDictionaryService;
import com.mppay.service.service.IQuickAgrService;
import com.mppay.service.service.common.ICipherService;
import com.mppay.service.vo.QuickAgrBankVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 快捷签约协议表 服务实现类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-09-06
 */
@Slf4j
@Service
public class QuickAgrServiceImpl extends ServiceImpl<QuickAgrMapper, QuickAgr> implements IQuickAgrService {

    @Autowired
    private IDictionaryService dictionaryService;
    @Autowired
    private ICipherService iCipherService;

    @Override
    public List<QuickAgrBankVO> findQuickAgrBank(Map<String, Object> params) {
        return baseMapper.findQuickAgrBank(params);
    }

    /**
     * 查询[快捷]银行卡信息
     *
     * @param map userId    用户id
     *            mobile    手机号
     *            page      页码
     *            limit     页面大小
     * @return
     * @throws Exception
     */
    @Override
    public Object quickCardBindList(Map<String, Object> map) throws Exception {
        log.info("|查询快捷银行卡信息|接收到请求报文:{}|", map);
        Integer page = map.get("page") == null ? 1 : Integer.parseInt(map.get("page").toString());
        Integer limit = map.get("limit") == null ? 10 : Integer.parseInt(map.get("limit").toString());
        Integer userId = (Integer) map.get("userId");
        String mercId = (String) map.get("mercId");
        String mobile = (String) map.get("mobile");
        Page<QuickAgr> pageCond = new Page<>(page, limit);
        QueryWrapper<QuickAgr> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", QuickAgrStatus.NORMAL.getId());
        queryWrapper.eq("merc_id", mercId);
        if (null != userId) {
            queryWrapper.eq("user_id", userId);
        }
        if (StringUtils.isNotBlank(mobile)) {
            String mobileEncrypt;
            try {
                mobileEncrypt = iCipherService.encryptAES(mobile);
            } catch (Exception e) {
                log.error("|查询快捷银行卡信息|手机号加密失败|");
                throw new BusiException(31114);
            }
            queryWrapper.eq("mobile", mobileEncrypt);
        }
        queryWrapper.orderByDesc("create_time");
        IPage<QuickAgr> quickAgrIPage = page(pageCond, queryWrapper);
        List<Map<String, Object>> items = new ArrayList<>();
        List<QuickAgr> quickAgrs = quickAgrIPage.getRecords();
        for (QuickAgr quickAgr : quickAgrs) {
            Map<String, Object> item = new HashMap<>();
            item.put("userId", quickAgr.getUserId());
            item.put("updateTime", quickAgr.getUpdateTime());
            String mobileCipher = quickAgr.getMobile();
            String cardNoCipher = quickAgr.getCardNo();
            String bankCardNoCipher = quickAgr.getBankCardNo();
            try {
                String mobileAbbr =  iCipherService.decryptAES(mobileCipher);
                String cardNoAbbr =iCipherService.decryptAES(cardNoCipher);
                String bankCardNoAbbr =iCipherService.decryptAES(bankCardNoCipher);
                if(StringUtils.isNotBlank(mobileAbbr)){
                    mobileAbbr = mobileAbbr.substring(0, 3) + "****" + mobileAbbr.substring(mobileAbbr.length() - 4);
                }
                if(StringUtils.isNotBlank(cardNoAbbr)){
                    cardNoAbbr = cardNoAbbr.substring(0, cardNoAbbr.length() - 4) + "****";
                }
                if(StringUtils.isNotBlank(bankCardNoAbbr)){
                    bankCardNoAbbr = bankCardNoAbbr.substring(0, bankCardNoAbbr.length() - 4) + "****";
                }
                item.put("mobile", mobileAbbr);
                item.put("cardNo", cardNoAbbr);
                item.put("bankCardNo", bankCardNoAbbr);
            } catch (Exception e) {
                log.error("|查询快捷支付银行卡信息|解密失败,手机号密文:{},身份证密文:{},银行卡密文:{}", mobileCipher, cardNoCipher, bankCardNoCipher);
                throw new BusiException(31102);
            }
            items.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("total", quickAgrIPage.getTotal());
        data.put("items", items);
        return ResponseUtil.ok(data);
    }

}
