package com.mppay.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志工具类（要配置logback-spring.xml文件:<logger name="xxx" addtivity="false"></logger>）
 */
public class LogUtil {

    // 支付结果通知
    public final static Logger NOTIFY = LoggerFactory.getLogger("notify");
    //定时器
    public final static Logger SCHEDULED = LoggerFactory.getLogger("scheduled");
}
