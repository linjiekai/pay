package com.mppay.service.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 微信交易对账流水表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-03
 */
@Data
public class TradeCheckWeixin implements Serializable {

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
     * 银行订单号
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
     * 订单金额
     */
    private BigDecimal price;

    /**
     * 交易类型 JSAPI：公众号支付,APP：app支付,NATIVE：扫码支付,MICROPAY：刷卡支付
     */
    private String tradeType;

    /**
     * 对账日期yyyy-MM-dd
     */
    private String checkDate;

    /**
     * 对账时间HH:mm:ss
     */
    private String checkTime;

    /**
     * 对账状态,0待对账,1成功,2对方无我方有,3对方有我方无,4金额差错,5存疑,6对账状态未明确
     */
    private String checkStatus;

    /**
     * 会计日期yyyy-MM-dd
     */
    private String accountDate;

    /**
     * 银行用户ID号
     */
    private String openId;

    /**
     * 银行系统商户号
     */
    private String bankMercId;

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
