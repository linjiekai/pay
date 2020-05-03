package com.mppay.gateway.dto.platform.gaohuitong;


import com.mppay.core.config.AbstractObject;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GHTResultDFDTO extends AbstractObject {

    /**
     * ⼦商户号
     */
    @XmlElement
    private String User_id;
    /**
     *要查询的交
     * 易流⽔
     */
    @XmlElement
    private String Query_sn;
    /**
     * 查询备注
     */
    @XmlElement
    private String Query_remark;

    //账号
    @XmlElement
    private String Account;
    //账号名
    @XmlElement
    private String Account_name;
    //⾦额
    @XmlElement
    private String Amount;

}
