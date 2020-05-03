package com.mppay.core.constant;

/**
 * 用户协议业务开通表状态
 */
public enum UserAgrBusiStatus {
    /**
     * 0:待生效
     */
    CHECK(0, "待生效"),
    /**
     * 1:正常
     */
    NORMAL(1, "正常"),
    /**
     * 2:解约
     */
    CANCEL(2, "解约"),
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

    private UserAgrBusiStatus(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
