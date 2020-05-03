package com.mppay.service.mapper;

import org.apache.ibatis.annotations.CacheNamespace;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mppay.core.utils.RedisCache;
import com.mppay.service.entity.BankSecret;

/**
 * <p>
 * 银行密钥表 Mapper 接口
 * </p>
 *
 * @author chenfeihang
 * @since 2019-04-16
 */
@CacheNamespace(implementation=RedisCache.class, eviction=RedisCache.class, flushInterval=300000)
public interface BankSecretMapper extends BaseMapper<BankSecret> {

}
