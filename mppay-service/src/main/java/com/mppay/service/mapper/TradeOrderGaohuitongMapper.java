package com.mppay.service.mapper;

import com.mppay.service.entity.TradeOrderAlipay;
import com.mppay.service.entity.TradeOrderGaohuitong;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * <p>
 * 高汇通交易订单流水表 Mapper 接口
 * </p>
 *
 * @author chenfeihang
 * @since 2019-09-05
 */
public interface TradeOrderGaohuitongMapper extends BaseMapper<TradeOrderGaohuitong> {

    @Select("select sum(price) total_price, count(1) counts "
            + " from trade_order_gaohuitong where trade_date = #{tradeDate} and check_status=#{checkStatus}"
            + " and order_status=#{orderStatus}"
    )
    Map<String, Object> statPrice(TradeOrderGaohuitong tradeOrderAlipay);
}
