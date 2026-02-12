package com.liubs.jareditor.ext;

import com.liubs.jareditor.constant.PathConstant;
import com.liubs.jareditor.entity.SplitResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 类jar格式
 * @author Liubsyy
 * @date 2026/1/27
 */
public class JarLikeSupports {
    public static String JAR = "jar";
    public static String WAR = "war";
    public static String EAR = "ear";
    public static String ZIP = "zip";
    public static String AAR = "aar";

    //类jar文件格式
    public static List<String> FILE_EXT = Arrays.asList(JAR,WAR,EAR,ZIP,AAR);
    public static List<String> FILE_EXT2 = FILE_EXT.stream().map(f->"."+f).collect(Collectors.toList());
    public static List<String> FILE_EXT3 = FILE_EXT2.stream().map(f->f+"!/").collect(Collectors.toList());

    //类jar正则表达式
    public static String PATTERN_STR = String.format("\\.(%s)!/", String.join("|",FILE_EXT));
    public static Pattern PATTERN = Pattern.compile(PATTERN_STR);

    //类jar匹配
    public static String MATCHER = String.format(".*%s.*", PATTERN_STR);

    //嵌套jar文件夹匹配
    //aaa/bbb_temp/jar_nested/bbb/ccc，aaa/bbb_temp/war_nested/bbb/ccc
    public static Pattern PATTERN_TEMP_NESTED_JAR;

    static {
        List<String> nestedJarDirs = FILE_EXT.stream()
                .map(f -> PathConstant.TEMP_SUFFIX+"/"+f+PathConstant.NESTED_JAR_SUFFIX)
                .collect(Collectors.toList());
        PATTERN_TEMP_NESTED_JAR = Pattern.compile(String.join("|",nestedJarDirs));
    }

    public static SplitResult split(String str) {
        return split(PATTERN,str);
    }

    public static SplitResult split(Pattern pattern,String str) {
        Matcher matcher = pattern.matcher(str);

        List<String> parts = new ArrayList<>();
        List<String> seps = new ArrayList<>();
        List<Integer> indexOfs = new ArrayList<>();

        int lastEnd = 0;

        while (matcher.find()) {
            int start  = matcher.start();

            indexOfs.add(start);

            // 分隔符前的内容
            parts.add(str.substring(lastEnd,start));

            // 分隔符本身
            seps.add(matcher.group());

            lastEnd = matcher.end();
        }

        // 最后一段
        parts.add(str.substring(lastEnd));

        return new SplitResult(parts,seps,indexOfs);
    }

    public static void main(String[] args) {
        System.out.println(split("aaa/bbb.txt"));
        System.out.println(split("aaa/bbb.jar"));
        System.out.println(split("aaa/bbb.jar!/ccc.class"));
        System.out.println(split("aaa/bbb.jar!/ccc/ddd.jar!/e/f/g.class"));
        System.out.println(split("aaa/bbb.war!/ccc/lib/ddd.jar!/e/f/g.class"));
        System.out.println(split(PATTERN_TEMP_NESTED_JAR,"aaa/bbb_temp/jar_nested/ccc/lib/ddd.jar!/e/f/g.class"));
    }
}
