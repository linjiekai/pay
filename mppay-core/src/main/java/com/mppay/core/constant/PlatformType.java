package com.mppay.core.constant;

public enum PlatformType {

    MPMALL("888000000000001", "MPMALL", "名品猫"),
    MPWJMALL("888000000000001", "MPWJMALL", "名品玩家"),
    MNLMALL("888000000000002", "MNLMALL", "蜜女郎"),
    XFYLMALL("888000000000003", "XFYLMALL", "幸福引力"),
    ZBMALL("888000000000004", "ZBMALL", "赚播"),
    ZBGMALL("888000000000004", "ZBGMALL", "幸福直播购"),
    ZBGWJMALL("888000000000004", "ZBGWJMALL", "商城直播购玩家"),
    ;
    private String id;
    private String code;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    PlatformType(String id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public static PlatformType parasByCode(String code) {
        for (PlatformType obj : PlatformType.values()) {
            if (obj.getCode().equalsIgnoreCase(code)) {
                return obj;
            }
        }
        return null;
    }

    public static PlatformType parasByName(String name) {
        for (PlatformType obj : PlatformType.values()) {
            if (obj.getName().equalsIgnoreCase(name)) {
                return obj;
            }
        }
        return null;
    }
}
