package com.mppay.service.mapper;

import org.apache.ibatis.annotations.CacheNamespace;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mppay.core.utils.RedisCache;
import com.mppay.service.entity.Merc;

/**
 * <p>
 * 商户订单表 Mapper 接口
 * </p>
 *
 * @author chenfeihang
 * @since 2019-04-16
 */
public interface MercMapper extends BaseMapper<Merc> {

}
