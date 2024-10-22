package com.liubs.jareditor.bytestool.asm.constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Liubsyy
 * @date 2024/10/22
 *
 * @see org.objectweb.asm.Opcodes
 */
public class AccessConstant {
    // Map for class flags
    public static final Map<Integer, String> CLASS_FLAGS = new HashMap<>();

    // Map for method flags
    public static final Map<Integer, String> METHOD_FLAGS = new HashMap<>();

    // Map for field flags
    public static final Map<Integer, String> FIELD_FLAGS = new HashMap<>();

    static {
        CLASS_FLAGS.put(0x0001, "public");
        CLASS_FLAGS.put(0x0010, "final");
        CLASS_FLAGS.put(0x0020, "super");
        CLASS_FLAGS.put(0x0200, "interface");
        CLASS_FLAGS.put(0x0400, "abstract");
        CLASS_FLAGS.put(0x1000, "synthetic");
        CLASS_FLAGS.put(0x2000, "annotation");
        CLASS_FLAGS.put(0x4000, "enum");
        CLASS_FLAGS.put(0x8000, "module");

        METHOD_FLAGS.put(0x0001, "public");
        METHOD_FLAGS.put(0x0002, "private");
        METHOD_FLAGS.put(0x0004, "protected");
        METHOD_FLAGS.put(0x0008, "static");
        METHOD_FLAGS.put(0x0010, "final");
        METHOD_FLAGS.put(0x0020, "synchronized");
        METHOD_FLAGS.put(0x0040, "bridge");
        METHOD_FLAGS.put(0x0080, "varargs");
        METHOD_FLAGS.put(0x0100, "native");
        METHOD_FLAGS.put(0x0400, "abstract");
        METHOD_FLAGS.put(0x0800, "strict");
        METHOD_FLAGS.put(0x1000, "synthetic");
        METHOD_FLAGS.put(0x8000, "mandated");

        FIELD_FLAGS.put(0x0001, "public");
        FIELD_FLAGS.put(0x0002, "private");
        FIELD_FLAGS.put(0x0004, "protected");
        FIELD_FLAGS.put(0x0008, "static");
        FIELD_FLAGS.put(0x0010, "final");
        FIELD_FLAGS.put(0x0040, "volatile");
        FIELD_FLAGS.put(0x0080, "transient");
        FIELD_FLAGS.put(0x1000, "synthetic");
        FIELD_FLAGS.put(0x8000, "mandated");
    }


    public static List<String> getMethodFlagNames(int accessFlag) {
        List<String> flagNames = new ArrayList<>();

        // Iterate through all flags in the methodFlags map
        for (Map.Entry<Integer, String> entry : METHOD_FLAGS.entrySet()) {
            int flag = entry.getKey();
            // Check if the current flag is set in the accessFlag using bitwise AND
            if ((accessFlag & flag) != 0) {
                flagNames.add(entry.getValue());
            }
        }
        return flagNames;
    }

}
