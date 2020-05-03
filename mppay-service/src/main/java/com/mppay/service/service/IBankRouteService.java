package com.mppay.service.service;

import com.mppay.service.entity.BankRoute;
import com.mppay.service.vo.BankRouteVO;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 银行路由关联表 服务类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-03
 */
public interface IBankRouteService extends IService<BankRoute> {

	List<BankRouteVO> findBankRoute(Map<String, Object> params);

}
