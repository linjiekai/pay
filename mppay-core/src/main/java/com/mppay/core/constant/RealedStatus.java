package com.mppay.core.constant;

/**
 * 用户实名状态
 */
public enum RealedStatus {
    /**
     * 未实名
     */
    UNREAL(0, "未实名"),
    /**
     * 软实名 (姓名+身份证)
     */
    SOFT_REAL (1, "软实名"), //
    /**
     * 弱实名(姓名+身份证+正反照片)
     */
    WEAK_REAL (2, "弱实名"),
    /**
     * 中实名(姓名+身份证+正反照片+提现银行卡)
     */
    MIDDLE_REAL(3, "中实名"),
    /**
     * 强实名(保留)
     */
    STRONG_REAL(4, "强实名"),
    ;
    private int id;

    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private RealedStatus(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
