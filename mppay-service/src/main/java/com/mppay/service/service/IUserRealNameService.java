package com.mppay.service.service;

import com.mppay.service.entity.UserRealName;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户实名认证信息表 服务类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-19
 */
public interface IUserRealNameService extends IService<UserRealName> {

    /**
     * 根据姓名,证件类型,证件号查询
     * @param userRealNames
     * @return
     */
    List<UserRealName> listByNameAndCarcNoAndCardType(List<UserRealName> userRealNames);

}
