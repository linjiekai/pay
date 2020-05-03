package com.mppay.gateway.dto.platform.gaohuitong;


import com.mppay.core.config.AbstractObject;
import lombok.Builder;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GHTBusiInfoDTO extends AbstractObject {

    /**
     * 业务编码
     */
    @XmlElement
    private String busiCode;

    @XmlElement(name = "rateList")
    private GHTRateDTO rateList;
    /**
     * 费率
     */
    @XmlElement
    private Integer futureRateValue;
    /**
     * 费率类型
     */
    @XmlElement
    private Integer futureRateType;


}
