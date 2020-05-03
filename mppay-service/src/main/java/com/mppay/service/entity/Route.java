package com.mppay.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 路由信息表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-03
 */
@Data
public class Route implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 路由编号
     */
    private String routeCode;

    /**
     * 路由名称
     */
    private String routeName;

    /**
     * 状态 0:审核 1:正常 2:冻结
     */
    private Integer status;

    /**
     * 后台通知URL
     */
    private String notifyUrl;

    /**
     * 页面通知URL
     */
    private String callbackUrl;

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
