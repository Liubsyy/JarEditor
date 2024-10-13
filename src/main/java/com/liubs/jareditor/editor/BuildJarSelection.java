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

public class BuildJarSelection extends DialogWrapper {

    private ComboBox<String> comboBox;
    private String[] nestedJars;

    public BuildJarSelection(String[] nestedJars) {
        super(true);
        this.nestedJars = nestedJars;
        setTitle("Build Jar");
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

        // Add label with the specified text and some spacing
        JLabel label = new JLabel("This is a nested jar, which jar do you want to build?");
        label.setAlignmentX(Component.LEFT_ALIGNMENT); // Align left
        panel.add(label);

        // Add vertical spacing between label and combobox
        panel.add(Box.createRigidArea(new Dimension(0, 10))); // 10px vertical space

        // Create a ComboBox with the jar options
        comboBox = new ComboBox<>(nestedJars);
        comboBox.setAlignmentX(Component.LEFT_ALIGNMENT); // Align left
        panel.add(comboBox);

        // Set preferred size for the ComboBox to make it larger
        comboBox.setPreferredSize(new Dimension(200, 30));
        return panel;
    }

    // Getter method to retrieve the selected option
    public int getSelectedJar() {
        return comboBox.getSelectedIndex();
    }

}