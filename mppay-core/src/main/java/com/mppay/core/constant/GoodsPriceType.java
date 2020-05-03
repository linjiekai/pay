package com.mppay.core.constant;

public enum GoodsPriceType {

    MPMALL("MPMALL", "名品猫商品"),
    MPWJMALL("MPWJMALL", "代理费"),
    MWJMALL("MWJMALL", "秘玩家商品"),
    MNLMALL("MNLMALL", "秘女郎商品"),
    XFYLMALL("XFYLMALL", "幸福引力商品"),
    ZBMALL("ZBMALL", "赚播商品")
    ;
    private String id;
    private String name;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    GoodsPriceType(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public static GoodsPriceType paras (String id) {
        for (GoodsPriceType obj : GoodsPriceType.values()) {
            if (obj.getId().equalsIgnoreCase(id)) {
                return obj;
            }
        }
        return GoodsPriceType.MPMALL;
    }
    
    public static String parasName (String id) {
        for (GoodsPriceType obj : GoodsPriceType.values()) {
            if (obj.getId().equalsIgnoreCase(id)) {
                return obj.getName();
            }
        }
        return "商品";
    }
}
