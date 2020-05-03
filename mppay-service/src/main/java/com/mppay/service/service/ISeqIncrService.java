package com.mppay.service.service;

import com.mppay.core.constant.Align;
import com.mppay.service.entity.SeqIncr;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 序列号表 服务类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-05-30
 */
public interface ISeqIncrService extends IService<SeqIncr> {

	/**
	 * 下一条序列
	 * @param length
	 * @param align
	 * @return
	 */
	public String nextVal(String seqName, int length, Align align);
	
	/**
	 * 当前最大的序列
	 * @param length
	 * @param align
	 * @return
	 */
	public String currVal(String seqName, int length, Align align);
	
}
