package com.liubs.jareditor.util;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Liubsyy
 * @date 2024/5/9
 */
public class MyFileUtil {

    public static void deleteDir(String path) {
        if(null ==path) {
            return;
        }
        try {
            Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public static List<String> findAllJars(String path) {
        List<String> jars = new ArrayList<>();
        if(null ==path) {
            return jars;
        }

        // 使用try-with-resources确保流会被正确关闭
        try (Stream<Path> files = Files.walk(Paths.get(path))) {
            jars = files
                    .filter(p -> p.toString().endsWith(".jar"))  // 过滤出所有.jar文件
                    .map(p -> p.toAbsolutePath().toString())  // 转换为绝对路径的字符串
                    .collect(Collectors.toList());  // 将结果收集到列表中

        } catch (IOException e) {
            e.printStackTrace();  // 打印异常信息
        }

        return jars;
    }

}
