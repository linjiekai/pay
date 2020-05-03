package com.mppay.service.service.impl;

import com.mppay.service.entity.BankRoute;
import com.mppay.service.mapper.BankRouteMapper;
import com.mppay.service.service.IBankRouteService;
import com.mppay.service.vo.BankRouteVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

/**
 * <p>
 * 银行路由关联表 服务实现类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-03
 */
@Service
public class BankRouteServiceImpl extends ServiceImpl<BankRouteMapper, BankRoute> implements IBankRouteService {

	@Override
	public List<BankRouteVO> findBankRoute(Map<String, Object> params) {
		return baseMapper.findBankRoute(params);
	}

}
