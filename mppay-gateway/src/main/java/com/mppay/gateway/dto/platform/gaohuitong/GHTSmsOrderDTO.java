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
public class GHTSmsOrderDTO extends AbstractObject {

    /**
     * 商户用户标识
     * 【用户在商户端注册时的唯一标识：如账户】
     * userOperNo
     */
    @XmlElement
    private String userId;

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
}
