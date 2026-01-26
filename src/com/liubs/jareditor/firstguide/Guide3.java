package com.liubs.jareditor.firstguide;

import com.intellij.openapi.util.IconLoader;
import com.intellij.util.IconUtil;

import javax.swing.*;
import java.awt.*;

/**
 * @author Liubsyy
 * @date 2025/12/9
 */
public class Guide3 extends JPanel {
    public Guide3() {
        super(new BorderLayout());

        Icon icon2 = IconLoader.getIcon("/icons/guide3.png", getClass());
        JLabel guide = new JLabel(IconUtil.scale(icon2, null, 0.32f));

        JLabel textLabel = new JLabel("<html><div style='margin-top:5px;margin-bottom:10px'>In the project view of the jar package, right-click to see JarEditor->New/Delete and other operations, where you can add/delete/rename/copy/paste/export/backup files.</div>"
                +"</html>"
        );

        add(textLabel,BorderLayout.NORTH);
        add(guide,BorderLayout.CENTER);
    }
}
