package com.mppay.service.mapper;

import com.mppay.service.entity.TradeRefundAlipay;
import com.mppay.service.entity.TradeRefundGaohuitong;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * <p>
 * 高汇通交易退款流水表 Mapper 接口
 * </p>
 *
 * @author chenfeihang
 * @since 2019-09-05
 */
public interface TradeRefundGaohuitongMapper extends BaseMapper<TradeRefundGaohuitong> {

    @Select("select sum(price) total_price, count(1) counts "
            + " from trade_refund_gaohuitong where refund_date = #{refundDate} and check_status=#{checkStatus}"
            + " and order_status=#{orderStatus}"
    )
    Map<String, Object> statPrice(TradeRefundGaohuitong tradeRefundAlipay);
}
