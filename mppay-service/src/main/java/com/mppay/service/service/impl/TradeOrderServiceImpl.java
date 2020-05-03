package com.mppay.service.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mppay.service.entity.TradeOrder;
import com.mppay.service.mapper.TradeOrderMapper;
import com.mppay.service.service.ITradeOrderService;

/**
 * <p>
 * 交易订单表 服务实现类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-04-16
 */
@Service
public class TradeOrderServiceImpl extends ServiceImpl<TradeOrderMapper, TradeOrder> implements ITradeOrderService {

}
