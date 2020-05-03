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
 * 对账差错表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-03
 */
@Data
public class CheckError implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 对账批次号 关联check_batch表ID
     */
    private Long batchId;

    /**
     * 商户发往银行订单号
     */
    private String outTradeNo;

    /**
     * 银行支付订单号 由微信或者支付宝返回的银行订单号
     */
    private String bankTradeNo;

    /**
     * 交易金额
     */
    private BigDecimal tradePrice;

    /**
     * 他方金额
     */
    private BigDecimal oppositePrice;

    /**
     * 我方金额
     */
    private BigDecimal ourPrice;

    /**
     * 对账状态,0待对账,1成功,2短款,3长款,4金额差错,5存疑
     */
    private String errorStatus;

    /**
     * 会计日期yyyy-MM-dd
     */
    private String accountDate;

    /**
     * 银行编码
     */
    private String bankCode;

    /**
     * 路由编号
     */
    private String routeCode;

    /**
     * 资金银行
     */
    private String fundBank;

    /**
     * 交易编号 01充值 02消费 03充值退款 04消费退款 05特殊退款 06调账
     */
    private String tradeCode;

    /**
     * 银行用户标识
     */
    private String openId;

    /**
     * 差错日期yyyy-MM-dd
     */
    private String errorDate;

    /**
     * 更新时间
     */
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


}
