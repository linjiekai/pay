package com.mppay.service.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mppay.service.entity.PrePayOrder;
import com.mppay.service.mapper.PrePayOrderMapper;
import com.mppay.service.service.IPrePayOrderService;

/**
 * <p>
 * 预支付交易表 服务实现类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-04-16
 */
@Service
public class PrePayOrderServiceImpl extends ServiceImpl<PrePayOrderMapper, PrePayOrder> implements IPrePayOrderService {

}
