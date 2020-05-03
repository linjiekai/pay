package com.mppay.service.service.impl;

import com.mppay.service.entity.TradeRefundAlipay;
import com.mppay.service.entity.TradeRefundGaohuitong;
import com.mppay.service.mapper.TradeRefundGaohuitongMapper;
import com.mppay.service.service.ITradeRefundGaohuitongService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * <p>
 * 高汇通交易退款流水表 服务实现类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-09-05
 */
@Service
public class TradeRefundGaohuitongServiceImpl extends ServiceImpl<TradeRefundGaohuitongMapper, TradeRefundGaohuitong> implements ITradeRefundGaohuitongService {


    @Override
    public Map<String, Object> statPrice(TradeRefundGaohuitong tradeRefundAlipay) {
        return baseMapper.statPrice(tradeRefundAlipay);
    }
}
