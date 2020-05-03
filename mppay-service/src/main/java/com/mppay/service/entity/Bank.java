package com.mppay.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 银行信息表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-09-04
 */
@Data
public class Bank implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 银行号
     */
    private String bankCode;

    /**
     * 银行名称
     */
    private String bankName;

    /**
     * 银行简称
     */
    private String bankAbbr;

    /**
     * 状态 0:审核 1:正常 2:冻结
     */
    private Integer status;

    /**
     * 银行类型 0:第三方平台;1:银行;9:未知
     */
    private Integer bankType;

    /**
     * 序号
     */
    private Integer indexs;

    /**
     * logo图标
     */
    private String logo;

    /**
     * 更新时间
     */
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


}
