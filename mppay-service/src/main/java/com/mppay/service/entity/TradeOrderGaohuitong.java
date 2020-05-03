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
 * 高汇通交易订单流水表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-09-17
 */
@Data
public class TradeOrderGaohuitong implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发往银行支付流水号
     */
    private String outTradeNo;

    /**
     * 平台编号 名品猫:MPMALL;合伙人：PTMALL
     */
    private String platform;

    /**
     * 交易订单号
     */
    private String tradeNo;

    /**
     * 交易订单创建时间yyyy-MM-dd
     */
    private String tradeDate;

    /**
     * 交易订单创建时间HH:mm:ss
     */
    private String tradeTime;

    /**
     * 交易类型 JSAPI：公众号支付,APP：app支付,NATIVE：扫码支付,MICROPAY：刷卡支付
     */
    private String tradeType;

    /**
     * 支付订单金额
     */
    private BigDecimal price;

    /**
     * 快捷银行卡协议号
     */
    private String agrNo;

    /**
     * 短信验证码
     */
    private String smsCode;

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
     * 银行支付订单号 由微信或者支付宝返回的银行订单号
     */
    private String bankTradeNo;

    /**
     * 支付日期  yyyy-MM-dd
     */
    private String payDate;

    /**
     * 支付时间  HH:mm:ss
     */
    private String payTime;

    /**
     * 二维码链接
     */
    private String qrcUrl;

    /**
     * 对账日期yyyy-MM-dd
     */
    private String checkDate;

    /**
     * 对账状态,0待对账,1成功,2对方无我方有,3对方有我方无,4金额差错,5存疑,6对账状态未明确
     */
    private String checkStatus;

    /**
     * 订单状态 A预登记状态,成功S,失败F,等待支付W,全额退款RF,部分退款RP
     */
    private String orderStatus;

    /**
     * 银行用户标识
     */
    private String openId;

    /**
     * 银行分配商户号
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
     * 银行应用ID
     */
    private String appId;

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


}
