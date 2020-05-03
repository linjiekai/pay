package com.mppay.gateway.dto.platform.gaohuitong;

import com.mppay.core.config.AbstractObject;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 查询银行卡信息 【高汇通：银行卡信息查询】DTO
 */
@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GHTQueryCardBindDTO extends AbstractObject {

    /**
     * 请求参数：银行卡号
     */
    @XmlElement
    private String bankCardNo;
    
    /**
     * 响应参数：银行卡类型
     */
    @XmlElement
    private String bankCardType;

    /**
     * 响应参数：银行名称
     */
    @XmlElement
    private String bankName;

    /**
     * 响应参数：银行编号
     */
    @XmlElement
    private String bankCode;

}
