package com.liubs.jareditor.sdk;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;
import com.liubs.jareditor.persistent.SDKSettingStorage;
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

    private Map<String,JCheckBox> genDebugInfosMap = new HashMap<>();

    public SDKSettingDialog() {
        super(true); // use current window as parent
        init();
        setTitle("SDK setting");

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
        JPanel mainPanel = new JPanel(new GridLayoutManager(3, 2));
        mainPanel.setPreferredSize(new Dimension(500, 300));

        JPanel genDebugInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel genDebugInfoLabel= new JLabel("Generate debug info(-g) : ");
        genDebugInfoPanel.add(genDebugInfoLabel);

        JCheckBox lines = new JCheckBox("lines");
        JCheckBox vars = new JCheckBox("vars");
        JCheckBox source = new JCheckBox("source");
        genDebugInfosMap.put("lines",lines);
        genDebugInfosMap.put("vars",vars);
        genDebugInfosMap.put("source",source);
        genDebugInfoPanel.add(lines);
        genDebugInfoPanel.add(vars);
        genDebugInfoPanel.add(source);
        if(StringUtils.isEmpty(SDKSettingStorage.getInstance().getGenDebugInfos()) || SDKSettingStorage.getInstance().getGenDebugInfos().contains("lines")) {
            lines.setSelected(true);
        }
        if(StringUtils.isEmpty(SDKSettingStorage.getInstance().getGenDebugInfos()) || SDKSettingStorage.getInstance().getGenDebugInfos().contains("vars")) {
            vars.setSelected(true);
        }
        if(StringUtils.isEmpty(SDKSettingStorage.getInstance().getGenDebugInfos()) || SDKSettingStorage.getInstance().getGenDebugInfos().contains("source")) {
            source.setSelected(true);
        }



        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        genDebugInfoPanel.setBorder(BorderFactory.createTitledBorder(etchedBorder,"Preferences"));


        mainPanel.add(genDebugInfoPanel, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null));



        Dimension buttonSize = new Dimension(20, 20);

        JButton copyNewButton = new JButton(AllIcons.Actions.Copy);
        copyNewButton.setOpaque(false);
        copyNewButton.setContentAreaFilled(false);
        copyNewButton.setBorderPainted(false);
        copyNewButton.setMargin(JBUI.emptyInsets());
        copyNewButton.setPreferredSize(buttonSize);
        copyNewButton.setMaximumSize(buttonSize);
        copyNewButton.setToolTipText("Copy new");

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


        JPanel mappingLabelPanel = new JPanel();
        mappingLabelPanel.setLayout(new BoxLayout(mappingLabelPanel, BoxLayout.X_AXIS));
        mappingLabelPanel.add(Box.createHorizontalStrut(10));
        mappingLabelPanel.add(copyNewButton);
        mappingLabelPanel.add(Box.createHorizontalStrut(10));
        mappingLabelPanel.add(addButton);
        mappingLabelPanel.add(Box.createHorizontalStrut(10));
        mappingLabelPanel.add(removeButton);


        // Add the new panel to the main panel
        mainPanel.add(mappingLabelPanel, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null));



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
        sdkHomeField.addBrowseFolderListener(
                "Select File",
                "Choose a file to open",
                null,
                FileChooserDescriptorFactory.createSingleFileDescriptor());

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

        // Add split pane to the main panel
        mainPanel.add(splitPane, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));



        copyNewButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent me) {
                copyNewButton.setBorderPainted(true);
            }
            public void mouseExited(MouseEvent me) {
                copyNewButton.setBorderPainted(false);
            }
            public void mousePressed(MouseEvent me) {
                // When addButton is clicked, add a new item to the list
                String newItem = "New SDK";
                connectionList.addElement(newItem);

                SDKSettingStorage.MyItem copyConnection = new SDKSettingStorage.MyItem();
                copyConnection.setName(nameField.getText());
                copyConnection.setPath(sdkHomeField.getText());
                allItems.add(copyConnection);

                // Select the new item
                list.setSelectedIndex(connectionList.getSize() - 1);
                enableField(true);
            }
        });

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


    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

}