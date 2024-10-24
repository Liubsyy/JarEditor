package com.liubs.jareditor.bytestool.asm.ui.common;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBLabel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * @author Liubsyy
 * @date 2024/10/24
 */
public class EditableLabel extends JPanel {
    private JLabel jLabel;
    private JBLabel editAction;

    public EditableLabel() {
        jLabel = new JLabel();
        editAction = new JBLabel(AllIcons.Actions.Edit);
        editAction.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // 设置手形指针

        setLayout(new FlowLayout(FlowLayout.LEFT, 20, 0));
        add(jLabel);
        add(editAction);
    }

    public void onClick(OnclickListener onClickListener){
        editAction.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onClickListener.actionPerform();
            }
        });
    }


    public void setText(String text) {
        jLabel.setText(text);
    }

    public String getText() {
        return jLabel.getText();
    }


    public interface OnclickListener{
        void actionPerform();
    }


    /**
     * 输入框编辑文本
     * @param resultHandler
     */
    public void onActionForInput(String message, InputValidator inputValidator, Consumer<String> resultHandler ) {
        editAction.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String inputValue = Messages.showInputDialog((Project)null,message,
                        "Edit Value", Messages.getInformationIcon(),
                        getText(),inputValidator);
                resultHandler.accept(inputValue);
                setText(inputValue);
            }
        });
    }
}
