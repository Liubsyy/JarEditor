package com.liubs.jareditor.clipboard;

import com.liubs.jareditor.util.MyFileUtil;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;


/**
 * @author Liubsyy
 * @date 2024/6/2
 */
public class ClipboardToFile {
    private String targetPath;

    public ClipboardToFile(String targetDirectory) {
        this.targetPath = targetDirectory;
    }

    public CopyResult copyFilesFromClipboard() {

        try{
            Path targetDirectory = Paths.get(targetPath);
            if (Files.notExists(targetDirectory)) {
                try {
                    Files.createDirectories(targetDirectory);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return new CopyResult(false, "Can not create directory: " + ex.getMessage());
                }
            }

            Transferable clipboardContent = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            if (clipboardContent != null && clipboardContent.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                try {
                    java.util.List<File> files = (java.util.List<File>) clipboardContent.getTransferData(DataFlavor.javaFileListFlavor);

                    // 遍历文件列表，检查是文件还是文件夹并相应地复制
                    for (File file : files) {
                        if (file.isDirectory()) {
                            // 如果是目录，则递归复制
                            copyDirectory(file.toPath(), targetDirectory.resolve(file.getName()));
                        } else {
                            // 如果是文件，直接复制
                            Path targetPath = targetDirectory.resolve(file.getName());
                            Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                } catch (UnsupportedFlavorException | IOException e) {
                    return new CopyResult(false, "Unable to get file from clipboard: " + e.getMessage());
                }


                return new CopyResult(true,null);
            }

            return new CopyResult(false, "Clipboard does not contain file");
        }catch (Exception e) {
            e.printStackTrace();
            return new CopyResult(false, "copyFilesFromClipboard err: " + e.getMessage());
        }
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // 在目标位置创建目录结构
                Files.createDirectories(target.resolve(source.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // 复制文件
                Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }


    public void deleteTargetDir(){
        try{
            if(null != targetPath) {
                MyFileUtil.deleteDir(targetPath);
            }
        }catch (Throwable fTh) {}
    }


}
