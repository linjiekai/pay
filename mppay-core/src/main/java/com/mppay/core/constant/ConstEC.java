
package com.mppay.core.constant;

/**
 * 常量类
 *
 */
public class ConstEC {

	public static final String METHODTYPE = "methodType";
	// 客户端IP
	public static final String CLIENTIP = "clientIp";

	// 处理结果
	public static final String SUCCESS = "SUCCESS";
	public static final String FAIL = "FAIL";
	public static final String REFUND = "REFUND";
	
	public static final String TRADE_SUCCESS = "TRADE_SUCCESS";
	public static final String TRADE_CLOSED = "TRADE_CLOSED";
	
	public static final String ENCODE_UTF8 = "UTF-8";
	public static final String ENCODE_GBK = "GBK";

	public static final String MIMETYPE = "text/plain";
	
	public static final String RETURNCODE = "returnCode";
	public static final String RETURNMSG = "returnMsg";
	public static final String DATA = "data";
	
	public static final String SUCCESS_10000 = "10000";
	public static final String SUCCESS_MSG = "交易成功";
	
	public static final String PRE_REQ = "req.";
	public static final String PRE_REGS = "regs.";
	public static final String PRE_RESP = "resp.";
	
	public static final String BANKBUSIHANDLER = "BankBusiHandler";
	public static final String CHECKCENTERHANDLER = "CheckCenterHandler";
	public static final String DEPOSITPROCHANDLER = "DepositProcHandler";
	public static final String TRADEREFUNDHANDLER = "TradeRefundHandler";
	public static final String TRADEREFUNDQUERYHANDLER = "TradeRefundQueryHandler";
	public static final String WITHDRORDERBUSIHANDLER = "WithdrOrderBusiHandler";
	public static final String PLATFORMBUSIHANDLER = "PlatformBusiHandler";
	public static final String QUICKBUSIHANDLER = "QuickBusiHandler";

	//签名串私钥
	public static final String MERC_PRIVATEKEY = "12345678";
	
	public static final String X_MPMALL_SIGNVER = "X-MPMALL-SignVer";
	public static final String X_MPMALL_SIGN = "X-MPMALL-Sign";
	public static final String X_MPMALL_TOKEN = "X-MPMall-Token";
	
	public static final String USER_REDUCE_PRICE = "reduce_price_";

	//加锁等待时间(秒)
	public static int LOCK_WAIT_TIME = 10;
	
	//加锁过期时间，超时强制解锁时间(秒)
	public static int LOCK_LEASE_TIME = 20;

	// 加密-解密算法
	public static final String AES_ALGORITHM = "AES";
	public static final String RSA_ALGORITHM = "RSA";

	// 加密-解密算法 / 工作模式 / 填充方式
	public static final String AES_CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
	public static final String RSA_CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";

	/**
	 * 提现绑卡-短信发送-60秒不可重发校验-redisKey
	 */
	public static final String REDIS_KEY_PREFIX_CARD_BIND_SMS_RETRY = "card_bind_sms_retry_";
	/**
	 * 高汇通hk支付回调标识
	 */
	public static final String REDIS_KEY_ORDER_NOTIFY_GAOHUITONGHK= "order_notify_gaohuitonghk_";
	public static final String REDIS_KEY_ORDER_NOTIFY_GAOHUITONG= "order_notify_gaohuitong_";
	//缓存存3*24h
	public static int CACHE_EXP_TIME = 60 * 60 * 24 * 3;
}
