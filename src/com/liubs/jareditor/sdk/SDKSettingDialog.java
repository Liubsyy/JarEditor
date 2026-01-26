package com.liubs.jareditor.sdk;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.liubs.jareditor.decompile.DecompiledEnum;
import com.liubs.jareditor.persistent.SDKSettingStorage;
import com.liubs.jareditor.util.CommandTools;
import com.liubs.jareditor.util.StringUtils;


import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Liubsyy
 * @date 2024/6/2
 */
public class SDKSettingDialog extends DialogWrapper {

    private java.util.List<SDKSettingStorage.MyItem> allItems = new ArrayList<>();
    private java.util.List<JComponent> enables = new ArrayList<>();
    private ComboBox<String> decompiledToolComboBox;
    private JCheckBox parameters;
    private JCheckBox procNone;


    private Map<String,JCheckBox> genDebugInfosMap = new HashMap<>();

    private String lastDecompiledTool;

    public SDKSettingDialog() {
        super(true); // use current window as parent
        init();
        setTitle("SDK setting");


        lastDecompiledTool = DecompiledEnum.findByValue(SDKSettingStorage.getInstance().getDecompiledTool()).name;

        pack(); //调整窗口大小以适应其子组件
    }

    private void enableField(boolean enable){
        enables.forEach(c->{
            c.setEnabled(enable);
        });
    }

    @Override
    protected JComponent createCenterPanel() {

        //basic config panel
        JPanel mainPanel = new JPanel(new GridLayoutManager(2, 2));
        mainPanel.setPreferredSize(new Dimension(500, 420));

        SDKSettingStorage sdkSetting = SDKSettingStorage.getInstance();

        //-g checkbox
        JPanel genDebugInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JCheckBox lines = new JCheckBox("lines");
        JCheckBox vars = new JCheckBox("vars");
        JCheckBox source = new JCheckBox("source");
        genDebugInfosMap.put("lines",lines);
        genDebugInfosMap.put("vars",vars);
        genDebugInfosMap.put("source",source);
        genDebugInfoPanel.add(lines);
        genDebugInfoPanel.add(vars);
        genDebugInfoPanel.add(source);
        if(StringUtils.isEmpty(sdkSetting.getGenDebugInfos()) || sdkSetting.getGenDebugInfos().contains("lines")) {
            lines.setSelected(true);
        }
        if(StringUtils.isEmpty(sdkSetting.getGenDebugInfos()) || sdkSetting.getGenDebugInfos().contains("vars")) {
            vars.setSelected(true);
        }
        if(StringUtils.isEmpty(sdkSetting.getGenDebugInfos()) || sdkSetting.getGenDebugInfos().contains("source")) {
            source.setSelected(true);
        }


        //decompiled comboBox
        decompiledToolComboBox = new ComboBox<>();
        for(DecompiledEnum decompiledEnum : DecompiledEnum.values()) {
            decompiledToolComboBox.addItem(decompiledEnum.name);
            if(decompiledEnum.value == sdkSetting.getDecompiledTool()) {
                decompiledToolComboBox.setSelectedItem(decompiledEnum.name);
            }
        }

        //-parameters
        parameters = new JCheckBox("-parameters");
        parameters.setSelected(sdkSetting.isParameters());

        //-proc:none
        procNone = new JCheckBox("-proc:none");
        procNone.setSelected(sdkSetting.isProcNone());

        JPanel args = new JPanel(new FlowLayout(FlowLayout.LEFT));
        args.add(parameters);
        args.add(procNone);


        JPanel preferencePanel = FormBuilder.createFormBuilder()
                .setVerticalGap(8)
                .addLabeledComponent("Generate debug info(-g) :", genDebugInfoPanel)
                .addLabeledComponent("Args :", args)
                .addLabeledComponent("Decompiled with :", decompiledToolComboBox)
                .getPanel();
        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        preferencePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(etchedBorder, "Preferences"),
                JBUI.Borders.empty(5)));


        mainPanel.add(preferencePanel, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null));



        Dimension buttonSize = new Dimension(20, 20);

