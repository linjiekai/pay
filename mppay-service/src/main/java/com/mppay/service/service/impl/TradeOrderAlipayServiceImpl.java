package com.mppay.service.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mppay.service.entity.TradeOrderAlipay;
import com.mppay.service.mapper.TradeOrderAlipayMapper;
import com.mppay.service.service.ITradeOrderAlipayService;

/**
 * <p>
 * 支付宝交易订单流水表 服务实现类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-04-01
 */
@Service
public class TradeOrderAlipayServiceImpl extends ServiceImpl<TradeOrderAlipayMapper, TradeOrderAlipay> implements ITradeOrderAlipayService {

	@Override
	public Map<String, Object> statPrice(TradeOrderAlipay tradeOrderAlipay) {
		return baseMapper.statPrice(tradeOrderAlipay);
	}

}
