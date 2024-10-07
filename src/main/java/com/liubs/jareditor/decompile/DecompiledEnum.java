package com.liubs.jareditor.decompile;

/**
 * @author Liubsyy
 * @date 2024/10/7
 */
public enum DecompiledEnum {
    FERNFLOWER(0,"Fernflower(Default)"),
    CFR(1,"CFR"),
    Procyon(2,"Procyon"),

    ;
    public int value;
    public String name;

    DecompiledEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static DecompiledEnum findByName(String name){
        for(DecompiledEnum e : values()) {
            if(e.name.equals(name)) {
                return e;
            }
        }
        return FERNFLOWER;
    }
}
