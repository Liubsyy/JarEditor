package com.liubs.jareditor.backup;

/**
 * @author Liubsyy
 * @date 2025/5/23
 */
public enum ChangeType {
    ADD(1),
    MODIFY(2),
    DELETE(3),
    ;
    public int value;

    ChangeType(int value) {
        this.value = value;
    }

    public static ChangeType findByValue(int value){
        for(ChangeType e : values()) {
            if(e.value == value) {
                return e;
            }
        }
        return null;
    }
}
