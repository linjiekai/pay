package com.mppay.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 主账户基本信息表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-03
 */
@Data
public class MasterAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 账号
     */
    private String acNo;

    /**
     * 内部用户号
     */
    private String userNo;

    /**
     * 账户种类  100-个人主账户, 800-企业商户账户
     */
    private String acType;

    /**
     * 盐值
     */
    private String salt;

    /**
     * 支付密码
     */
    private String payPwd;

    /**
     * 支付密码状态 0：初始化状态; 1：正常;2：锁定
     */
    private Integer payPwdSts;

    /**
     * 当日支付密码错误次数累计
     */
    private Integer errPayPwdCount;

    /**
     * 支付密码错误日期YYYY-MM-DD
     */
    private String errPayPwdDate;

    /**
     * 状态 0:待审核 1:正常 2:销户 3:冻结
     */
    private Integer status;

    /**
     * 销户日期YYYY-MM-DD
     */
    private String closeDate;

    /**
     * 注册日期YYYY-MM-DD
     */
    private String regDate;

    /**
     * 注册时间HH:mm:ss
     */
    private String regTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(value="update_time", update="now()")
    private LocalDateTime updateTime;


}
