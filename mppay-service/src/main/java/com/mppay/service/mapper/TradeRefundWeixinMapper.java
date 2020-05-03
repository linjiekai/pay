package com.mppay.service.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mppay.service.entity.TradeRefundWeixin;

/**
 * <p>
 * 微信交易退款流水表 Mapper 接口
 * </p>
 *
 * @author chenfeihang
 * @since 2019-04-02
 */
public interface TradeRefundWeixinMapper extends BaseMapper<TradeRefundWeixin> {

	@Select("select sum(price) total_price, count(1) counts "
			+ " from trade_refund_weixin where refund_date = #{refundDate} and check_status=#{checkStatus}"
			+ " and order_status=#{orderStatus}"
			)
	Map<String, Object> statPrice(TradeRefundWeixin tradeRefundWeixin);

}
