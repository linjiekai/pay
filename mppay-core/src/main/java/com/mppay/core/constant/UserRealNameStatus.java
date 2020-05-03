package com.mppay.core.constant;

import lombok.Getter;

/**
 * @description: 用户实名状态
 */
@Getter
public enum UserRealNameStatus {
    /**
     * 身份证状态:0:注销
     */
    CANCEL(0, "注销"),
    /**
     * 身份证状态:1:实名
     */
    REAL(1, "实名"),
    /**
     * 身份证状态:2:冻结
     */
    FREEZE(2, "冻结");

    private int id;

    private String name;

    private UserRealNameStatus(int id, String name) {
        this.id = id;
        this.name = name;
    }
}