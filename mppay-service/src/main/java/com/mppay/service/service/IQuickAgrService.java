package com.mppay.service.service;

import com.mppay.service.entity.QuickAgr;
import com.mppay.service.vo.QuickAgrBankVO;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 快捷签约协议表 服务类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-09-06
 */
public interface IQuickAgrService extends IService<QuickAgr> {

    List<QuickAgrBankVO> findQuickAgrBank(Map<String, Object> params);

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
    Object quickCardBindList(Map<String, Object> map) throws Exception;
}
