package com.mppay.gateway;

import java.util.List;

import lombok.Data;

@Data
public class CartParamsDTO2{
    private Integer productId;
    private Integer number;
    private List<Integer> cartIds;
    //不传默认为0 兼容老版本
    private Integer cartType = 0;
}
