package com.mppay.gateway.dto.platform.gaohuitong;

import com.mppay.core.config.AbstractObject;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 高汇通-快捷支付-快捷签约确认【绑卡信息确认接口】 DTO
 */
@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GHTConfirmSignDTO extends AbstractObject {

    /**
     * 绑卡订单号
     * 【请求绑卡短信时返回的平台绑卡订单号】
     */
    @XmlElement
    private String bindOrderNo;
    /**
     * 商户用户标识
     * 【用户在商户端注册时的唯一标识：如账户】
     * 【userOperNo】
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
     * 【商户请求绑卡短信时的商户订单号】
     */
    @XmlElement
    private String oriReqMsgId;
    /**
     * 短信验证码
     */
    @XmlElement
    private String validateCode;
    /**
     * 绑卡ID
     * 【交易成功时返回】
     */
    @XmlElement
    private String bindId;

}
