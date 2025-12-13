package com.liubs.jareditor.firstguide;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBUI;
import com.liubs.jareditor.persistent.GuideStorage;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * @author Liubsyy
 * @date 2025/12/9
 */
public class FirstGuideDialog extends DialogWrapper {
    private JPanel mainPanel;

    private int[] currentGuide;
    private java.util.List<JPanel> guides;
    private JButton previous;
    private JButton next;

    public FirstGuideDialog() {
        super(true);

        guides = new ArrayList<>();
        guides.add(new Guide1());
        guides.add(new Guide2());
        guides.add(new Guide3());
        currentGuide=new int[1];

        Dimension buttonSize = new Dimension(20, 20);
        previous = new JButton(AllIcons.Actions.Play_back);
        previous.setOpaque(false);
        previous.setContentAreaFilled(false);
        previous.setBorderPainted(false);
        previous.setMargin(JBUI.emptyInsets());
        previous.setPreferredSize(buttonSize);
        previous.setMaximumSize(buttonSize);

        next = new JButton(AllIcons.Actions.Play_forward);
        next.setOpaque(false);
        next.setContentAreaFilled(false);
        next.setBorderPainted(false);
        next.setMargin(JBUI.emptyInsets());
        next.setPreferredSize(buttonSize);
        next.setMaximumSize(buttonSize);

        init();
        setTitle("JarEditor First Guide");
        pack(); //调整窗口大小以适应其子组件
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        mainPanel = new JPanel(new BorderLayout());
        Dimension panelSize  =new Dimension(700, 500);
        mainPanel.setPreferredSize(panelSize);
        mainPanel.setMinimumSize(panelSize);
        mainPanel.setMaximumSize(panelSize);
        updateGuidePanel();

        previous.addActionListener(e->{
            if(currentGuide[0] <=0) {
                return;
            }
            currentGuide[0]--;
            updateGuidePanel();
        });

        next.addActionListener(e->{
            if(currentGuide[0] >= guides.size()-1) {
                return;
            }
            currentGuide[0]++;
            updateGuidePanel();
        });
        return mainPanel;
    }

    private void updateGuidePanel(){
        mainPanel.removeAll();
        mainPanel.add(guides.get(currentGuide[0]),BorderLayout.CENTER);
        if(currentGuide[0] < guides.size()-1) {
            mainPanel.add(next, BorderLayout.EAST);
        }

        if(currentGuide[0] > 0) {
            mainPanel.add(previous, BorderLayout.WEST);
        }

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    @Override
    protected Action [] createActions() {
        Action okAction = getOKAction();
        okAction.putValue(Action.NAME, "I have read and learned it");
        return new Action[]{ okAction };
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
        GuideStorage.getInstance().setShowed(true);
    }
}
