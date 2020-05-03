package com.mppay.service.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 对账批次总控表
 * </p>
 *
 * @author chenfeihang
 * @since 2019-08-03
 */
@Data
public class CheckControl implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID 对账批次号
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 对账日期yyyy-MM-dd
     */
    private String checkDate;

    /**
     * 创建日期yyyy-MM-dd
     */
    private String createDate;

    /**
     * 开始时间 yyyy-MM-dd HH:mm:ss
     */
    private String startTime;

    /**
     * 结束时间 yyyy-MM-dd HH:mm:ss
     */
    private String endTime;

    /**
     * 会计日期yyyy-MM-dd
     */
    private String accountDate;

    /**
     * 路由编号
     */
    private String routeCode;

    /**
     * 交易编号 01充值 02消费 03充值退款 04消费退款 05特殊退款 06调账
     */
    private String tradeCode;

    /**
     * 对账状态,0待获取文件,1文件已获取,2文件已入库,3对账失败,4对账结束
     */
    private String checkStatus;

    /**
     * 平账总金额
     */
    private BigDecimal tatolSuccessPrice;

    /**
     * 平账总笔数
     */
    private Integer tatolSuccessNum;

    /**
     * 长款总金额
     */
    private BigDecimal longPrice;

    /**
     * 长款总笔数
     */
    private Integer longNum;

    /**
     * 短款总金额
     */
    private BigDecimal shortPrice;

    /**
     * 短款总笔数
     */
    private Integer shortNum;

    /**
     * 金额差错总金额
     */
    private BigDecimal errorPrice;

    /**
     * 金额差错总笔数
     */
    private Integer errorNum;

    /**
     * 对账差异总金额
     */
    private BigDecimal errorTatolPrice;

    /**
     * 对账差异总笔数
     */
    private Integer errorTatolNum;

    /**
     * 存疑总金额
     */
    private BigDecimal dubiousPrice;

    /**
     * 存疑总笔数
     */
    private Integer dubiousNum;

    /**
     * 对账文件总金额
     */
    private BigDecimal filePrice;

    /**
     * 对账文件总笔数
     */
    private Integer fileNum;

    /**
     * 对账文件名称
     */
    private String fileName;

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
