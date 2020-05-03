package com.mppay.service.service;

import com.mppay.service.entity.UserOper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 用户操作基础信息表 服务类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-05-23
 */
public interface IUserOperService extends IService<UserOper> {

    /**
     * 取消实名
     *
     * @param reqMap 用户id
     * @return
     */
    void cancelRealname(Map<String, Object> reqMap);
}
