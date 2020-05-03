package com.mppay.service.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mppay.service.entity.MercOrder;
import com.mppay.service.mapper.MercOrderMapper;
import com.mppay.service.service.IMercOrderService;

/**
 * <p>
 * 商户订单表 服务实现类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-04-16
 */
@Service
public class MercOrderServiceImpl extends ServiceImpl<MercOrderMapper, MercOrder> implements IMercOrderService {

}
