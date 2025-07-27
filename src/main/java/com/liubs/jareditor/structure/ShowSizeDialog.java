package com.liubs.jareditor.structure;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Liubsyy
 * @date 2025/7/27
 */
public class ShowSizeDialog extends DialogWrapper {
    private String jarPath;
    private String entryName;   //可为空

    public ShowSizeDialog(String jarPath,String entryName) {
        super(true);
        this.jarPath = jarPath;
        this.entryName = entryName;
        setTitle("Size");
        init();
    }


    @Override
    protected @Nullable JComponent createCenterPanel() {

        String jarSize= String.format("%,d bytes",new File(jarPath).length());
        String entrySize="";
        String compressedSize="";
        try (JarFile jarFile = new JarFile(jarPath)) {
            if(null != entryName) {
                JarEntry entry = jarFile.getJarEntry(entryName);
                if (entry != null) {
                    if(entry.isDirectory()) {
                        long size = 0;
                        long comSize = 0;
                        Enumeration<JarEntry> entries = jarFile.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry enEmt = entries.nextElement();
                            if (enEmt.getName().startsWith(entryName) && !enEmt.isDirectory()) {
                                size += enEmt.getSize();
                                comSize += enEmt.getCompressedSize();
                            }
                        }
                        entrySize=String.format("%,d bytes",size);
                        compressedSize=String.format("%,d bytes",comSize);
                    }else {
                        long size = entry.getSize(); // 获取未压缩大小
                        long comSize = entry.getCompressedSize(); // 获取压缩后的大小
                        entrySize=String.format("%,d bytes",size);
                        compressedSize=String.format("%,d bytes",comSize);
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        JPanel panel = FormBuilder.createFormBuilder()
                .setVerticalGap(8)
                .addLabeledComponent("Jar total size: ", new JLabel(jarSize))
                .addLabeledComponent("Entry : ", new JLabel(entryName))
                .addLabeledComponent("Uncompressed Size : ", new JLabel(entrySize))
                .addLabeledComponent("Compressed Size : ", new JLabel(compressedSize))
                .getPanel();

        return panel;
    }
}
