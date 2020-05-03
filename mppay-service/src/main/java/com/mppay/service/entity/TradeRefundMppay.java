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
 * 名品猫交易退款流水表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-03
 */
@Data
public class TradeRefundMppay implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发往银行退款流水号
     */
    private String outRefundNo;

    /**
     * 退款订单号
     */
    private String refundNo;

    /**
     * 交易订单号
     */
    private String tradeNo;

    /**
     * 支付时发往银行的支付流水号
     */
    private String outTradeNo;

    /**
     * 商户订单号
     */
    private String mercOrderNo;

    /**
     * 商户退款订单号
     */
    private String mercRefundNo;

    /**
     * 退款申请日期yyyy-MM-dd
     */
    private String refundDate;

    /**
     * 退款申请时间HH:mm:ss
     */
    private String refundTime;

    /**
     * 银行退款订单号
     */
    private String bankRefundNo;

    /**
     * 银行支付订单号
     */
    private String bankTradeNo;

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
     * 退款渠道 ORIGINAL—原路退款 BALANCE—退回到余额
     */
    private String refundChannel;

    /**
     * 退款订单状态 退款成功S,退款失败F,等待退款RW,退款登记R,银行受理中BW
     */
    private String orderStatus;

    /**
     * 订单金额
     */
    private BigDecimal price;

    /**
     * 申请退款金额
     */
    private BigDecimal applyPrice;

    /**
     * 实际退款金额
     */
    private BigDecimal actualPrice;

    /**
     * 对账日期yyyy-MM-dd
     */
    private String checkDate;

    /**
     * 对账状态,0待对账,1成功,2对方无我方有,3对方有我方无,4金额差错,5存疑,6对账状态未明确
     */
    private String checkStatus;

    /**
     * 银行返回退款结果日期yyyy-MM-dd
     */
    private String bankReturnDate;

    /**
     * 银行返回退款结果时间HH:mm:ss
     */
    private String bankReturnTime;

    /**
     * 返回码 银行受理中BW,SUCCESS—退款成功FAIL—退款失败 PROCESSING—退款处理中NOTSURE—未确定CHANGE—转入代发
     */
    private String returnCode;

    /**
     * 返回信息
     */
    private String returnMsg;

    /**
     * 更新时间
     */
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 退款方式，0：退款，1：支付撤销
     */
    private Integer refundType;
}
