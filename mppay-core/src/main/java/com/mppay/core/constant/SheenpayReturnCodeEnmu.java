package com.mppay.core.constant;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum SheenpayReturnCodeEnmu {

    SUCCESS("","SUCCESS","交易成功"),
    ORDER_NOT_EXIST("", "ORDER_NOT_EXIST", "订单不存在"),
    ORDER_MISMATCH("", "ORDER_MISMATCH", "订单号与商户不匹配"),
    ORDER_NOT_PAID("", "ORDER_NOT_PAID", "订单未支付完成，无法退款"),
    SYSTEMERROR("", "SYSTEMERROR", "银行系统内部异常"),
    INVALID_SHORT_ID("", "INVALID_SHORT_ID", "商户编码不合法或没有对应商户"),
    SIGN_TIMEOUT("", "SIGN_TIMEOUT", "签名超时，time字段与服务器时间相差超过5分钟"),
    INVALID_SIGN("", "INVALID_SIGN", "签名错误"),
    PARAM_INVALID("", "PARAM_INVALID", "参数不符合要求，具体细节可参考return_msg字段"),
    NOT_PERMITTED("", "NOT_PERMITTED", "未开通网关支付权限"),
    INVALID_CHANNEL("", "INVALID_CHANNEL", "不合法的支付渠道名称，请检查大小写"),
    ;
    private String id;
    private String code;
    private String msg;

    /**
     * 根据code获取枚举信息
     * @param code
     * @return
     */
    public static SheenpayReturnCodeEnmu getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (SheenpayReturnCodeEnmu obj : SheenpayReturnCodeEnmu.values()) {
            if (obj.code.equalsIgnoreCase(code)) {
                return obj;
            }
        }
        return null;
    }
}
