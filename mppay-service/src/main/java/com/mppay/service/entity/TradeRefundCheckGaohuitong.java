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
 * 高汇通退款对账流水表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-09-05
 */
@Data
public class TradeRefundCheckGaohuitong implements Serializable {

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
     * 商户退款单号
     */
    private String outRefundNo;

    /**
     * 商户发往银行订单号
     */
    private String outTradeNo;

    /**
     * 银行支付订单号
     */
    private String bankTradeNo;

    /**
     * 银行退款订单号
     */
    private String bankRefundNo;

    /**
     * 支付银行
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
     * 退款订单金额
     */
    private BigDecimal price;

    /**
     * 对账日期yyyy-MM-dd
     */
    private String checkDate;

    /**
     * 对账时间HH:mm:ss
     */
    private String checkTime;

    /**
     * 对账状态,0待对账,1成功,2对方无我方有,3对方有我方无,4金额差错,5存疑
     */
    private String checkStatus;

    /**
     * 会计时间
     */
    private String accountDate;

    /**
     * 交易类型 JSAPI：公众号支付,APP：app支付,NATIVE：扫码支付,MICROPAY：刷卡支付
     */
    private String tradeType;

    /**
     * 银行用户ID号
     */
    private String openId;

    /**
     * 银行系统商户号
     */
    private String bankMercId;

    /**
     * 银行退款状态
     */
    private String bankReturnStatus;

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
