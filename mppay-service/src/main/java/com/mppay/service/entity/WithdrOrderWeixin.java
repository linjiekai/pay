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
 * 微信提现订单流水表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-09-17
 */
@Data
public class WithdrOrderWeixin implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商户发往银行订单号
     */
    private String outTradeNo;

    /**
     * 提现订单号
     */
    private String withdrOrderNo;

    /**
     * 商城商户号
     */
    private String mercId;

    /**
     * 平台编号 名品猫:MPMALL，合伙人：PTMALL
     */
    private String platform;

    /**
     * 订单流水日期
     */
    private String orderDate;

    /**
     * 订单流水时间
     */
    private String orderTime;

    /**
     * 订单流水状态 待提现:W;提现成功:S;银行受理中:BW;提现失败:F;审批拒绝:R
     */
    private String orderStatus;

    /**
     * 提现金额
     */
    private BigDecimal price;

    /**
     * 业务类型 01：充值;02：消费;03：提现;04：收益
     */
    private String busiType;

    /**
     * 交易编号 01充值 02消费 03提现 04充值退款 05消费退款 06特殊充值 07特殊退款 08调账 09分账
     */
    private String tradeCode;

    /**
     * 银行提现订单号
     */
    private String bankWithdrNo;

    /**
     * 提现日期 YYYY-MM-DD
     */
    private String bankWithdrDate;

    /**
     * 提现时间 HH:mm:ss
     */
    private String bankWithdrTime;

    /**
     * 开户行银行联行号
     */
    private String bankNo;

    /**
     * 银行编号：支付宝：ALIPAY 微信：WEIXIN 名品猫：MPPAY
     */
    private String bankCode;

    /**
     * 路由编号
     */
    private String routeCode;

    /**
     * 银行卡姓名
     */
    private String bankCardName;

    /**
     * 银行卡号
     */
    private String bankCardNo;

    /**
     * 校验用户姓名 NO_CHECK：不校验真实姓名 FORCE_CHECK：强校验真实姓名
     */
    private String checkName;

    /**
     * 银行商户号
     */
    private String bankMercId;

    /**
     * 机构号
     */
    private String orgNo;

    /**
     * 终端号
     */
    private String terminalNo;

    /**
     * 用户客户端IP
     */
    private String clientIp;

    /**
     * 返回码
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
     * 手续费
     */
    private BigDecimal servicePrice;
}
