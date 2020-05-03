package com.mppay.service.service;

import com.mppay.service.entity.UserRealNameDetails;

import java.util.Map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户实名认证信息明细表 服务类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-14
 */
public interface IUserRealNameDetailsService extends IService<UserRealNameDetails> {

	IPage<UserRealNameDetails> page(Page<UserRealNameDetails> ipage, Map<String, Object> params);

	/**
	 * 根据条件分页查询
	 * @param msgMap
	 * @return
	 */
	Object pageByCondition(Map<String, Object> msgMap);
}
