package com.mppay.service.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mppay.core.constant.Align;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.exception.BusiException;
import com.mppay.core.service.impl.RedissonDistributedLocker;
import com.mppay.service.entity.SeqIncr;
import com.mppay.service.mapper.SeqIncrMapper;
import com.mppay.service.service.ISeqIncrService;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 序列号表 服务实现类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-05-30
 */
@Service
@Slf4j
public class SeqIncrServiceImpl extends ServiceImpl<SeqIncrMapper, SeqIncr> implements ISeqIncrService {

	@Autowired
    private RedissonDistributedLocker redissonLocker;
	
	private static Map<String, SeqIncr> seqIncrMap = new HashMap<String, SeqIncr>();
	
	private static final int DEFAULT_INCREMENT = 1;
	
    private String padding = "00000000000000000000000000000000";
    
    static Lock lock = new ReentrantLock(true);

    @Override
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String nextVal(String seqName, int length, Align align) {
    	long currentValue = 0;
    	
    	try {
    		lock.tryLock(ConstEC.LOCK_WAIT_TIME, TimeUnit.SECONDS);
    		SeqIncr seqIncr = seqIncrMap.get(seqName);
        	if (null == seqIncr) {
        		seqIncr = getSeqIncr(seqName);
        		
        		if (null == seqIncr) {
        			throw new BusiException("11200", "获取序列号失败,seqName=" + seqName);
        		}
        		
        		//如果增长值小于等于DEFAULT_INCREMENT，直接返回
            	if (seqIncr.getIncrement().intValue() <= DEFAULT_INCREMENT) {
            		return paddingVal(String.valueOf(seqIncr.getCurrentValue() + seqIncr.getIncrement()), length, align);
            	}
        	}
        	
        	long nextValue = seqIncr.getNextValue();
        	currentValue = seqIncr.getCurrentValue();
        	//当序列值超过DEFAULT_LOAD_FACTOR值，需要重新扩展
        	if (currentValue >= nextValue) {
        		resize(seqName);
        		seqIncr = seqIncrMap.get(seqName);
        		nextValue = seqIncr.getNextValue();
        		currentValue = seqIncr.getCurrentValue();
        	}
        	currentValue++;
        	seqIncr.setCurrentValue(currentValue);
        	return paddingVal(String.valueOf(currentValue), length, align);
    	} catch (Exception e) {
    		log.error("获取序列号失败,seqName=" + seqName);
			throw new BusiException("11201", "获取序列号失败", e);
    	} finally {
    		lock.unlock();
		}
    	
    }

    @Override
    public String currVal(String seqName, int length, Align align) {
        return this.paddingVal(String.valueOf(baseMapper.currVal(seqName)), length, align);
    }

    public String paddingVal(String value, int length, Align align) {
        if (length > value.length()) {
            if (align.equals(Align.LEFT)) {
                return padding.substring(0, length - value.length()) + value;
            }
            return value + padding.substring(0, length - value.length());
        } else if (length < value.length()) {
            if (align.equals(Align.LEFT)) {
                return value.substring(value.length() - length, value.length());
            }
            return value.substring(0, value.length() - length);
        }
        return value;
    }
    
    private SeqIncr getSeqIncr(String seqName) throws InterruptedException {
    	try {
    		boolean lockFlag = false;
        	
        	lockFlag = redissonLocker.tryLock(seqName, TimeUnit.SECONDS, ConstEC.LOCK_WAIT_TIME, ConstEC.LOCK_LEASE_TIME);
        	
        	if (!lockFlag) {
        		log.error("获取分布式失败lockFlag=" + lockFlag + ",seqName=" + seqName);
				throw new BusiException("11201");
			}
        	
			SeqIncr seqIncr = seqIncrMap.get(seqName);
			
			if (null == seqIncr) {
				seqIncr = this.getOne(new QueryWrapper<SeqIncr>().eq("name", seqName));
				if (null == seqIncr) {
					log.error("获取序列号失败,seqName=" + seqName);
					throw new BusiException("11201");
				}
				long nextVal = baseMapper.nextVal(seqName);
				seqIncr.setNextValue(nextVal);
				seqIncr.setCurrentValue(nextVal - seqIncr.getIncrement());
				
				//如果增长值小于等于DEFAULT_INCREMENT，直接返回
            	if (seqIncr.getIncrement().intValue() > DEFAULT_INCREMENT) {
            		seqIncrMap.put(seqName, seqIncr);
            	}
			}
			
    		return seqIncr;
		} finally {
			redissonLocker.unlock(seqName);
		}
    }
    
    private void resize(String seqName) {
    	try {
    		boolean lockFlag = false;
        	
        	lockFlag = redissonLocker.tryLock(seqName, TimeUnit.SECONDS, ConstEC.LOCK_WAIT_TIME, ConstEC.LOCK_LEASE_TIME);
        	
        	if (!lockFlag) {
        		log.error("获取分布式失败lockFlag=" + lockFlag + ",seqName=" + seqName);
				throw new BusiException("11201");
			}
        	SeqIncr seqIncr = this.getOne(new QueryWrapper<SeqIncr>().eq("name", seqName));
			if (null == seqIncr) {
				log.error("获取序列号失败,seqName=" + seqName);
				throw new BusiException("11201");
			}
			long nextVal = baseMapper.nextVal(seqName);
			seqIncr.setNextValue(nextVal);
			seqIncr.setCurrentValue(nextVal - seqIncr.getIncrement());
			
        	seqIncrMap.put(seqName, seqIncr);
			
		} finally {
			redissonLocker.unlock(seqName);
		}
    }
	
}
