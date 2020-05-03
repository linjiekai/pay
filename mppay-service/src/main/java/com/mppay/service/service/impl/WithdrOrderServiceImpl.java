package com.mppay.service.service.impl;

import com.mppay.service.entity.WithdrOrder;
import com.mppay.service.mapper.WithdrOrderMapper;
import com.mppay.service.service.IWithdrOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 提现订单表 服务实现类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-03
 */
@Slf4j
@Service
public class WithdrOrderServiceImpl extends ServiceImpl<WithdrOrderMapper, WithdrOrder> implements IWithdrOrderService {

}
