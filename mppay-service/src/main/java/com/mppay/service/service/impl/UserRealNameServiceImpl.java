package com.mppay.service.service.impl;

import com.mppay.service.entity.UserRealName;
import com.mppay.service.mapper.UserRealNameMapper;
import com.mppay.service.service.IUserRealNameService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 用户实名认证信息表 服务实现类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-19
 */
@Service
public class UserRealNameServiceImpl extends ServiceImpl<UserRealNameMapper, UserRealName> implements IUserRealNameService {

    /**
     * 根据姓名,证件类型,证件号查询
     *
     * @param userRealNames
     * @return
     */
    @Override
    public List<UserRealName> listByNameAndCarcNoAndCardType(List<UserRealName> userRealNames) {
        return baseMapper.listByNameAndCarcNoAndCardType(userRealNames);
    }
}
