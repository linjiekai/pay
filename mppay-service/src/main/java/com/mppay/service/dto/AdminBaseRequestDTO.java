package com.mppay.service.dto;

import lombok.Data;

@Data
public class AdminBaseRequestDTO {
    private Integer page = 1;
    private Integer limit = 10;

    /**
     * 操作人id
     */
    private Integer operatorId;

    /**
     * 操作人
     */
    private String operator;
}
