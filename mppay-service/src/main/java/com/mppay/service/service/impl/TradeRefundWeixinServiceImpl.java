package com.mppay.service.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mppay.service.entity.TradeRefundWeixin;
import com.mppay.service.mapper.TradeRefundWeixinMapper;
import com.mppay.service.service.ITradeRefundWeixinService;

/**
 * <p>
 * 微信交易退款流水表 服务实现类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-04-02
 */
@Service
public class TradeRefundWeixinServiceImpl extends ServiceImpl<TradeRefundWeixinMapper, TradeRefundWeixin> implements ITradeRefundWeixinService {

	@Override
	public Map<String, Object> statPrice(TradeRefundWeixin tradeRefundWeixin) {
		return baseMapper.statPrice(tradeRefundWeixin);
	}

}