//        JButton copyNewButton = new JButton(AllIcons.Actions.Copy);
//        copyNewButton.setOpaque(false);
//        copyNewButton.setContentAreaFilled(false);
//        copyNewButton.setBorderPainted(false);
//        copyNewButton.setMargin(JBUI.emptyInsets());
//        copyNewButton.setPreferredSize(buttonSize);
//        copyNewButton.setMaximumSize(buttonSize);
//        copyNewButton.setToolTipText("Copy new");
//        JLabel sdkListLabel = new JLabel("SDK List");

        JButton addButton = new JButton(AllIcons.General.Add);
        addButton.setOpaque(false);
        addButton.setContentAreaFilled(false);
        addButton.setBorderPainted(false);
        addButton.setMargin(JBUI.emptyInsets());
        addButton.setPreferredSize(buttonSize);
        addButton.setMaximumSize(buttonSize);

        JButton removeButton = new JButton(AllIcons.General.Remove);
        removeButton.setOpaque(false);
        removeButton.setContentAreaFilled(false);
        removeButton.setBorderPainted(false);
        removeButton.setPreferredSize(buttonSize);
        removeButton.setMaximumSize(buttonSize);
        removeButton.setMargin(JBUI.emptyInsets());


        JPanel optPanel = new JPanel();
        optPanel.setLayout(new BoxLayout(optPanel, BoxLayout.X_AXIS));
//        optPanel.add(sdkListLabel);
//        optPanel.add(Box.createHorizontalStrut(20));
//        mappingLabelPanel.add(copyNewButton);
//        mappingLabelPanel.add(Box.createHorizontalStrut(10));
        optPanel.add(addButton);
//        mappingLabelPanel.add(Box.createHorizontalStrut(5));
        optPanel.add(removeButton);


        // Add the new panel to the main panel
//        mainPanel.add(optPanel, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST,
//                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
//                GridConstraints.SIZEPOLICY_FIXED, null, null, null));



        // Create dynamic list model for left panel
        DefaultListModel<String> connectionList = new DefaultListModel<>();
        JBList<String> list = new JBList<>(connectionList);
        for(SDKSettingStorage.MyItem e : SDKSettingStorage.getMySdksDefaultProjectSdks()) {
            SDKSettingStorage.MyItem connectionItemTemp = new SDKSettingStorage.MyItem();
            if(StringUtils.isEmpty(e.getName()) || StringUtils.isEmpty(e.getPath())) {
                continue;
            }
            connectionItemTemp.setName(e.getName());
            connectionItemTemp.setPath(e.getPath());
            allItems.add(connectionItemTemp);
            connectionList.addElement(e.getName());
        }
        JBScrollPane leftScroll = new JBScrollPane(list);


        // Create right panel with 3 input fields
        JPanel rightPanel = new JPanel(new GridLayout(8, 2));

        rightPanel.add(new JLabel("Name"));
        JTextField nameField = new JTextField();
        rightPanel.add(nameField);
        nameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) { }

            public void update() {
                // perform your task here
                int selected = list.getSelectedIndex();
                if (selected != -1) {
                    allItems.get(selected).setName(nameField.getText());

                    if(StringUtils.isNotEmpty(nameField.getText())) {
                        connectionList.set(selected, nameField.getText());
                    }
                }
            }
        });

        rightPanel.add(new JLabel("SDK Home"));
        TextFieldWithBrowseButton sdkHomeField = new TextFieldWithBrowseButton();
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, true, true, true, false, false);
        sdkHomeField.addBrowseFolderListener(new TextBrowseFolderListener( fileChooserDescriptor,null));

        sdkHomeField.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }

            public void update() {
                int selected = list.getSelectedIndex();
                if (selected != -1) {
                    allItems.get(selected).setPath(sdkHomeField.getText());
                }
            }
        });
        rightPanel.add(sdkHomeField);


        enables.add(nameField);
        enables.add(sdkHomeField);

        // Create a split pane with the two scroll panes in it.
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                leftScroll, rightPanel);
//        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(150);
        splitPane.setDividerSize(0);

        //Provide minimum sizes for the two components in the split pane
        Dimension minimumSize = new Dimension(100, 67);
        leftScroll.setMinimumSize(minimumSize);
        rightPanel.setMinimumSize(minimumSize);


        JPanel sdkListPanel = new JPanel(new BorderLayout());
        sdkListPanel.add(optPanel,BorderLayout.NORTH);
        sdkListPanel.add(splitPane,BorderLayout.CENTER);

        sdkListPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(etchedBorder, "SDK List"),
                JBUI.Borders.empty(5)));

        // Add split pane to the main panel
        mainPanel.add(sdkListPanel, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));



