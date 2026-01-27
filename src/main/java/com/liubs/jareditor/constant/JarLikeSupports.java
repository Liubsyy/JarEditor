package com.liubs.jareditor.constant;

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

    //类jar文件格式
    public static List<String> FILE_EXT = Arrays.asList(JAR,WAR);
    public static List<String> FILE_EXT2 = FILE_EXT.stream().map(f->"."+f).collect(Collectors.toList());
    public static List<String> FILE_EXT3 = FILE_EXT2.stream().map(f->f+"!/").collect(Collectors.toList());

    //类jar正则表达式
    public static String PATTERN_STR = String.format("\\.(%s)!/", String.join("|",FILE_EXT));
    public static Pattern PATTERN = Pattern.compile(PATTERN_STR);

    //类jar匹配
    public static String MATCHER = String.format(".*%s.*", PATTERN_STR);


    public static SplitResult split(String str) {
        Matcher matcher = PATTERN.matcher(str);

        List<String> parts = new ArrayList<>();
        List<String> seps = new ArrayList<>();

        int lastEnd = 0;

        while (matcher.find()) {
            // 分隔符前的内容
            parts.add(str.substring(lastEnd, matcher.start()));

            // 分隔符本身
            seps.add(matcher.group());

            lastEnd = matcher.end();
        }

        // 最后一段
        parts.add(str.substring(lastEnd));

        return new SplitResult(parts,seps);
    }

    public static void main(String[] args) {
        System.out.println(split("aaa/bbb.txt"));
        System.out.println(split("aaa/bbb.jar"));
        System.out.println(split("aaa/bbb.jar!/ccc.class"));
        System.out.println(split("aaa/bbb.war!/ccc/lib/ddd.jar!/e/f/g.class"));
    }
}
