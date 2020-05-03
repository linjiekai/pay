package com.mppay.service.service.impl;

import com.mppay.service.entity.TradeOrderGaohuitong;
import com.mppay.service.entity.TradeOrderWeixin;
import com.mppay.service.mapper.TradeOrderGaohuitongMapper;
import com.mppay.service.service.ITradeOrderGaohuitongService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * <p>
 * 高汇通交易订单流水表 服务实现类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-09-05
 */
@Service
public class TradeOrderGaohuitongServiceImpl extends ServiceImpl<TradeOrderGaohuitongMapper, TradeOrderGaohuitong> implements ITradeOrderGaohuitongService {

    @Override
    public Map<String, Object> statPrice(TradeOrderGaohuitong tradeOrderWeixin) {
        return baseMapper.statPrice(tradeOrderWeixin);
    }
}
