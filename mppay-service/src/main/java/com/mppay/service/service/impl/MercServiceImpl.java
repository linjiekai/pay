package com.mppay.service.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mppay.service.entity.Merc;
import com.mppay.service.mapper.MercMapper;
import com.mppay.service.service.IMercService;

/**
 * <p>
 * 商户订单表 服务实现类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-04-16
 */
@Service
public class MercServiceImpl extends ServiceImpl<MercMapper, Merc> implements IMercService {

}
