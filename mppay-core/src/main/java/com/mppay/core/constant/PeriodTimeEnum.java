package com.mppay.core.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum PeriodTimeEnum {


    MINUTE("00","mi", "分钟"),
    HOUR ("01", "h","小时"),
    DAY ("02", "d","天"),
    MONTH ("03","m", "月"),
    ;

    private String id;
    private String flag;
    private String name;


    public static PeriodTimeEnum parse(String id) {
        for (PeriodTimeEnum type : PeriodTimeEnum.values()) {
            if (type.id.equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }
}

