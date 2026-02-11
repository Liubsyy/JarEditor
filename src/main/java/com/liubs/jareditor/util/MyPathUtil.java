package com.liubs.jareditor.util;

import com.liubs.jareditor.ext.JarLikeSupports;
import com.liubs.jareditor.constant.PathConstant;
import com.liubs.jareditor.entity.SplitResult;

import java.util.List;

/**
 * @author Liubsyy
 * @date 2024/5/9
 */
public class MyPathUtil {


    /**
     * /path/a.jar!/com/liubs/A.class转换成com.liubs.A
     * @param classNameInJar
     * @return
     */
    public static String getClassNameFromJar(String classNameInJar) {
        SplitResult splitResult = JarLikeSupports.split(classNameInJar);
        List<String> split = splitResult.getParts();
        if(split.size()!=2) {
            return null;
        }
        String classFullName = split.get(1);
        int lastIndex = classFullName.lastIndexOf('.');
        if(lastIndex > 0) {
            classFullName = classFullName.substring(0,lastIndex);
        }
        return classFullName.replace("/", ".");
//        String replace = split[1].replace("/", ".");
//        return replace.endsWith(".class") ? replace.substring(0,replace.lastIndexOf(".class")) : replace;
    }

    public static String getJarPathFromJar(String classNameInJar) {
        SplitResult splitResult = JarLikeSupports.split(classNameInJar);
        List<String> split = splitResult.getParts();
        if(split.size()<2 && splitResult.getSeparators().isEmpty()) {
            return null;
        }
        return splitResult.filePath0();
    }


    public static String getEntryPathFromJar(String fullPath) {
        SplitResult splitResult = JarLikeSupports.split(fullPath);
        if(splitResult.getParts().size()<2) {
            return null;
        }
        return splitResult.getParts().get(1);
    }


    /**
     * 获取编译输出目录
     * /path/a.jar!/com/liubs/A.class转换成 /path/a_temp/jar_edit_out
     * @param classNameInJar
     * @return
     */
    public static String getJarEditOutput(String classNameInJar){
        SplitResult splitResult = JarLikeSupports.split(classNameInJar);
        List<String> parts = splitResult.getParts();
        if(parts.size()<2) {
            for(String fileExt : JarLikeSupports.FILE_EXT2) {
                if(classNameInJar.endsWith(fileExt)) {
                    return classNameInJar.substring(0,classNameInJar.length()-fileExt.length())+PathConstant.TEMP_SUFFIX+"/"+ PathConstant.JAR_EDIT_CLASS_PATH;
                }
            }
            return null;
        }

        return parts.get(0)+PathConstant.TEMP_SUFFIX+"/"+ PathConstant.JAR_EDIT_CLASS_PATH;
    }

    public static String getCLIPBOARD_TO_FILE(String classNameInJar){
        String jarEditTemp = getJarEditTemp(classNameInJar);
        return null == jarEditTemp ? null : jarEditTemp+"/"+PathConstant.CLIPBOARD_TO_FILE;
    }
    public static String getFILE_TO_CLIPBOARD(String classNameInJar){
        String jarEditTemp = getJarEditTemp(classNameInJar);
        return null == jarEditTemp ? null : jarEditTemp+"/"+PathConstant.FILE_TO_CLIPBOARD;
    }


    public static String getJarEditTemp(String classNameInJar){
        SplitResult splitResult = JarLikeSupports.split(classNameInJar);
        List<String> split = splitResult.getParts();
        if(split.size()<2) {
            for(String fileExt : JarLikeSupports.FILE_EXT2) {
                if(classNameInJar.endsWith(fileExt)) {
                    return classNameInJar.substring(0,classNameInJar.length()-fileExt.length())+PathConstant.TEMP_SUFFIX;
                }
            }
            for(String fileExt : JarLikeSupports.FILE_EXT3) {
                if(classNameInJar.endsWith(fileExt)) {
                    return classNameInJar.substring(0,classNameInJar.length()-fileExt.length())+PathConstant.TEMP_SUFFIX;
                }
            }
            return null;
        }

        return split.get(0)+PathConstant.TEMP_SUFFIX;
    }


    public static boolean isSourceJar(String classNameInJar) {
        return null != classNameInJar && classNameInJar.contains("-sources.jar!");
    }

    public static String getJarFullPath(String classNameInJar) {
        for(String endExt : JarLikeSupports.FILE_EXT3) {
            if(classNameInJar.endsWith(endExt)) {
                return classNameInJar.substring(0,classNameInJar.length()-2);
            }
        }
        SplitResult splitResult = JarLikeSupports.split(classNameInJar);
        if(splitResult.getParts().size()<2) {
            return null;
        }
        return splitResult.filePath0();
    }
    public static String getJarSingleName(String classNameInJar) {
        String jarFullPath = getJarFullPath(classNameInJar);
        return getSingleFileName(jarFullPath);
    }

    public static String getSingleFileName(String path) {
        if(null == path) {
            return null;
        }
        int lastIndex = path.lastIndexOf("/");
        if(lastIndex>=0 && lastIndex<path.length()-1) {
            return path.substring(lastIndex+1);
        }
        return path;
    }


    public static String getNestedJarPath(String filePath){
        String jarEditTemp = getJarEditTemp(filePath);
        String nestedJarDir = PathConstant.NESTED_JAR_DIR_DEFAULT;

        SplitResult splitResult = JarLikeSupports.split(filePath);
        if(!splitResult.getSeparators().isEmpty()) {
            String extType = splitResult.getSeparators().get(0)
                    .replace(".","")
                    .replace("!/","");
            if(JarLikeSupports.FILE_EXT.contains(extType)) {
                nestedJarDir = extType+PathConstant.NESTED_JAR_SUFFIX;
            }
        }
        return null == jarEditTemp ? null : jarEditTemp+"/"+nestedJarDir;
    }


    public static String getFileExtension(String name){
        if(null == name){
            return null;
        }
        int lastIndexOf = name.lastIndexOf(".");
        if(lastIndexOf>0 && lastIndexOf+1 < name.length()) {
            return name.substring(lastIndexOf+1);
        }
        return name;
    }

}
