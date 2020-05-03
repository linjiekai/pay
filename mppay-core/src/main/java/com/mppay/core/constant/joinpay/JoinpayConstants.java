package com.mppay.core.constant.joinpay;

public class JoinpayConstants {


    //微信app
    public static final String FRPCODE_WEIXIN_APP = "WEIXIN_APP";
    //微信app+支付
    public static final String FRPCODE_WEIXIN_APP3 = "WEIXIN_APP3";
    //微信H5
    public static final String FRPCODE_WEIXIN_H5 = "WEIXIN_H5";
    //微信公众号
    public static final String FRPCODE_WEIXIN_GZH = "WEIXIN_GZH";
    //微信小程序
    public static final String FRPCODE_WEIXIN_XCX = "WEIXIN_XCX";


    public static final String RESPCODE_100 = "100";//成功
    public static final String RESPCODE_101 = "101";//失败
    public static final String RESPCODE_102 = "102";//已取消

    /**
     * 支付回调标识
     */
    public static final String REDIS_KEY_ORDER_NOTIFY_JOINPAY= "order_notify_joinpay_";
}
