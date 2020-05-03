package com.mppay.core.config;

import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mppay.core.utils.RedissonProperties;

@Configuration
@AutoConfigureAfter(value = {RedissonProperties.class})
public class RedissonAutoConfiguration {
 
    @Autowired
    private RedissonProperties redissonProperties;
 
 
    /**
     * 单机的
     * @return
     */
    @Bean
    @ConditionalOnProperty(value = "redisson.address")
    public RedissonClient redissonSingle(){
        Config config = new Config();
        SingleServerConfig serverConfig = config.useSingleServer().setAddress(redissonProperties.getAddress())
                .setTimeout(redissonProperties.getTimeout())
                .setDatabase(redissonProperties.getDatabase())
                .setConnectionPoolSize(redissonProperties.getConnectionPoolSize())
                .setConnectionMinimumIdleSize(redissonProperties.getConnectionMiniumIdleSize());
        if(StringUtils.isNotEmpty(redissonProperties.getPassword())){
            serverConfig.setPassword(redissonProperties.getPassword());
        }
        return Redisson.create(config);
    }
 
    /**
     * 集群的
     * @return
     */
    @Bean
    @ConditionalOnProperty(value = "redisson.masterAddresses")
    public RedissonClient redissonSentinel(){
        Config config = new Config();
        ClusterServersConfig serverConfig = config.useClusterServers().addNodeAddress(redissonProperties.getMasterAddresses())
                .setTimeout(redissonProperties.getTimeout())
                //设置集群扫描时间
                .setScanInterval(redissonProperties.getScanInterval())
                //主节点线程池数量
                .setMasterConnectionPoolSize(redissonProperties.getMasterConnectionPoolSize())
                //从节点线程池数量
                .setSlaveConnectionPoolSize(redissonProperties.getSlaveConnectionPoolSize());
 
        if(StringUtils.isNotEmpty(redissonProperties.getPassword())){
            serverConfig.setPassword(redissonProperties.getPassword());
        }
        return Redisson.create(config);
    }
    
}