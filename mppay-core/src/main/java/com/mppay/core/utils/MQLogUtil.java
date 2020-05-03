package com.mppay.core.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * 专门作用于记录mq信息
 * 格式：(((mppay((({"exchange":"","routingKey":"", "queue":"","msg":""}
 */
@Slf4j
public class MQLogUtil {

    private static final String PRE = "(((mppay(((";

    public static void error(String msg) {
        log.error(PRE + msg);
    }

    public static void error(String msg, Object... obj) {
        log.error(PRE + msg, obj);
    }

    public static void warn(String msg) {
        log.error(PRE + msg);
    }

    public static void warn(String msg, Object... obj) {
        log.error(PRE + msg, obj);
    }

    public static void info(String msg) {
        log.info(PRE + msg);
    }

    public static void info(String msg, Object... obj) {
        log.info(PRE + msg, obj);
    }

    public static void debug(String msg) {
        log.debug(PRE + msg);
    }

    public static void debug(String msg, Object... obj) {
        log.debug(PRE + msg, obj);
    }
    // 要什么补什么,记得加上前缀PRE
}
