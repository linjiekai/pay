package com.mppay.service.service.impl;

import com.mppay.service.entity.Bank;
import com.mppay.service.mapper.BankMapper;
import com.mppay.service.service.IBankService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 银行信息表 服务实现类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-04-20
 */
@Service
public class BankServiceImpl extends ServiceImpl<BankMapper, Bank> implements IBankService {

}
