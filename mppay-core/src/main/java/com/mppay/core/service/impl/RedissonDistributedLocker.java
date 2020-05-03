package com.mppay.core.service.impl;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mppay.core.service.DistributedLocker;
import com.mppay.core.utils.RedisUtil;

@Service
public class RedissonDistributedLocker implements DistributedLocker {

	@Autowired
	private RedissonClient redissonClient;
	
	private final String KEY_PRE = RedisUtil.KEY_PRE;

    @Override
    public RLock lock(String lockKey) {
        RLock lock = redissonClient.getLock(KEY_PRE + lockKey);
        lock.lock();
        return lock;
    }

    @Override
    public RLock lock(String lockKey, int leaseTime) {
        RLock lock = redissonClient.getLock(KEY_PRE + lockKey);
        lock.lock(leaseTime, TimeUnit.SECONDS);
        return lock;
    }
    
    @Override
    public RLock lock(String lockKey, TimeUnit unit ,int timeout) {
        RLock lock = redissonClient.getLock(KEY_PRE + lockKey);
        lock.lock(timeout, unit);
        return lock;
    }
    
    @Override
    public boolean tryLock(String lockKey, TimeUnit unit, int waitTime, int leaseTime) {
        RLock lock = redissonClient.getLock(KEY_PRE + lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            return false;
        }
    }
    
    @Override
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(KEY_PRE + lockKey);
        lock.unlock();
    }
    
    @Override
    public void unlock(RLock lock) {
        lock.unlock();
    }

}
