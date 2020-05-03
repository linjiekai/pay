package com.mppay.gateway.dto.platform.gaohuitong;

import com.mppay.core.config.AbstractObject;
import lombok.Data;

import javax.xml.bind.annotation.*;

/**
 * @author: Jiekai Lin
 * @Description(描述): 通用请求类
 * @date: 2019/9/7 16:37
 */
@Data
@XmlRootElement(name = "merchant")
@XmlSeeAlso({GHTBankInfoRegisterDTO.class,
        GHTBaseInfoRegisterDTO.class,
        GHTInitiateBusiDTO.class,
        GHTQuickBindDTO.class,
        GHTSmsSignDTO.class,
        GHTSmsOrderDTO.class,
        GHTConfirmOrderDTO.class,
        GHTConfirmSignDTO.class,
        GHTqryCardInfoDTO.class,
        GHTQueryCardBindDTO.class,
        GHTQueryOrderDTO.class,
        GHTOrderDTO.class,
        GHTTransferDTO.class,
        GHTPayDTO.class,
        GHTImageInfoDTO.class,
        GHTBalanceInfoDTO.class,
        GHTResultDFDTO.class
	})
@XmlAccessorType(XmlAccessType.FIELD)
public class GHTReq<T> extends AbstractObject {

    @XmlElement(name = "head")
    private GHTHeadDTO head;

    @XmlElement(name = "body")
    private T body;
}
