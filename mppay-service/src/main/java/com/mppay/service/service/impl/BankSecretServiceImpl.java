package com.mppay.service.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mppay.service.entity.BankSecret;
import com.mppay.service.mapper.BankSecretMapper;
import com.mppay.service.service.IBankSecretService;

/**
 * <p>
 * 银行密钥表 服务实现类
 * </p>
 *
 * @author chenfeihang
 * @since 2019-04-16
 */
@Service
public class BankSecretServiceImpl extends ServiceImpl<BankSecretMapper, BankSecret> implements IBankSecretService {

}
