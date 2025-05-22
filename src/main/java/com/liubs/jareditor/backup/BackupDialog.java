package com.liubs.jareditor.backup;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.liubs.jareditor.persistent.BackupStorage;
import com.liubs.jareditor.util.MyPathUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Liubsyy
 * @date 2025/5/22
 */
public class BackupDialog extends DialogWrapper {
    private String jarPath;
    private String jarName;

    private JCheckBox enableBackupCheckBox = new JCheckBox();
    private TextFieldWithBrowseButton backupPathText = new TextFieldWithBrowseButton();

    public BackupDialog(String jarPath) {
        super(true); // use current window as parent
        this.jarPath = jarPath;
        this.jarName = MyPathUtil.getSingleFileName(jarPath);

        init();
        setTitle("Backup");
        pack(); //调整窗口大小以适应其子组件
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new GridLayoutManager(2, 2));
        mainPanel.setPreferredSize(new Dimension(600, 500));



        String backupPath = BackupStorage.getInstance().getBackupPath();
        backupPathText.setText(backupPath);

        JPanel settingPanel = FormBuilder.createFormBuilder()
                .setVerticalGap(8)
                .addLabeledComponent("Enable backup :", enableBackupCheckBox)
                .addLabeledComponent("Backup directory :", backupPathText)
                .getPanel();
        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        settingPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(etchedBorder, "Setting"),
                JBUI.Borders.empty(5)));

        enableBackupCheckBox.setSelected(BackupStorage.getInstance().isEnableBackup());
        backupPathText.setEnabled(enableBackupCheckBox.isSelected());
        enableBackupCheckBox.addActionListener(e->{
            backupPathText.setEnabled(enableBackupCheckBox.isSelected());
        });

        JPanel jarHistoryPanel = new JPanel(new BorderLayout());

        DefaultListModel<String> jarHistoryListModel = new DefaultListModel<>();
        jarHistoryListModel.addElement("2025-05-22 10:11:02");
        jarHistoryListModel.addElement("2025-05-22 10:12:02");
        jarHistoryListModel.addElement("2025-05-22 10:13:02");
        JBList<String> jarHistoryList = new JBList<>(jarHistoryListModel);

        DefaultListModel<String> entryHistoryListModel = new DefaultListModel<>();
        entryHistoryListModel.addElement("com/liubs/jareditor/jarbuild/a.class");
        entryHistoryListModel.addElement("com/liubs/jareditor/build/b.class");
        entryHistoryListModel.addElement("com/liubs/jareditor/jar/build/c.class");
        JBList<String> entryHistoryList = new JBList<>(entryHistoryListModel);


        // 创建右键菜单
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem diffWithCurrentJar = new JMenuItem("Diff with current jar");
        JMenuItem showChangeWithLastVersion = new JMenuItem("Diff with previous version");
        JMenuItem revertVersion = new JMenuItem("Revert current entry to this version");
        revertVersion.addActionListener(e -> {
            String selectedValue = entryHistoryList.getSelectedValue();

        });
        popupMenu.add(diffWithCurrentJar);
        popupMenu.add(showChangeWithLastVersion);
        popupMenu.add(revertVersion);

        entryHistoryList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            private void showPopup(MouseEvent e) {
                int index = entryHistoryList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    entryHistoryList.setSelectedIndex(index); // 设置选中项
                    popupMenu.show(entryHistoryList, e.getX(), e.getY());
                }
            }
        });

        JBScrollPane entryHistoryListScrollPane = new JBScrollPane(entryHistoryList);

        JPanel splitRightPanel = new JPanel(new BorderLayout());
        JPanel splitRightPanelAction = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JCheckBox onlyChangeCheckBox = new JCheckBox("Only changed entries");
        JButton buildNewJar = new JButton("Build Jar");
        splitRightPanelAction.add(onlyChangeCheckBox);
        splitRightPanelAction.add(Box.createHorizontalStrut(20));
        splitRightPanelAction.add(buildNewJar);
        splitRightPanel.add(entryHistoryListScrollPane,BorderLayout.CENTER);
        splitRightPanel.add(splitRightPanelAction,BorderLayout.NORTH);


        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JBScrollPane(jarHistoryList), splitRightPanel);
        splitPane.setDividerLocation(150);
        splitPane.setDividerSize(0);

        jarHistoryPanel.add(splitPane,BorderLayout.CENTER);

        jarHistoryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(etchedBorder, jarName+" version history"),
                JBUI.Borders.empty(5)));


        mainPanel.add(settingPanel, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mainPanel.add(jarHistoryPanel, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));



        return mainPanel;
    }

    @Override
    protected void doOKAction() {
        boolean enableBackup = enableBackupCheckBox.isSelected();
        String backupPath = backupPathText.getText().trim();

        BackupStorage backupStorage = BackupStorage.getInstance();
        backupStorage.setEnableBackup(enableBackup);
        backupStorage.setBackupPath(backupPath);
        super.doOKAction();
    }
}
