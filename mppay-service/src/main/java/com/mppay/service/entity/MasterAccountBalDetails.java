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
 * 主账户资金收支明细表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-03
 */
@Data
public class MasterAccountBalDetails implements Serializable {

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
     * 交易订单号
     */
    private String tradeNo;

    /**
     * 交易编号 01充值 02消费 03充值退款 04消费退款 05特殊退款 06调账 07提现
     */
    private String tradeCode;

    /**
     * 交易订单日期yyyy-MM-dd
     */
    private String tradeDate;

    /**
     * 交易订单时间HH:mm:ss
     */
    private String tradeTime;

    /**
     * 订单金额
     */
    private BigDecimal price;

    /**
     * 系统日期yyyy-MM-dd
     */
    private String sysDate;

    /**
     * 系统时间HH:mm:ss
     */
    private String sysTime;

    /**
     * 记账日期yyyy-MM-dd
     */
    private String acDate;

    /**
     * 上日账户余额
     */
    private BigDecimal lastAcBal;

    /**
     * 不可用金额
     */
    private BigDecimal uavaBal;

    /**
     * 可用不可提现发生额
     */
    private BigDecimal notTxBal;

    /**
     * 账户余额
     */
    private BigDecimal acBal;

    /**
     * 保证金
     */
    private BigDecimal sctBal;

    /**
     * 余额更新标志 1:未更新 2:已更新
     */
    private Integer updBalFlg;

    /**
     * 入账商户号
     */
    private String mercId;

    /**
     * 状态 0:待审核 1:正常 2:销户 3:冻结
     */
    private Integer status;

    /**
     * 业务类型 01：充值;02：消费;03：提现;04：收益
     */
    private String busiType;

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
