package com.liubs.jareditor.constant;

import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

/**
 * 获取class版本
 * @author Liubsyy
 * @date 2024/5/9
 */
public class ClassVersion {

    //上古时期的版本
    public static final Map<Integer, String> ELDEN_VERSIONS = new TreeMap<>();

    //常用版本
    public static final Map<Integer, String> JAVA_VERSIONS = new TreeMap<>();

    static {
        ELDEN_VERSIONS.put(1,"1.1");
        ELDEN_VERSIONS.put(2,"1.2");
        ELDEN_VERSIONS.put(3,"1.3");
        ELDEN_VERSIONS.put(4,"1.4");

        JAVA_VERSIONS.put(45, "1.1");
        JAVA_VERSIONS.put(46, "1.2");
        JAVA_VERSIONS.put(47, "1.3");
        JAVA_VERSIONS.put(48, "1.4");
        JAVA_VERSIONS.put(49, "5"); //从java1.5开始命名为 java5
        JAVA_VERSIONS.put(50, "6");
        JAVA_VERSIONS.put(51, "7");
        JAVA_VERSIONS.put(52, "8");
        JAVA_VERSIONS.put(53, "9");
        JAVA_VERSIONS.put(54, "10");
        JAVA_VERSIONS.put(55, "11");
        JAVA_VERSIONS.put(56, "12");
        JAVA_VERSIONS.put(57, "13");
        JAVA_VERSIONS.put(58, "14");
        JAVA_VERSIONS.put(59, "15");
        JAVA_VERSIONS.put(60, "16");
        JAVA_VERSIONS.put(61, "17");
        JAVA_VERSIONS.put(62, "18");
        JAVA_VERSIONS.put(63, "19");
        JAVA_VERSIONS.put(64, "20");
        JAVA_VERSIONS.put(65, "21");
    }

    public static String detectClassVersion(VirtualFile file) {
        if(!"class".equals(file.getExtension())){
            return null;
        }
        try (InputStream inputStream = file.getInputStream()) {

            // Skip first 4 bytes (magic number)
            inputStream.skip(4);

            // Read minor version (2 bytes, big-endian)
            int minorVersion = readTwoBytes(inputStream);

            // Read major version (2 bytes, big-endian)
            int majorVersion = readTwoBytes(inputStream);

            String javaVerion = JAVA_VERSIONS.get(majorVersion);
            if(null == javaVerion) {
                //找规律应该是相差44，如果未来jdk版本不讲武德随便命名的话再改
                //52=>8
                //53=>9
                //...
                //65=>21
                return String.valueOf(majorVersion - 44);
            }
            return javaVerion;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static int readTwoBytes(InputStream inputStream) throws IOException {
        return (inputStream.read() << 8) | inputStream.read();
    }
}
