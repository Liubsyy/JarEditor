package com.liubs.jareditor.editor;

/**
 * @author Liubsyy
 * @date 2024/10/13
 */
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.jar.JarEntry;

public class BuildJarSelection extends DialogWrapper {

    private ComboBox<String> jarComboBox;
    private ComboBox<String> methodComboBox;
    private String[] nestedJars;
    private String[] methods = {"STORED", "DEFLATED"};  // Method options

    public BuildJarSelection(String[] nestedJars) {
        super(true);
        this.nestedJars = nestedJars;
        setTitle("Nested Jar Build");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        // Create main panel with a BoxLayout for vertical alignment
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Add padding/margin around the panel
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // First row: "Build Jar" label and jarComboBox
        JPanel jarPanel = new JPanel();
        jarPanel.setLayout(new BoxLayout(jarPanel, BoxLayout.X_AXIS));  // Horizontal alignment
        JLabel jarLabel = new JLabel("Build Jar : ");
        jarComboBox = new ComboBox<>(nestedJars);
        jarPanel.add(jarLabel);
        jarPanel.add(Box.createRigidArea(new Dimension(10, 0)));  // Add horizontal spacing
        jarPanel.add(jarComboBox);

        // Set preferred size for the ComboBox to make it larger
        jarComboBox.setPreferredSize(new Dimension(200, 30));
        panel.add(jarPanel);

        // Add vertical spacing between the first row and second row
        panel.add(Box.createRigidArea(new Dimension(0, 10)));  // 10px vertical space

        // Second row: "Nested Jar Method" label and methodComboBox
        JPanel methodPanel = new JPanel();
        methodPanel.setLayout(new BoxLayout(methodPanel, BoxLayout.X_AXIS));  // Horizontal alignment
        JLabel methodLabel = new JLabel("Nested Jar Method : ");
        methodComboBox = new ComboBox<>(methods);
        methodPanel.add(methodLabel);
        methodPanel.add(Box.createRigidArea(new Dimension(10, 0)));  // Add horizontal spacing
        methodPanel.add(methodComboBox);

        // Set preferred size for the method ComboBox
        methodComboBox.setPreferredSize(new Dimension(200, 30));
        panel.add(methodPanel);

        return panel;
    }

    // Getter method to retrieve the selected jar option
    public int getSelectedJar() {
        return jarComboBox.getSelectedIndex();
    }

    // Getter method to retrieve the selected method option
    public int getSelectedMethod() {
        return methodComboBox.getSelectedIndex() == 0 ? JarEntry.STORED : JarEntry.DEFLATED;
    }
}
