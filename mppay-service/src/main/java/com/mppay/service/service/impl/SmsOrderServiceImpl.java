package com.mppay.service.service.impl;

import com.mppay.service.entity.SmsOrder;
import com.mppay.service.mapper.SmsOrderMapper;
import com.mppay.service.service.ISmsOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 短信订单表 服务实现类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-09-06
 */
@Service
public class SmsOrderServiceImpl extends ServiceImpl<SmsOrderMapper, SmsOrder> implements ISmsOrderService {

}
