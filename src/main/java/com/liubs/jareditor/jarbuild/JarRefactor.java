package com.liubs.jareditor.jarbuild;

import java.io.*;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import com.liubs.jareditor.util.ExceptionUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;

/**
 * @author Liubsyy
 * @date 2024/9/12
 */
public class JarRefactor extends JarBuilder {
    public JarRefactor(String jarFile) {
        super(jarFile);
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int n;
        while (0 <= (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }

    /**
     * 重构Class
     * @param oldClassName a/b/c这样的格式
     * @param newCLassName a/b/c'这样的格式
     * @return
     */
    public JarBuildResult refactorClass(String oldClassName,String newCLassName) {
        JarBuildResult jarBuildResult;

        Map<String,String> classRefactorMapping = new HashMap<>();
        classRefactorMapping.put(oldClassName,newCLassName);

        File tempJarFile = null;
        try {
            tempJarFile = Files.createTempFile("tempJar", ".jar").toFile();

            try (JarFile originalJar = new JarFile(jarFile);
                 JarOutputStream tempJarOutputStream = new JarOutputStream(new FileOutputStream(tempJarFile))) {

                Enumeration<JarEntry> entries = originalJar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();

                    if (entry.getName().endsWith(".class")) {
                        try (InputStream entryInputStream = originalJar.getInputStream(entry)) {
                            byte[] bytes = toByteArray(entryInputStream);
                            if(bytes.length<=6) {
                                continue;
                            }
                            ClassReader cr = new ClassReader(new ByteArrayInputStream(bytes));

                            ClassWriter cw = new ClassWriter(0);

                            SimpleRemapper remapper = new SimpleRemapper(classRefactorMapping);
                            final ClassRemapper cv = new ClassRemapper(cw, remapper);

                            try {
                                cr.accept(cv, ClassReader.EXPAND_FRAMES);
                            } catch (Throwable ise) {
                                ise.printStackTrace();
                            }

                            final byte[] renamedClass = cw.toByteArray();
                            String newClassName = entry.getName().equals(oldClassName+".class") ? newCLassName+".class" : entry.getName();

                            JarEntry newEntry = new JarEntry(newClassName);
                            newEntry.setTime(entry.getTime());
                            newEntry.setMethod(entry.getMethod());

                            tempJarOutputStream.putNextEntry(newEntry);
                            tempJarOutputStream.write(renamedClass);
                        }

                    } else {
                        JarEntry newEntry = copyNewEntry(originalJar, entry, entry.getName());
                        tempJarOutputStream.putNextEntry(newEntry);

                        try (InputStream entryInputStream = originalJar.getInputStream(entry)) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = entryInputStream.read(buffer)) != -1) {
                                tempJarOutputStream.write(buffer, 0, bytesRead);
                            }
                        }
                    }

                    tempJarOutputStream.closeEntry();
                }

            }

            writeTargetJar(tempJarFile);
            jarBuildResult = new JarBuildResult(true, null);

        } catch (Exception e) {
            jarBuildResult = new JarBuildResult(false, "Refactor class failed: " + ExceptionUtil.getExceptionTracing(e));
        }finally {
            if (null != tempJarFile && tempJarFile.exists()) {
                tempJarFile.delete(); // 删除临时文件
            }
        }

        return jarBuildResult;
    }


    public JarBuildResult refactorPackage(String oldPackage,String newPackage) {
        JarBuildResult jarBuildResult;

        File tempJarFile = null;
        try {
            tempJarFile = Files.createTempFile("tempJar", ".jar").toFile();

            try (JarFile originalJar = new JarFile(jarFile);
                 JarOutputStream tempJarOutputStream = new JarOutputStream(new FileOutputStream(tempJarFile))) {

                Enumeration<JarEntry> entries = originalJar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();

                    if (entry.getName().endsWith(".class")) {
                        try (InputStream entryInputStream = originalJar.getInputStream(entry)) {
                            byte[] bytes = toByteArray(entryInputStream);
                            if(bytes.length<=6) {
                                continue;
                            }
                            ClassReader cr = new ClassReader(new ByteArrayInputStream(bytes));

                            ClassWriter cw = new ClassWriter(0);

                            PackageRemapper packageRemapper = new PackageRemapper(oldPackage,newPackage);
                            final ClassRemapper cv = new ClassRemapper(cw, packageRemapper);

                            try {
                                cr.accept(cv, ClassReader.EXPAND_FRAMES);
                            } catch (Throwable ise) {
                                ise.printStackTrace();
                            }

                            final byte[] renamedClass = cw.toByteArray();
                            String newClassName = packageRemapper.map(entry.getName().substring(0,entry.getName().length()-6));

                            JarEntry newEntry = new JarEntry(newClassName+".class");
                            newEntry.setTime(entry.getTime());
                            newEntry.setMethod(entry.getMethod());
                            tempJarOutputStream.putNextEntry(newEntry);

                            tempJarOutputStream.write(renamedClass);
                        }

                    } else {
                        JarEntry newEntry = copyNewEntry(originalJar, entry, entry.getName());
                        tempJarOutputStream.putNextEntry(newEntry);

                        try (InputStream entryInputStream = originalJar.getInputStream(entry)) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = entryInputStream.read(buffer)) != -1) {
                                tempJarOutputStream.write(buffer, 0, bytesRead);
                            }
                        }
                    }

                    tempJarOutputStream.closeEntry();
                }
            }

            writeTargetJar(tempJarFile);
            jarBuildResult = new JarBuildResult(true, null);

        } catch (Throwable e) {
            jarBuildResult = new JarBuildResult(false, "Refactor class failed: " + ExceptionUtil.getExceptionTracing(e));
        }finally {
            if (null != tempJarFile && tempJarFile.exists()) {
                tempJarFile.delete(); // 删除临时文件
            }
        }

        return jarBuildResult;
    }


}
