package com.mppay.gateway.dto.platform.gaohuitong;

import com.mppay.core.config.AbstractObject;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * 商户开通业务DTO
 */
@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GHTInitiateBusiDTO extends AbstractObject {

    /**
     * 0：新增 1：修改 2：关闭业务 3： 重新开通
     */
    @XmlElement
    private String handleType;
    /**
     * 子商户编
     * 码
     */
    @XmlElement
    private String merchantId;
    /**
     * 结算周期
     */
    @XmlElement
    private Integer cycleValue;


    @XmlElement(name = "busiList")
    private List<GHTBusiInfoDTO> busiList;


}
