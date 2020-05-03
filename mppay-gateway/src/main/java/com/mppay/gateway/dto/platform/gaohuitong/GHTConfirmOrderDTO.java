package com.mppay.gateway.dto.platform.gaohuitong;

import com.mppay.core.config.AbstractObject;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;

/**
 * 高汇通-快捷支付-快捷支付确认【高汇通：确认支付】-DTO
 */
@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GHTConfirmOrderDTO extends AbstractObject {
    /**
     * 商户用户标识
     * 【用户在商户端注册时的唯一标识：如账户】
     * userOperNo
     */
    @XmlElement
    private String userId;

    /**
     * 子商户号
     * 【子商户号，如果为一户一码模式则必填】
     */
    @XmlElement
    private String childMerchantId;

    /**
     * 原订单号
     * 支付请求时上送报文头中的商户交易流水号
     */
    @XmlElement
    private String oriReqMsgId;

    /**
     * 短信验证码
     */
    @XmlElement
    private String validateCode;

    /**
     * 设备类型
     * 每类设备对应一个整数值 1:手机， 2:平板， 3:手表， 4:PC
     */
    @XmlElement
    private String deviceType;

    /**
     * 设备号
     * 移动终端设备的唯一标识
     */
    @XmlElement
    private String deviceId;

    /**
     * 客户端IP
     * 绑卡设备所在的公网 IP，可用于定位所属地区，不是 wifi 连接时的局域网 IP。
     */
    @XmlElement
    private String userIP;

    /**
     * ============================ 响应 ============================
     */
    /**
     * 绑卡ID
     */
    @XmlElement
    private String bindId;

    /**
     * 交易金额
     * 以分为单位
     */
    @XmlElement
    private Integer amount;

    /**
     * 绑卡有效期
     * 格式为yyyyMMddHHmmss
     */
    @XmlElement
    private String bindValid;

    /**
     * 银行卡号
     */
    @XmlElement
    private String bankCardNo;

    /**
     * 银行编号
     */
    @XmlElement
    private String bankCode;

}