//        copyNewButton.addMouseListener(new MouseAdapter() {
//            public void mouseEntered(MouseEvent me) {
//                copyNewButton.setBorderPainted(true);
//            }
//            public void mouseExited(MouseEvent me) {
//                copyNewButton.setBorderPainted(false);
//            }
//            public void mousePressed(MouseEvent me) {
//                // When addButton is clicked, add a new item to the list
//                String newItem = "New SDK";
//                connectionList.addElement(newItem);
//
//                SDKSettingStorage.MyItem copyConnection = new SDKSettingStorage.MyItem();
//                copyConnection.setName(nameField.getText());
//                copyConnection.setPath(sdkHomeField.getText());
//                allItems.add(copyConnection);
//
//                // Select the new item
//                list.setSelectedIndex(connectionList.getSize() - 1);
//                enableField(true);
//            }
//        });

        // Update addButton MouseAdapter
        addButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent me) {
                addButton.setBorderPainted(true);
            }
            public void mouseExited(MouseEvent me) {
                addButton.setBorderPainted(false);
            }
            public void mousePressed(MouseEvent me) {
                // When addButton is clicked, add a new item to the list
                String newItem = "New SDK";
                connectionList.addElement(newItem);
                allItems.add(new SDKSettingStorage.MyItem());

                // Select the new item
                list.setSelectedIndex(connectionList.getSize() - 1);
                nameField.setText("");
                sdkHomeField.setText("");
                enableField(true);
            }
        });

        // Update removeButton MouseAdapter
        removeButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent me) {
                removeButton.setBorderPainted(true);
            }
            public void mouseExited(MouseEvent me) {
                removeButton.setBorderPainted(false);
            }
            public void mousePressed(MouseEvent me) {
                // When removeButton is clicked, remove the selected item from the list
                int selected = list.getSelectedIndex();
                if (selected != -1) {
                    connectionList.remove(selected);
                    allItems.remove(selected);

                    list.setSelectedIndex(connectionList.getSize() - 1);
                }
                if(allItems.isEmpty()) {
                    enableField(false);
                }
            }
        });


        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { // This line prevents double events
                int selected = list.getSelectedIndex();
                if (selected != -1) {
                    nameField.setText(allItems.get(selected).getName());
                    sdkHomeField.setText(allItems.get(selected).getPath());

                }else {
                    nameField.setText("");
                    sdkHomeField.setText("");
                }
            }
        });

        if(allItems.isEmpty()) {
            enableField(false);
        }else {
            list.setSelectedIndex(0);
        }
        return mainPanel;

    }


    public java.util.List<SDKSettingStorage.MyItem> getAllItems() {
        return allItems.stream()
                .filter(f->StringUtils.isNotEmpty(f.getName()) && StringUtils.isNotEmpty(f.getPath()))
                .collect(Collectors.toList());
    }


    public String getGenDebugInfo(){
        String genDebugInfo = genDebugInfosMap.entrySet().stream().filter(entry->entry.getValue().isSelected()).map(Map.Entry::getKey).collect(Collectors.joining(","));
        return StringUtils.isEmpty(genDebugInfo) ? "none" : genDebugInfo;
    }

    public boolean isDecompiledChanged(){
        return !lastDecompiledTool.equals(decompiledToolComboBox.getSelectedItem());
    }

    @Override
    protected void doOKAction() {
        SDKSettingStorage sdkSettingStorage = SDKSettingStorage.getInstance();
        //持久化
        sdkSettingStorage.setMySdks(this.getAllItems());
        sdkSettingStorage.setGenDebugInfos(this.getGenDebugInfo());

        //刷新最高jdk版本
        int maxJavaVersion = sdkSettingStorage.getMySdks().parallelStream().map(sdk->{
            try{
                String javacVersion = CommandTools.exec(sdk.getPath()+"/bin/javac","-version");
                if(null != javacVersion) {
                    return JavacToolProvider.parseJavaVersion(javacVersion);
                }
            }catch (Throwable ex){}
            return 8;
        }).max(Integer::compareTo).orElse(8);
        maxJavaVersion = Math.max(maxJavaVersion,JavacToolProvider.parseJavaVersion(System.getProperty("java.version"))); //当前IDEA运行的java版本

        sdkSettingStorage.setMaxJavaVersion(maxJavaVersion);
        sdkSettingStorage.setDecompiledTool(DecompiledEnum.findByName((String)decompiledToolComboBox.getSelectedItem()).value);
        sdkSettingStorage.setParameters(parameters.isSelected());
        sdkSettingStorage.setProcNone(procNone.isSelected());

        super.doOKAction();
    }

}