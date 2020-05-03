package com.mppay.service.mapper;

import com.mppay.service.entity.SeqIncr;

import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 序列号表 Mapper 接口
 * </p>
 *
 * @author chenfeihang
 * @since 2019-05-30
 */
public interface SeqIncrMapper extends BaseMapper<SeqIncr> {

	/**
	 * 下一条序列
	 * @param length
	 * @param align
	 * @return
	 */
	@Select("select nextval(#{seqName})")
	public long nextVal(String seqName);
	
	/**
	 * 当前最大的序列
	 * @param length
	 * @param align
	 * @return
	 */
	@Select("select currval(#{seqName})")
	public long currVal(String seqName);

}
