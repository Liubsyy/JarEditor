package com.liubs.jareditor.clipboard;

import com.liubs.jareditor.util.MyFileUtil;
import com.liubs.jareditor.util.ScheduleUtil;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;

/**
 * @author Liubsyy
 * @date 2024/6/2
 */
public class FileToClipBoard {

    public static CopyResult copyFilesToClipboard(java.util.List<File> files) {

        try{
            // 获取系统剪切板
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

            // 创建一个Transferable对象，处理文件列表
            Transferable transferable = new Transferable() {
                @Override
                public DataFlavor[] getTransferDataFlavors() {
                    return new DataFlavor[]{DataFlavor.javaFileListFlavor};
                }

                @Override
                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    return DataFlavor.javaFileListFlavor.equals(flavor);
                }

                @Override
                public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                    if (!isDataFlavorSupported(flavor)) {
                        throw new UnsupportedFlavorException(flavor);
                    }
                    return files;
                }
            };

            // 将Transferable对象放到剪切板上
            clipboard.setContents(transferable, null);

            return new CopyResult(true,null);
        }catch (Exception e) {
            e.printStackTrace();
            return new CopyResult(false, "copyFilesFromClipboard err: " + e.getMessage());
        }finally {
            //60秒后删除临时文件
            ScheduleUtil.schedule(()->
                    files.forEach(c-> {
                        if(c.isDirectory()) {
                            MyFileUtil.deleteDir(c.getAbsolutePath());
                        }else {
                            c.delete();
                        }
                    }), 60);
        }

    }

}
