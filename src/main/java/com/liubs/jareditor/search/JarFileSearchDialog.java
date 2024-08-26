package com.liubs.jareditor.search;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTabbedPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * jar包内搜索
 * @author Liubsyy
 * @date 2024/6/25
 */
public class JarFileSearchDialog extends DialogWrapper {
    private final Project project;

    //允许为null，如果为null则只有全局搜索
    private final VirtualFile jarFile;

    private SearchInJarPanel searchInJarPanel;
    private SearchAllJarPanel searchAllJarPanel;

    public JarFileSearchDialog(@NotNull Project project,VirtualFile jarFile) {
        super(true);

        this.project = project;
        this.jarFile =  jarFile;

        init();
        setTitle("Search in jar");
        pack(); //调整窗口大小以适应其子组件
        setModal(false);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {

        Dimension dimension = new Dimension(800, 500);

        if(null != jarFile) {
            searchInJarPanel = new SearchInJarPanel(project,jarFile);
            searchInJarPanel.setPreferredSize(dimension);
        }

        searchAllJarPanel = new SearchAllJarPanel(project);
        searchAllJarPanel.setPreferredSize(dimension);

        JBTabbedPane tabbedPane = new JBTabbedPane();

        if(null != jarFile) {
            tabbedPane.addTab(jarFile.getName(), searchInJarPanel);
        }
        tabbedPane.addTab("All Jar", searchAllJarPanel);
        return tabbedPane;
    }


    @Override
    protected Action [] createActions() {
        // Return an empty array to hide OK and Cancel buttons
        return new Action[0];
    }

    @Override
    public void doCancelAction() {
        if(null != searchInJarPanel) {
            searchInJarPanel.exit();
        }

        if(null != searchAllJarPanel) {
            searchAllJarPanel.exit();
        }
        super.doCancelAction();
    }

}
