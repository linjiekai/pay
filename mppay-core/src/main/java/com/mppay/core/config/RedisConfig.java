package com.mppay.core.config;

import java.io.IOException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.support.NullValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class RedisConfig {
    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<Object, Object> redisTemplate(
            RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        
        GenericJackson2JsonRedisSerializerEx genericJackson2JsonRedisSerializerEx = new GenericJackson2JsonRedisSerializerEx();
        redisTemplate.setDefaultSerializer(genericJackson2JsonRedisSerializerEx);
        redisTemplate.setValueSerializer(genericJackson2JsonRedisSerializerEx);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(stringRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

//    @Bean
//    public JedisPoolConfig jedisPoolConfig() {
//        JedisPoolConfig config = new JedisPoolConfig();
//        config.setMaxIdle(10);
//        config.setMaxTotal(100);
//        config.setMaxWaitMillis(5000);
//        config.setMinIdle(0);
//        config.setTestOnBorrow(true);
//        config.setTestWhileIdle(true);
//        config.setNumTestsPerEvictionRun(2);
//        config.setTimeBetweenEvictionRunsMillis(30000);
//        config.setMinEvictableIdleTimeMillis(60000);
//        config.setSoftMinEvictableIdleTimeMillis(60000);
//        return config;
//    }
    
    @Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    public StringRedisTemplate stringRedisTemplate(
            RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    class GenericJackson2JsonRedisSerializerEx implements RedisSerializer<Object> {

        protected GenericJackson2JsonRedisSerializer serializer = null;

        public GenericJackson2JsonRedisSerializerEx() {
            ObjectMapper om = new ObjectMapper();
            om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            om.registerModule(new JavaTimeModule());
            om.registerModule(new Jdk8Module());
            om.registerModule((new SimpleModule())
                    .addSerializer(new NullValueSerializer()));
            om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
            this.serializer = new GenericJackson2JsonRedisSerializer(om);
        }

        public GenericJackson2JsonRedisSerializerEx(ObjectMapper om) {
            this.serializer = new GenericJackson2JsonRedisSerializer(om);
        }

        @Override
        public byte[] serialize(Object o) throws SerializationException {
            return serializer.serialize(o);
        }

        @Override
        public Object deserialize(byte[] bytes) throws SerializationException {
            return serializer.deserialize(bytes);
        }

        protected class NullValueSerializer extends StdSerializer<NullValue> {
            private static final long serialVersionUID = 1999052150548658807L;
            private final String classIdentifier="@class";

            NullValueSerializer() {
                super(NullValue.class);
            }

            public void serialize(NullValue value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                jgen.writeStartObject();
                jgen.writeStringField(this.classIdentifier, NullValue.class.getName());
                jgen.writeEndObject();
            }
        }
    }
}
