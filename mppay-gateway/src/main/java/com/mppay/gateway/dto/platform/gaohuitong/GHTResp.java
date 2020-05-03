package com.mppay.gateway.dto.platform.gaohuitong;

import com.mppay.core.config.AbstractObject;
import lombok.Data;

import javax.xml.bind.annotation.*;

/**
 * @author: Jiekai Lin
 * @Description(描述):  通用响应类
 * @date: 2019/9/7 16:37
 */
@Data
@XmlRootElement(name = "ipay")
@XmlAccessorType(XmlAccessType.FIELD)
public class GHTResp<T> extends AbstractObject {

    @XmlElement(name = "head")
    private GHTHeadDTO head;

    @XmlElement(name = "body")
    private T body;

}
