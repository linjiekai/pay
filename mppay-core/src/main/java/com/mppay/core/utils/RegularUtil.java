package com.mppay.core.utils;

/**
 * @author: Jiekai Lin
 * @Description(描述): 正则工具类
 * @date: 2019/9/20 10:49
 */
public class RegularUtil {

    //身份证
    public static final String REGULAR_ID_CARD = "(^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$)|(^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}$)";
    //手机号
    public static final String REGULAR_MOBILE = "^[0-9]\\d*$";
    //银行卡号
    public static final String REGULAR_BANKCARDNO = "^([1-9]{1})(\\d{11,18})$";
    public static final String REGULAR_VALIDDATE = "[0-9]{4}";
    public static final String REGULAR_CVN2 = "[0-9]{3}";

    //匹配规则
    public static boolean matches(Object data,String regex) {
        boolean b = false;
        try{
            b = data.toString().trim().matches(regex);
        }catch(Exception e){
        }
        return b;
    }
}
