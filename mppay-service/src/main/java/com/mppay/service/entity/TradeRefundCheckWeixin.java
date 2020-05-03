package com.mppay.service.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 微信退款对账流水表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-03
 */
public class TradeRefundCheckWeixin implements Serializable {

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
     * 银行退款订单号  由微信或者退款宝返回的银行订单号
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }
    public String getOutRefundNo() {
        return outRefundNo;
    }

    public void setOutRefundNo(String outRefundNo) {
        this.outRefundNo = outRefundNo;
    }
    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }
    public String getBankTradeNo() {
        return bankTradeNo;
    }

    public void setBankTradeNo(String bankTradeNo) {
        this.bankTradeNo = bankTradeNo;
    }
    public String getBankRefundNo() {
        return bankRefundNo;
    }

    public void setBankRefundNo(String bankRefundNo) {
        this.bankRefundNo = bankRefundNo;
    }
    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }
    public String getRouteCode() {
        return routeCode;
    }

    public void setRouteCode(String routeCode) {
        this.routeCode = routeCode;
    }
    public String getFundBank() {
        return fundBank;
    }

    public void setFundBank(String fundBank) {
        this.fundBank = fundBank;
    }
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    public String getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(String checkDate) {
        this.checkDate = checkDate;
    }
    public String getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(String checkTime) {
        this.checkTime = checkTime;
    }
    public String getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus;
    }
    public String getAccountDate() {
        return accountDate;
    }

    public void setAccountDate(String accountDate) {
        this.accountDate = accountDate;
    }
    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }
    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }
    public String getBankMercId() {
        return bankMercId;
    }

    public void setBankMercId(String bankMercId) {
        this.bankMercId = bankMercId;
    }
    public String getBankReturnStatus() {
        return bankReturnStatus;
    }

    public void setBankReturnStatus(String bankReturnStatus) {
        this.bankReturnStatus = bankReturnStatus;
    }
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "TradeRefundCheckWeixin{" +
        "id=" + id +
        ", batchId=" + batchId +
        ", outRefundNo=" + outRefundNo +
        ", outTradeNo=" + outTradeNo +
        ", bankTradeNo=" + bankTradeNo +
        ", bankRefundNo=" + bankRefundNo +
        ", bankCode=" + bankCode +
        ", routeCode=" + routeCode +
        ", fundBank=" + fundBank +
        ", price=" + price +
        ", checkDate=" + checkDate +
        ", checkTime=" + checkTime +
        ", checkStatus=" + checkStatus +
        ", accountDate=" + accountDate +
        ", tradeType=" + tradeType +
        ", openId=" + openId +
        ", bankMercId=" + bankMercId +
        ", bankReturnStatus=" + bankReturnStatus +
        ", updateTime=" + updateTime +
        ", createTime=" + createTime +
        "}";
    }
}
