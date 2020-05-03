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
public class GHTRateDTO extends AbstractObject {
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
    /**
     * 附 加 手 续费类型
     */
    @XmlElement
    private Integer attachRateType;
    /**
     * 附 加 手 续费费率
     */
    @XmlElement
    private Integer attachRateValue;

}
