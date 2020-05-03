package com.mppay.core.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum RouteCode {

    /*** 微信 */
    WEIXIN("WEIXIN", "微信"),
    /*** 支付宝 */
    ALIPAY("ALIPAY", "支付宝"),
    /*** 名品猫 */
    MPPAY("MPPAY", "名品猫"),
    /*** 高汇通 */
    GAOHUITONG("GAOHUITONG", "高汇通"),
    /*** 高汇通香港 */
    GAOHUITONGHK("GAOHUITONGHK", "高汇通香港"),
    /*** 信来国际支付香港 */
    SHEENPAYHK("SHEENPAYHK", "信来国际支付香港"),
    /*** 汇聚 */
    JOINPAY("JOINPAY", "汇聚"),
    ;

    private String id;
    private String name;

    /**
     * 根据id获取
     *
     * @param id
     * @return
     */
    public static RouteCode parse(String id) {
        for (RouteCode obj : RouteCode.values()) {
            if (obj.getId().equals(id)) {
                return obj;
            }
        }
        return null;
    }

}
