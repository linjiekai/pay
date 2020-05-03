package com.mppay.core.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 身份证类型
 * 证件类型 1:身份证 2:护照 3:军官证 4:回乡证 5:台胞证 6:港澳通行证 7:国际海员证 8:外国人永久居住证 9:其它
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum CardType {

    /**
     * 身份证
     */
    ID_CARD(1, "身份证"),
    /**
     * 护照
     */
    PASSPORT(2, "护照"),
    /**
     * 军官证
     */
    OFFICIAL_CARD(3, "军官证"),
    /**
     * 回乡证
     */
    HOME_RETURN_CERTIFICATE(4, "回乡证"),
    /**
     * 台胞证
     */
    TAIWAN_COMPATRIOT_TRAVEL(5, "台胞证"),
    /**
     * 港澳通行证
     */
    PASS_INTO_HONG_KONG_AND_MACAO(6, "港澳通行证"),
    /**
     * 国际海员证
     */
    INTERNATIONAL_SEAFARER_CERTIFICAT(7, "国际海员证"),
    /**
     * 外国人永久居住证
     */
    FOREIGNERS_PERMANENT_RESIDENCE_PERMI(8, "外国人永久居住证"),
    /**
     * 其它
     */
    OTHER(9, "其它"),
    ;
    private Integer id;
    private String name;

    /**
     * 根据id获取名称
     * @param id
     * @return
     */
    public static String getNameById(int id) {
        for (CardType type : CardType.values()) {
            if (type.id == id) {
                return type.name;
            }
        }
        return null;
    }

}
