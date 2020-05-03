package com.mppay.core.sign.gaohuitong;

/**
 * 第三方支付平台：高汇通：常亮类
 */
public class GaohuitongConstants {

    //============================= 高汇通-业务代码 ===============================
    /**
     *  代付业务
     */
    public static final String GHT_BUSINESS_PAY = "B00302";

    //============================= 高汇通-交易服务码 ===============================
    /**
     * 高汇通-交易服务码-基础信息登记
     */
    public static final String GHT_BASICINFO_TRANCODE = "100001";
    /**
     * 高汇通-交易服务码-商户银行卡信息登记
     */
    public static final String GHT_BANKINFO_TRANCODE = "100002";
    /**
     * 高汇通-交易服务码-业务开通
     */
    public static final String GHT_INITIATEBUSI_TRANCODE = "100003";
    /**
     * 高汇通-交易服务码-银行卡信息查询
     */
    public static final String GHT_QRYCARDINFO = "100006";
    /**
     * 高汇通-交易服务码-鉴权绑卡
     */
    public static final String GHT_QUICK_SIGN_BIND = "IFP001";
    /**
     * 高汇通-交易服务码-支付请求
     */
    public static final String GHT_QUICK_ORDER= "IFP004";
    /**
     * 高汇通-交易服务码-签约短信【高汇通：绑卡短信请求接口】
     */
    public static final String GHT_QUICK_SMS_SIGN = "IFP010";
    /**
     * 高汇通-交易服务码-快捷签约确认【高汇通：绑卡信息确认接口】
     */
    public static final String GHT_QUICK_CONFIRM_SIGN = "IFP011";
    /**
     * 高汇通-交易服务码-支付短信【高汇通：支付短验发送】
     */
    public static final String GHT_QUICK_SMS_ORDER = "IFP012";
    /**
     * 高汇通-交易服务码-快捷支付确认【高汇通：确认支付】
     */
    public static final String GHT_QUICK_CONFIRM_ORDER = "IFP013";
    /**
     * 高汇通-交易服务码-查询银行卡信息 【高汇通：银行卡信息查询】
     */
    public static final String GHT_QUICK_QUERY_CARD_BIND = "IFP014";
    /**
     * 高汇通-交易服务码-交易流水查询 【高汇通：交易流水查询】
     */
    public static final String GHT_QUICK_QUERY_ORDER = "IFP006";
    /**
     * 高汇通-交易服务码-转账
     */
    public static final String GHT_TRANSFER = "100012";
    /**
     * 高汇通-交易服务码-代付
     */
    public static final String GHT_PAY_OTHER = "200001";
    /**
     * 高汇通-交易服务码-代付结果查询
     */
    public static final String GHT_RESULTDF_OTHER = "200002";
    /**
     * 高汇通-交易服务码-账户余额查询
     */
    public static final String GHT_QRYBALANCEINFO = "100005";
    /**
     * 高汇通-交易服务码-图片上传
     */
    public static final String GHT_ADDIMAGEINFO = "100011";
    /**
     * 高汇通-预下单
     */
    public static final String GHTHK_PAY = "PAY";
    /**
     * 高汇通-预下单
     */
    public static final String GHTHK_SEARCH = "SEARCH";
    /**
     * 高汇通-退款
     */
    public static final String GHTHK_REFUND= "REFUND";
    /**
     * 高汇通-退款查询
     */
    public static final String GHTHK_SEARCH_REFUND= "SEARCH_REFUND";
    /**
     * 高汇通-支付撤销
     */
    public static final String GHTHK_PAYC= "PAYC";
    /**
     * 高汇通-下载对账文件
     */
    public static final String GHTHK_DOWNLOAD_SETTLE_FILE= "DOWNLOAD_SETTLE_FILE";

    //============================= 高汇通-字典数据 ===============================
    /**
     * 路由编码
     */
    public static final String GHT_ROUTE = "GAOHUITONG";
    /**
     * 路由编码
     */
    public static final String GHT_ROUTE_HK = "GAOHUITONGHK";
    /**
     * 高汇通-路由字典表-category-开户行3位银行代码
     */
    public static final String ROUTE_DICTIONARY_CATEGORY_BANKCODE = "bankCode";
    /**
     * 高汇通-路由字典表-category-银行英文代码
     */
    public static final String ROUTE_DICTIONARY_CATEGORY_BANKOPEN = "bankOpen";
    /**
     * 高汇通-路由字典表-category-业务代码
     */
    public static final String ROUTE_DICTIONARY_CATEGORY_BUSINESSCODE = "businessCode";
    /**
     * 高汇通-路由字典表-category-证件类型
     */
    public static final String ROUTE_DICTIONARY_CATEGORY_CERTCODE = "certCode";

    //============================= 高汇通-响应码 ===============================
    /**
     * 高汇通-响应码-成功：000000
     */
    public static final String RETURN_CODE_SUCCESS = "000000";
    public static final String RETURN_MSG_SUCCESS = "成功";
    public static final String RETURN_CODE_100007  = "100007";
    public static final String RETURN_CODE_100008  = "100008";
    public static final String RETURN_CODE_100009  = "100009";
    public static final String RESPTYPE_S= "S";
    public static final String RESPTYPE_E= "E";
    public static final String RESPTYPE_R = "R";
    public static final String ERROR_10000X = "10000x";

    //============================= 高汇通-报文属性 ===============================
    /**
     * 高汇通-报文属性-报文类型:商户相关报文：01
     */
    public static final String MESSAGE_PROPERTY_MSGTYPE_MERCHANT = "01";
    /**
     * 高汇通-报文属性-报文类型:支付平台相关报文：02
     */
    public static final String MESSAGE_PROPERTY_MSGTYPE_PAY = "02";
    /**
     * 高汇通-报文属性-账户属性:0-私人
     */
    public static final Integer MESSAGE_PROPERTY_BANKACCPROP_PERSONAL = 0;
    /**
     * 高汇通-报文属性-账户属性:1-公司
     */
    public static final Integer MESSAGE_PROPERTY_BANKACCPROP_COMPANY = 1;

    //============================= redis key 前缀 ===============================
    /**
     * 高汇通-快捷[支付]确认-短信重发redis-key前缀
     */
    public static final String REDIS_KEY_PREFIX_QUICK_PAY_SMS_RETRY = "quick_pay_sms_retry_";
    /**
     * 高汇通-快捷[签约]确认-短信重发redis-key前缀
     */
    public static final String REDIS_KEY_PREFIX_QUICK_SIGN_SMS_RETRY = "quick_pay_sign_retry_";
}
