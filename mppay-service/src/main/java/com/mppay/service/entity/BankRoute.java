package com.mppay.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 银行路由关联表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-03
 */
@Data
public class BankRoute implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 商户号
     */
    private String mercId;

    /**
     * 银行编号
     */
    private String bankCode;

    /**
     * 路由编号
     */
    private String routeCode;

    /**
     * 交易编号 01充值 02消费 03提现 04充值退款 05消费退款 06特殊充值 07特殊退款 08调账 09分账
     */
    private String tradeCode;

    /**
     * 排序索引
     */
    private Integer indexs;
    /**
     * 银行卡类型 01:借记卡;02:贷记卡;08:第三方平台;
     */
    private String bankCardType;


}
