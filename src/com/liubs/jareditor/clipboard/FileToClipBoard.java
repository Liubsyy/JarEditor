package com.liubs.jareditor.clipboard;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.util.ArrayList;

/**
 * @author Liubsyy
 * @date 2024/6/2
 */
public class FileToClipBoard {

    private String clipBoardDir;
    public FileToClipBoard(String clipBoardDir) {
        this.clipBoardDir = clipBoardDir;
    }

    public CopyResult copyFilesToClipboard(java.util.List<File> files) {
        try{
            // 将文件列表转换为 Transferable 对象
            FileTransferable transferable = new FileTransferable(files);
            // 获取系统剪切板并设置内容
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(transferable, null);

            return new CopyResult(true,null);
        }catch (Exception e) {
            e.printStackTrace();
            return new CopyResult(false, "copyFilesToClipboard err: " + e.getMessage());
        }
    }

    // 创建一个实现 Transferable 接口的类
    private static class FileTransferable implements Transferable {
        private java.util.List<File> fileList;

        public FileTransferable(java.util.List<File> files) {
            this.fileList = new ArrayList<>(files);
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { DataFlavor.javaFileListFlavor };
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.javaFileListFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!DataFlavor.javaFileListFlavor.equals(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return fileList;
        }
    }

}
