package com.liubs.jareditor.backup;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.liubs.jareditor.editor.JarEditorCore;
import com.liubs.jareditor.editor.MyJarEditor;
import com.liubs.jareditor.persistent.BackupStorage;
import com.liubs.jareditor.sdk.MessageDialog;
import com.liubs.jareditor.util.*;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Liubsyy
 * @date 2025/5/22
 */
public class BackupDialog extends DialogWrapper {
    private Project project;
    private String jarPath;
    private String jarName;
    private Backup backup;
    private List<BackupData> backupDatas;


    private JCheckBox enableBackupCheckBox;
    private TextFieldWithBrowseButton backupPathText;
    private JCheckBox onlyChangeCheckBox;
    private JTextField searchField;

    public BackupDialog(Project project, String jarPath) {
        super(true); // use current window as parent
        this.project = project;
        this.jarPath = jarPath;
        this.jarName = MyPathUtil.getSingleFileName(jarPath);
        this.backup = new Backup();

        init();
        setTitle("Backup");
        pack(); //调整窗口大小以适应其子组件
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new GridLayoutManager(2, 2));
        mainPanel.setPreferredSize(new Dimension(700, 550));

        String backupPath = BackupStorage.getInstance().getBackupPath();
        backupPathText = new TextFieldWithBrowseButton();
        enableBackupCheckBox = new JCheckBox();

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
        backupDatas = backup.getBackupData(jarPath);
        DefaultListModel<String> jarHistoryListModel = new DefaultListModel<>();

        for(BackupData backupData : backupDatas) {
            jarHistoryListModel.addElement(backupData.getChangeData().getCreateTime());
        }
        JBList<String> jarHistoryList = new JBList<>(jarHistoryListModel);

        DefaultListModel<EntryItem> entryHistoryListModel = new DefaultListModel<>();
        JBList<EntryItem> entryHistoryList = new JBList<>(entryHistoryListModel);
        entryHistoryList.setCellRenderer(new ListCellRenderer<>() {
            private final JPanel panel = new JPanel(new BorderLayout());
            private final JLabel iconLabel = new JLabel();
            private final JLabel textLabel = new JLabel();

            @Override
            public Component getListCellRendererComponent(JList<? extends EntryItem> list, EntryItem value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                panel.removeAll();

                // 设置图标根据 ChangeType
                Icon icon = getIconForChangeType(value.getChangeType());
                iconLabel.setIcon(icon);

                // 设置文本
                textLabel.setText(" "+value.getEntry());
                textLabel.setOpaque(true);

                // 设置选中效果
                if (isSelected) {
                    panel.setBackground(list.getSelectionBackground());
                    textLabel.setBackground(list.getSelectionBackground());
                } else {
                    panel.setBackground(list.getBackground());
                    textLabel.setBackground(list.getBackground());
                }

                panel.add(iconLabel, BorderLayout.WEST);
                panel.add(textLabel, BorderLayout.CENTER);

                return panel;
            }

            private Icon getIconForChangeType(ChangeType type) {
                if(null == type) {
                    return null;
                }
                switch (type) {
                    case ADD:
                        return AllIcons.General.Add;
                    case MODIFY:
                        return AllIcons.General.Modified;
                    case DELETE:
                        return AllIcons.General.Remove;
                    default:
                        return null;
                }
            }
        });

        jarHistoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { // This line prevents double events
                refreshEntryList(jarHistoryList,entryHistoryListModel);
            }
        });
        // 创建jar的菜单
        JPopupMenu jarHistoryMenu = new JPopupMenu();
        JMenuItem deleteBackupJar = new JMenuItem("Delete backup jar");
        JMenuItem revertJarToThisVersion = new JMenuItem("Revert jar to this version");

        deleteBackupJar.addActionListener(e->{
            int selectedJarIndex = jarHistoryList.getSelectedIndex();
            if(selectedJarIndex<0) {
                return;
            }
            BackupData backupData = backupDatas.get(selectedJarIndex);
            int response = Messages.showYesNoDialog(
                    project,
                    "Delete backup "+backupData.getChangeData().getCreateTime()+"?", // 消息内容
                    "Confirmation", // 窗口标题
                    Messages.getQuestionIcon() // 使用一个问号图标
            );
            if (response != Messages.YES) {
                return;
            }

            String versionDir = new File(backupData.getBackupJar()).getParentFile().getAbsolutePath();
            MyFileUtil.deleteDir(versionDir);

            jarHistoryListModel.remove(selectedJarIndex);
            backupDatas.remove(selectedJarIndex);
        });
        revertJarToThisVersion.addActionListener(e->{
            int selectedJarIndex = jarHistoryList.getSelectedIndex();
            if(selectedJarIndex<0) {
                return;
            }
            BackupData backupData = backupDatas.get(selectedJarIndex);
            int response = Messages.showYesNoDialog(
                    project,
                    "Revert to backup "+backupData.getChangeData().getCreateTime()+"?", // 消息内容
                    "Confirmation", // 窗口标题
                    Messages.getQuestionIcon() // 使用一个问号图标
            );
            if (response != Messages.YES) {
                return;
            }

            try (FileInputStream tempInputStream = new FileInputStream(backupData.getBackupJar());
                 FileOutputStream fileOutputStream = new FileOutputStream(jarPath)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = tempInputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
            }catch (Exception ex){
                ex.printStackTrace();
                MessageDialog.showErrorMessageDialog("Revert fail",ExceptionUtil.getExceptionTracing(ex));
                return;
            }
            MessageDialog.showMessageDialog("Revert success","Revert sucess to "+jarPath);
            Backup backup = new Backup();
            ChangeData changeData = new ChangeData();
            changeData.setCreateTime(DateUtil.formatDate(new Date()));
            changeData.setChangeList(new ArrayList<>());
            backup.backupJar(jarPath,changeData);

            jarHistoryListModel.add(0,changeData.getCreateTime());
            List<BackupData> newBackupDatas = backup.getBackupData(jarPath);
            backupDatas.add(0,newBackupDatas.get(0));
        });
        jarHistoryMenu.add(deleteBackupJar);
        jarHistoryMenu.add(revertJarToThisVersion);
        jarHistoryList.addMouseListener(new MouseAdapter() {
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
                int index = jarHistoryList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    jarHistoryList.setSelectedIndex(index); // 设置选中项
                    jarHistoryMenu.show(jarHistoryList, e.getX(), e.getY());
                }
            }
        });

        // 创建entry的右键菜单
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem diffWithCurrentJar = new JMenuItem("Diff with current jar");
        diffWithCurrentJar.addActionListener(e->{
            int selectedJarIndex = jarHistoryList.getSelectedIndex();
            if(selectedJarIndex<0) {
                return;
            }
            EntryItem entryItem = entryHistoryList.getSelectedValue();
            if(null == entryItem) {
                return;
            }
            BackupData backupData = backupDatas.get(selectedJarIndex);
            String backupJar = backupData.getBackupJar();
            String currentJar = jarPath;

            VirtualFile backupVersionFile = VirtualFileManager.getInstance().findFileByUrl("jar://" + backupJar + "!/"+entryItem.getEntry());
            backupVersionFile.refresh(false, false);

            VirtualFile currentVersionFile = VirtualFileManager.getInstance().findFileByUrl("jar://" + currentJar + "!/"+entryItem.getEntry());
            String backupVersionText = MyJarEditor.getDecompiledText(project, backupVersionFile);

            String currentVersionText = "";
            if(null != currentVersionFile){
                currentVersionFile.refresh(false, false);
                currentVersionText = MyJarEditor.getDecompiledText(project, currentVersionFile);
            }

            SimpleDiffRequest request = new SimpleDiffRequest(
                    "Compare Entry: "+entryItem.getEntry(),
                    DiffContentFactory.getInstance().create(project, backupVersionText),
                    DiffContentFactory.getInstance().create(project, currentVersionText),
                    "Version "+backupData.getChangeData().getCreateTime(),
                    "Current Version"
            );
            DiffManager.getInstance().showDiff(project, request);
        });
        JMenuItem showChangeWithLastVersion = new JMenuItem("Diff with previous version");
        showChangeWithLastVersion.addActionListener(e->{
            int selectedJarIndex = jarHistoryList.getSelectedIndex();
            if(selectedJarIndex<0) {
                return;
            }
            EntryItem entryItem = entryHistoryList.getSelectedValue();
            if(null == entryItem) {
                return;
            }
            BackupData backupData = backupDatas.get(selectedJarIndex);
            BackupData backupPreviousData = selectedJarIndex < backupDatas.size()-1 ? backupDatas.get(selectedJarIndex+1) : null;

            String backupJar = backupData.getBackupJar();
            VirtualFile backupVersionFile = VirtualFileManager.getInstance().findFileByUrl("jar://" + backupJar + "!/"+entryItem.getEntry());
            backupVersionFile.refresh(false, false);
            String backupVersionText = MyJarEditor.getDecompiledText(project, backupVersionFile);

            String previousVersionText = "";
            if(null != backupPreviousData) {
                String previousJar = backupPreviousData.getBackupJar();
                VirtualFile previousVersionFile = VirtualFileManager.getInstance().findFileByUrl("jar://" + previousJar + "!/"+entryItem.getEntry());
                if(null != previousVersionFile) {
                    previousVersionFile.refresh(false, false);
                    previousVersionText = MyJarEditor.getDecompiledText(project, previousVersionFile);
                }
            }

            SimpleDiffRequest request = new SimpleDiffRequest(
                    "Compare Entry: "+entryItem.getEntry(),
                    DiffContentFactory.getInstance().create(project, previousVersionText),
                    DiffContentFactory.getInstance().create(project, backupVersionText),
                    null == backupPreviousData ? "": "Version "+backupPreviousData.getChangeData().getCreateTime(),
                    "Version "+backupData.getChangeData().getCreateTime()
            );
            DiffManager.getInstance().showDiff(project, request);
        });
        JMenuItem revertVersion = new JMenuItem("Revert current entry to this version");
        revertVersion.addActionListener(e -> {
            int selectedJarIndex = jarHistoryList.getSelectedIndex();
            if(selectedJarIndex<0) {
                return;
            }
            EntryItem entryItem = entryHistoryList.getSelectedValue();
            if(null == entryItem) {
                return;
            }
            BackupData backupData = backupDatas.get(selectedJarIndex);
            String destinationDirectory = MyPathUtil.getJarEditOutput(jarPath);
            Set<String> copyFiles = new HashSet<>();
            copyFiles.add(entryItem.getEntry());
            try {
                JarUtil.copyJarRelativeEntries(backupData.getBackupJar(), destinationDirectory,copyFiles);
                MessageDialog.showMessageDialog("Save success","Save success to: " + destinationDirectory);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

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
        searchField = new JTextField(18);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                refreshEntryList(jarHistoryList,entryHistoryListModel);
            }
            public void removeUpdate(DocumentEvent e) {
                refreshEntryList(jarHistoryList,entryHistoryListModel);
            }
            public void insertUpdate(DocumentEvent e) {
                refreshEntryList(jarHistoryList,entryHistoryListModel);
            }
        });
        onlyChangeCheckBox = new JCheckBox("Only changed entries",null,true);
        onlyChangeCheckBox.addActionListener(e->{
            refreshEntryList(jarHistoryList,entryHistoryListModel);
        });


        JButton buildNewJar = new JButton("Build Jar");
        buildNewJar.addActionListener(e->{
            VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl("jar://" + jarPath + "!/");
            JarEditorCore jarEditorCore = new JarEditorCore(project, virtualFile ,null);
            jarEditorCore.buildJar(callback->{
                ApplicationManager.getApplication().invokeLater(() -> {
                    if(callback.isSuccess()) {
                        MessageDialog.showMessageDialog("Build success","Build jar successfully!");
                    }else {
                        MessageDialog.showErrorMessageDialog("Build fail","Build jar err:"+callback.getErr());
                    }
                });
            });
        });
        splitRightPanelAction.add(searchField);
        splitRightPanelAction.add(onlyChangeCheckBox);
        splitRightPanelAction.add(Box.createHorizontalStrut(1));
        splitRightPanelAction.add(buildNewJar);
        splitRightPanel.add(entryHistoryListScrollPane,BorderLayout.CENTER);
        splitRightPanel.add(splitRightPanelAction,BorderLayout.NORTH);


        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JBScrollPane(jarHistoryList), splitRightPanel);
        splitPane.setDividerLocation(160);
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

    private void refreshEntryList(JBList<String> jarHistoryList, DefaultListModel<EntryItem> entryHistoryListModel){
        int selected = jarHistoryList.getSelectedIndex();
        entryHistoryListModel.clear();

        if (selected != -1) {
            String searchText = searchField.getText();
            BackupData backupData = backupDatas.get(selected);
            if(onlyChangeCheckBox.isSelected()) {
                for(ChangeItem changeItem : backupData.getChangeData().getChangeList()) {
                    EntryItem entryItem = new EntryItem();
                    entryItem.setChangeType(ChangeType.findByValue(changeItem.getChangeType()));
                    entryItem.setEntry(changeItem.getEntry());
                    if(StringUtils.isEmpty(searchText) || changeItem.getEntry().contains(searchText)) {
                        entryHistoryListModel.addElement(entryItem);
                    }
                }
            }else {
                Map<String,ChangeType> modifiedEntries = new HashMap<>();
                for(ChangeItem changeItem : backupData.getChangeData().getChangeList()) {
                    modifiedEntries.put(changeItem.getEntry(),ChangeType.findByValue(changeItem.getChangeType()));
                }
                try (JarFile jarFile = new JarFile(backupData.getBackupJar())) {
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        EntryItem entryItem = new EntryItem();
                        entryItem.setChangeType(modifiedEntries.get(entry.getName()));
                        entryItem.setEntry(entry.getName());

                        if(StringUtils.isEmpty(searchText) || entryItem.getEntry().contains(searchText)) {
                            entryHistoryListModel.addElement(entryItem);
                        }
                    }
                }catch (Throwable e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    class EntryItem {
        ChangeType changeType;
        String entry;

        public ChangeType getChangeType() {
            return changeType;
        }

        public void setChangeType(ChangeType changeType) {
            this.changeType = changeType;
        }

        public String getEntry() {
            return entry;
        }

        public void setEntry(String entry) {
            this.entry = entry;
        }

        @Override
        public String toString() {
            return entry;
        }
    }
}
