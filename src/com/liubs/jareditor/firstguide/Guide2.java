package com.liubs.jareditor.firstguide;

import com.intellij.openapi.util.IconLoader;
import com.intellij.util.IconUtil;

import javax.swing.*;
import java.awt.*;

/**
 * @author Liubsyy
 * @date 2025/12/9
 */
public class Guide2 extends JPanel {
    public Guide2() {
        super(new BorderLayout());

        Icon icon2 = IconLoader.getIcon("/icons/guide2.png", getClass());
        JLabel guide = new JLabel(IconUtil.scale(icon2, null, 0.32f));

        JLabel textLabel = new JLabel("<html><div style='margin-top:5px;'>Open a file and switch to the <span style='color:orange'>Jar Editor</span> tab.</div>"
                + "<br><div style='margin-bottom:10px'> After modifying file, click <span style='color:orange'> Save → Build Jar</span>, you will get a modified JAR. </div>"
                +"</html>"
        );

        add(textLabel,BorderLayout.NORTH);
        add(guide,BorderLayout.CENTER);
    }
}
