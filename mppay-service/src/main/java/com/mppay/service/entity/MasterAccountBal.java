package com.mppay.service.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 主账户余额表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-03
 */
@Data 
public class MasterAccountBal implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 账号
     */
    private String acNo;

    /**
     * 内部用户号
     */
    private String userNo;

    /**
     * 账户种类  100-个人主账户, 800-企业商户账户
     */
    private String acType;

    /**
     * 资金种类 1：现金 
     */
    private Integer capType;

    /**
     * 币种
     */
    private String ccy;

    /**
     * 账户余额
     */
    private BigDecimal acBal;

    /**
     * 上日账户余额
     */
    private BigDecimal lastAcBal;

    /**
     * 不可用金额
     */
    private BigDecimal uavaBal;

    /**
     * 上日不可用金额
     */
    private BigDecimal lastUavaBal;

    /**
     * 可用不可提现余额
     */
    private BigDecimal notTxAvaBal;
    
    /**
     * 已提现余额
     */
    private BigDecimal withdrBal;
    
    /**
     * 保证金
     */
    private BigDecimal sctBal;

    /**
     * 透支额度
     */
    private BigDecimal odLmt;

    /**
     * 累计透支金额
     */
    private BigDecimal totOdAmt;

    /**
     * 最大余额限额
     */
    private BigDecimal maxBalLmt;

    /**
     * 最小余额限额
     */
    private BigDecimal minBalLmt;

    /**
     * 注册日期YYYY-MM-DD
     */
    private String regDate;

    /**
     * 注册时间HH:mm:ss
     */
    private String regTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

}
