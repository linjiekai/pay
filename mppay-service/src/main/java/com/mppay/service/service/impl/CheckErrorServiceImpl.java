package com.mppay.service.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mppay.service.entity.CheckError;
import com.mppay.service.mapper.CheckErrorMapper;
import com.mppay.service.service.ICheckErrorService;

/**
 * <p>
 * 对账差错表 服务实现类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-04-16
 */
@Service
public class CheckErrorServiceImpl extends ServiceImpl<CheckErrorMapper, CheckError> implements ICheckErrorService {

}
