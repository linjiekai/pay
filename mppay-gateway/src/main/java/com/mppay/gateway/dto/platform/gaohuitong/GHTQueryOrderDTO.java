package com.mppay.gateway.dto.platform.gaohuitong;

import com.mppay.core.config.AbstractObject;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 高汇通-快捷支付- 支付短信【高汇通：支付短验发送】 DTO
 */
@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GHTQueryOrderDTO extends AbstractObject {

    /**
     * 原订单号:绑卡请求时的 reqMsgId
     */
    @XmlElement
    private String oriReqMsgId;

    /**
     * 子商户号
     * 【子商户号，如果为一户一码模式则必填】
     */
    @XmlElement
    private String childMerchantId;
    
    /**
     * 交易金额
     * 以分为单位
     */
    @XmlElement
    private Integer oriAmount;
    
    /**
     * 交易应答类型
     * 订单支付结果
     * S：成功
     * E：失败
     * R：不确定（处理中）
     */
    @XmlElement
    private String oriRespType;

    /**
     * 商户交易请求时间，格式为yyyyMMddHHmmss
     */
    @XmlElement
    private String orderDate;
    
    /**
     * 交易变成最终状态时间，格式为yyyyMMddHHmmss
     */
    @XmlElement
    private String payedDate;
    
    /**
     * 交易应答码
     */
    @XmlElement
    private String oriRespCode;
    
    /**
     * 交易应答描述
     */
    @XmlElement
    private String oriRespMsg;

}
