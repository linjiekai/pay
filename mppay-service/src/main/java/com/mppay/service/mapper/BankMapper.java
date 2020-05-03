package com.mppay.service.mapper;

import com.mppay.core.utils.RedisCache;
import com.mppay.service.entity.Bank;

import org.apache.ibatis.annotations.CacheNamespace;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 银行信息表 Mapper 接口
 * </p>
 *
 * @author chenfeihang
 * @since 2019-04-20
 */
@CacheNamespace(implementation=RedisCache.class, eviction=RedisCache.class, flushInterval=300000)
public interface BankMapper extends BaseMapper<Bank> {

}
