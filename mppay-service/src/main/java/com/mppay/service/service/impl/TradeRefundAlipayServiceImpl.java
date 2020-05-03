package com.mppay.service.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mppay.service.entity.TradeRefundAlipay;
import com.mppay.service.mapper.TradeRefundAlipayMapper;
import com.mppay.service.service.ITradeRefundAlipayService;

/**
 * <p>
 * 支付宝交易退款流水表 服务实现类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-04-02
 */
@Service
public class TradeRefundAlipayServiceImpl extends ServiceImpl<TradeRefundAlipayMapper, TradeRefundAlipay> implements ITradeRefundAlipayService {

	@Override
	public Map<String, Object> statPrice(TradeRefundAlipay tradeRefundAlipay) {
		return baseMapper.statPrice(tradeRefundAlipay);
	}

}
