package com.liubs.jareditor.firstguide;

import com.intellij.openapi.util.IconLoader;
import com.intellij.util.IconUtil;

import javax.swing.*;
import java.awt.*;

/**
 * @author Liubsyy
 * @date 2025/12/9
 */
public class Guide1 extends JPanel {
    public Guide1() {
        super(new BorderLayout());

        JLabel icon1 = new JLabel(IconUtil.scale(IconLoader.getIcon("/icons/guide1_1.png",
                getClass()), null, 0.4f));

        JLabel icon2 = new JLabel(IconUtil.scale(IconLoader.getIcon("/icons/guide1_2.png",
                getClass()), null, 0.4f));

        JLabel textLabel = new JLabel("<html><div style='margin-top:5px;'>If you cannot see the contents of the files in JAR, try the following steps:</div>"
                + "<br><div style='margin-bottom:10px;color:orange'> File → Project Structure → Libraries → + → Java</div>"
                +"</html>"
        );

        JPanel picPanal = new JPanel(new FlowLayout());
        picPanal.add(icon2);
        picPanal.add(icon1);

        add(textLabel,BorderLayout.NORTH);
        add(picPanal,BorderLayout.CENTER);

    }
}
