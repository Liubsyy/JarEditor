package com.liubs.jareditor.editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.ui.components.JBList;
import com.liubs.jareditor.util.MyPathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * jar包内搜索
 * @author Liubsyy
 * @date 2024/6/25
 */
public class JarFileSearchDialog extends DialogWrapper {
    private Project project;
    private VirtualFile jarFile;

    private JTextField searchField;
    private DefaultListModel<String> searchResult = new DefaultListModel<>();
    private volatile ProgressIndicator currentIndicator = null;


    public JarFileSearchDialog(@Nullable Project project,VirtualFile jarFile) {
        super(true); // use current window as parent
        this.project = project;
        this.jarFile =  jarFile;

        init();
        setTitle("Search in jar");
        pack(); //调整窗口大小以适应其子组件
        setModal(false);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(500, 400));

        searchField = new JTextField(20);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                runSearch();
            }
            public void removeUpdate(DocumentEvent e) {
                runSearch();
            }
            public void insertUpdate(DocumentEvent e) {
                runSearch();
            }
        });

        mainPanel.add(searchField, BorderLayout.NORTH);
        JBList<String> resultList = new JBList<>(searchResult);
        mainPanel.add(new JScrollPane(resultList), BorderLayout.CENTER);

        resultList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (/*e.getClickCount() == 2 && */e.getButton() == MouseEvent.BUTTON1) {
                    int index = resultList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        String selectedPath = resultList.getModel().getElementAt(index);

                        VirtualFile openFile = VirtualFileManager.getInstance().findFileByUrl(jarFile.getUrl()+selectedPath);
                        if (openFile != null) {
                            FileEditorManager.getInstance(project).openFile(openFile, true);
                        }
                    }
                }
            }
        });

        return mainPanel;
    }

    private void runSearch() {
        if (currentIndicator != null) {
            currentIndicator.cancel();
        }
        String query = searchField.getText();
        if (query.isEmpty()) {
            searchResult.clear();
        } else {
            performSearch(query);
        }
    }


    private void performSearch(String query) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Searching Jar File", true) {
            public void run(@NotNull ProgressIndicator indicator) {
                currentIndicator = indicator;
                searchResult.clear();

                VfsUtilCore.visitChildrenRecursively(jarFile, new VirtualFileVisitor<Void>() {
                    @Override
                    public boolean visitFile(@NotNull VirtualFile file) {
                        if(indicator.isCanceled()) {
                            return false;
                        }

                        if (file.isValid() && !file.isDirectory()) {
                            ApplicationManager.getApplication().runReadAction(() -> {
                                String allText = MyJarEditor.getDecompiledText(project, file);
                                if (allText.contains(query)) {
                                    ApplicationManager.getApplication().invokeLater(() ->
                                            searchResult.addElement(MyPathUtil.getEntryPathFromJar(file.getPath()))
                                    );
                                }
                            });
                        }

                        return true;
                    }
                });
            }
        });
    }


}
