package com.liubs.jareditor.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.PathUtil;
import com.liubs.jareditor.filetree.NestedJar;
import com.liubs.jareditor.jarbuild.JarBuildResult;
import com.liubs.jareditor.jarbuild.JarBuilder;
import com.liubs.jareditor.sdk.NoticeInfo;
import com.liubs.jareditor.util.MyPathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * jar entry的压缩方式
 * @author Liubsyy
 * @date 2024/10/14
 */
public class CompressionMethod extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        VirtualFile selectedFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        Project project = e.getProject();
        if(project == null) {
            NoticeInfo.warning("Please open a project");
            return;
        }
        if(null == selectedFile) {
            NoticeInfo.warning("No file selected");
            return;
        }
        if("jar".equals(selectedFile.getExtension()) && selectedFile.getPath().contains(NestedJar.KEY)) {
            String originalPath = PathUtil.getLocalPath(selectedFile.getPath()).replaceFirst(NestedJar.KEY,".jar!");
            selectedFile = VirtualFileManager.getInstance().findFileByUrl("jar://"+originalPath);
            if(null == selectedFile){
                return;
            }
        }

        String entryPathFromJar = MyPathUtil.getEntryPathFromJar(selectedFile.getPath());
        if(null == entryPathFromJar) {
            return;
        }
        final String jarPath = MyPathUtil.getJarPathFromJar(selectedFile.getPath());

        int method = -1;
        try(JarFile jarFile = new JarFile(jarPath)){
            ZipEntry entry = jarFile.getEntry(entryPathFromJar);
            method = entry.getMethod();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        Dialog dialog = new Dialog(entryPathFromJar,method);
        if(dialog.showAndGet()){
            int selectedMethod = dialog.getSelectedMethod();
            if(selectedMethod == method) {
                return;
            }

            ProgressManager.getInstance().run(new Task.Backgroundable(null, "Change compression method...", false) {
                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    try {
                        JarBuilder jarBuilder = new JarBuilder(jarPath);
                        JarBuildResult jarBuildResult = jarBuilder.setCompressionMethod(entryPathFromJar,selectedMethod);
                        if(!jarBuildResult.isSuccess()) {
                            NoticeInfo.error("Change Compression err: \n%s",jarBuildResult.getErr());
                            return;
                        }
                        VirtualFileManager.getInstance().refreshWithoutFileWatcher(true);
                        NoticeInfo.info("Change success, compression method=%s",selectedMethod == JarEntry.STORED ? "STORED" : "DEFLATED");
                    }catch (Throwable e) {
                        NoticeInfo.error("Add file err",e);
                    }
                }
            });
        }
    }


    public class Dialog extends DialogWrapper {

        private ComboBox<String> methodComboBox;
        private String entryName;
        private int method;
        private String[] methods = {"STORED", "DEFLATED"};  // Method options

        public Dialog(String entryName,int method) {
            super(true);
            this.entryName = entryName;
            this.method = method;
            setTitle("Entry Compression Method");
            init();
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            // Create main panel with a BoxLayout for vertical alignment
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            JPanel jarPanel = new JPanel();
            jarPanel.setLayout(new BoxLayout(jarPanel, BoxLayout.X_AXIS));  // Horizontal alignment
            JLabel jarLabel = new JLabel("Jar Entry : ");
            JLabel jarEntry = new JLabel(entryName);
            jarPanel.add(jarLabel);
            jarPanel.add(Box.createRigidArea(new Dimension(10, 0)));  // Add horizontal spacing
            jarPanel.add(jarEntry);

            jarEntry.setPreferredSize(new Dimension(200, 30));
            panel.add(jarPanel);

            panel.add(Box.createRigidArea(new Dimension(0, 10)));  // 10px vertical space

            JPanel methodPanel = new JPanel();
            methodPanel.setLayout(new BoxLayout(methodPanel, BoxLayout.X_AXIS));  // Horizontal alignment
            JLabel methodLabel = new JLabel("Compression Method : ");
            methodComboBox = new ComboBox<>(methods);
            methodPanel.add(methodLabel);
            methodPanel.add(Box.createRigidArea(new Dimension(10, 0)));  // Add horizontal spacing
            methodPanel.add(methodComboBox);
            methodComboBox.setPreferredSize(new Dimension(200, 30));
            if(method == JarEntry.STORED) {
                methodComboBox.setSelectedIndex(0);
            }else {
                methodComboBox.setSelectedIndex(1);
            }

            panel.add(methodPanel);

            return panel;
        }

        public int getSelectedMethod() {
            return methodComboBox.getSelectedIndex() == 0 ? JarEntry.STORED : JarEntry.DEFLATED;
        }
    }


}
