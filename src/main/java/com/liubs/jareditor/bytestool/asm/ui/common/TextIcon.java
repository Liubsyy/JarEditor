package com.liubs.jareditor.bytestool.asm.ui.common;

import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;

/**
 * @author Liubsyy
 * @date 2024/10/20
 */
public class TextIcon implements Icon {
    private final String text;
    private final int width;
    private final int height;
    private final Color textColor;
    private final Font font;

    public TextIcon(String text) {
        this.text = text;
        this.font = new Font("Arial", Font.BOLD, 12); // 您可以根据需要调整字体
        this.textColor = JBColor.GREEN;

        // 计算图标的宽度和高度
        FontMetrics metrics = new JLabel().getFontMetrics(font);
        this.width = metrics.stringWidth(text);
        this.height = metrics.getHeight();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setFont(font);
        g.setColor(textColor);
        g.drawString(text, x, y + height - 2); // 调整 y 坐标以正确显示文本
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }
}
