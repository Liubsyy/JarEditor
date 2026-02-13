package com.liubs.jareditor.decompile;

/**
 * 所有反编译器
 * @author Liubsyy
 * @date 2024/10/7
 */
public enum DecompiledEnum {
    FERNFLOWER(0,"Fernflower(Default)", new IdeaDecompiler()),
    CFR(1,"CFR", new CFRDecompiler()),
    Procyon(2,"Procyon", new ProcyonDecompiler()),
    JADX(3,"Jadx", new JadXDecompiler()),

    ;
    public int value;
    public String name;
    public IDecompiler decompiler;

    DecompiledEnum(int value, String name,IDecompiler decompiler) {
        this.value = value;
        this.name = name;
        this.decompiler = decompiler;
    }

    public static DecompiledEnum findByName(String name){
        for(DecompiledEnum e : values()) {
            if(e.name.equals(name)) {
                return e;
            }
        }
        return FERNFLOWER;
    }
    public static DecompiledEnum findByValue(int value){
        for(DecompiledEnum e : values()) {
            if(e.value == value) {
                return e;
            }
        }
        return FERNFLOWER;
    }
}
