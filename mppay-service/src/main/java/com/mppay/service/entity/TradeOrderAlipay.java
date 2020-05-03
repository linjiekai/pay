package com.mppay.service.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 支付宝交易订单流水表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-09-17
 */
public class TradeOrderAlipay implements Serializable {

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
     * 交易订单日期yyyy-MM-dd
     */
    private String tradeDate;

    /**
     * 交易订单时间HH:mm:ss
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
     * 支付日期yyyy-MM-dd
     */
    private String payDate;

    /**
     * 支付时间HH:mm:ss
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }
    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }
    public String getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(String tradeDate) {
        this.tradeDate = tradeDate;
    }
    public String getTradeTime() {
        return tradeTime;
    }

    public void setTradeTime(String tradeTime) {
        this.tradeTime = tradeTime;
    }
    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    public String getAgrNo() {
        return agrNo;
    }

    public void setAgrNo(String agrNo) {
        this.agrNo = agrNo;
    }
    public String getSmsCode() {
        return smsCode;
    }

    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
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
    public String getBankTradeNo() {
        return bankTradeNo;
    }

    public void setBankTradeNo(String bankTradeNo) {
        this.bankTradeNo = bankTradeNo;
    }
    public String getPayDate() {
        return payDate;
    }

    public void setPayDate(String payDate) {
        this.payDate = payDate;
    }
    public String getPayTime() {
        return payTime;
    }

    public void setPayTime(String payTime) {
        this.payTime = payTime;
    }
    public String getQrcUrl() {
        return qrcUrl;
    }

    public void setQrcUrl(String qrcUrl) {
        this.qrcUrl = qrcUrl;
    }
    public String getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(String checkDate) {
        this.checkDate = checkDate;
    }
    public String getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus;
    }
    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
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
    public String getOrgNo() {
        return orgNo;
    }

    public void setOrgNo(String orgNo) {
        this.orgNo = orgNo;
    }
    public String getTerminalNo() {
        return terminalNo;
    }

    public void setTerminalNo(String terminalNo) {
        this.terminalNo = terminalNo;
    }
    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }
    public String getReturnMsg() {
        return returnMsg;
    }

    public void setReturnMsg(String returnMsg) {
        this.returnMsg = returnMsg;
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
        return "TradeOrderAlipay{" +
        "id=" + id +
        ", outTradeNo=" + outTradeNo +
        ", platform=" + platform +
        ", tradeNo=" + tradeNo +
        ", tradeDate=" + tradeDate +
        ", tradeTime=" + tradeTime +
        ", tradeType=" + tradeType +
        ", price=" + price +
        ", agrNo=" + agrNo +
        ", smsCode=" + smsCode +
        ", bankCode=" + bankCode +
        ", routeCode=" + routeCode +
        ", fundBank=" + fundBank +
        ", bankTradeNo=" + bankTradeNo +
        ", payDate=" + payDate +
        ", payTime=" + payTime +
        ", qrcUrl=" + qrcUrl +
        ", checkDate=" + checkDate +
        ", checkStatus=" + checkStatus +
        ", orderStatus=" + orderStatus +
        ", openId=" + openId +
        ", bankMercId=" + bankMercId +
        ", orgNo=" + orgNo +
        ", terminalNo=" + terminalNo +
        ", appId=" + appId +
        ", returnCode=" + returnCode +
        ", returnMsg=" + returnMsg +
        ", updateTime=" + updateTime +
        ", createTime=" + createTime +
        "}";
    }
}
