package com.liubs.jareditor.structure;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.liubs.jareditor.constant.JarConstant;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.jar.JarEntry;

/**
 * @author Liubsyy
 * @date 2024/10/14
 */
public class CompressionMethodDialog extends DialogWrapper {

    private ComboBox<String> methodComboBox;
    private String entryName;
    private int method;

    public CompressionMethodDialog(String entryName,int method) {
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
        methodComboBox = new ComboBox<>(JarConstant.COMPRESSION_METHODS);
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
