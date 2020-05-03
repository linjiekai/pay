package com.mppay.core.sign.hupo;

/**
 * @author j.t
 * @title: YixiangtongConstaines
 * @description: TODO
 * @date 2020/4/13 17:56
 */
public class YixiangtongConstants {


    /**
     * 易享通-访问口令key: mppay:yixiangtong:key:accesstoken
     */
    public static final String YXT_ACCESS_TOKEN_KEY = "mppay:yixiangtong:accesstoken:key";

    /**
     * 易享通-票据key: mppay:yixiangtong:key:ticket
     */
    public static final String YXT_ACCESS_TICKET_KEY = "mppay:yixiangtong:accessticket:key";

    /**
     * 易享通-授权类型,默认值: client_credential
     */
    public static final String YXT_GRANT_TYPE_CLIENT_CREDENTIAL = "client_credential";
    /**
     * 易享通-ticket类型
     */
    public static final String YXT_TICKET_TYPE_SIGN = "SIGN";

    /*============================  易享通-开放平台响应码 =====================================*/
    /**
     * 易享通-开放平台响应码-请求成功
     */
    public static final String YXT_RETURNCODE_SUCCESS = "0";
    /**
     * 易享通-开放平台响应码-服务内部错误[系统异常]
     */
    public static final String YXT_RETURNCODE_FAIL = "999999";
    /**
     * 易享通-开放平台响应码-不合法的请求[缺省参数，或验签失败，或 App 无访问权限]
     */
    public static final String YXT_RETURNCODE_ERRORREQUEST = "400100";
    /**
     * 易享通-开放平台响应码-登录态失效[Access Token 过期]
     */
    public static final String YXT_RETURNCODE_TOKENEXPIRE = "400102";
    /**
     * 易享通-开放平台响应码-服务器拒绝访问此接口
     */
    public static final String YXT_RETURNCODE_ACCESSDENIED = "400103";
    /**
     * 易享通-开放平台响应码-无权限访问此请求
     */
    public static final String YXT_RETURNCODE_PERMISSIONDENIED = "400104";
    /**
     * 易享通-开放平台响应码-身份验证不通过
     */
    public static final String YXT_RETURNCODE_AUTHENTICATION_FAILED = "400105";
    /**
     * 易享通-开放平台响应码-请求超过最大限制
     */
    public static final String YXT_RETURNCODE_REQUESTMAXIMUMLIMIT  = "400501";
    /**
     * 易享通-开放平台响应码-请求上送版本参数错误
     */
    public static final String YXT_RETURNCODE_ERRORVERSION = "400502";
    /**
     * 易享通-开放平台响应码-请求访问频率过高[同一 Appid 在 10 分钟内只能请求 Access Token 一次]
     */
    public static final String YXT_RETURNCODE_RATELIMIT = "400504";
}
