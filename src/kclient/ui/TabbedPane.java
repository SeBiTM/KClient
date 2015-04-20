package kclient.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;
import kclient.knuddels.GroupChat;
import kclient.knuddels.reflection.KClass;

/**
 *
 * @author SeBi
 */
public class TabbedPane extends JTabbedPane {
    public void addTab(String title, Component com, boolean close) {
        super.addTab(title, com);
        if (close)
            super.setTabComponentAt(super.getTabCount() - 1, new ButtonPanel(this));
    }
    
    private class ButtonPanel extends JPanel {
        public ButtonPanel(final JTabbedPane pane) {
            setOpaque(false);
            JLabel label = new JLabel() {
                @Override
                public String getText() {
                    int i = pane.indexOfTabComponent(ButtonPanel.this);
                    if (i != -1) {
                        return pane.getTitleAt(i);
                    }
                    return null;
                }
            };

            add(label);
            label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            JButton button = new TabButton(pane, this);
            add(button);
            setBorder(BorderFactory.createEmptyBorder(-5, 0, -5, 0));
        }
    }
    
    private class TabButton extends JButton implements ActionListener {
        private final JTabbedPane pane;
        private final ButtonPanel bpanel;
       
        public TabButton(JTabbedPane pane, ButtonPanel bp) {
            this.pane = pane;
            this.bpanel = bp;
            int size = 17;
            setPreferredSize(new Dimension(size, size));
            setToolTipText("Tab schließen");
            setUI(new BasicButtonUI());
            setContentAreaFilled(false);
            setFocusable(false);
            setBorderPainted(false);
            setRolloverEnabled(true);
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int i = pane.indexOfTabComponent(bpanel);
            if (i != -1) {
                Component component = pane.getComponentAt(i);
                if (component.getClass().getName().equals("Start")) {
                    ((GroupChat)new KClass(component).getField("tunnel")).stop();
                }
                pane.remove(i);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            if (getModel().isPressed()) {
                g2.translate(1, 1);
            }
            g2.setColor(Color.BLACK);
            if (getModel().isRollover()) {
                g2.setColor(Color.RED);
            }
            int delta = 11;
            g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
            g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
            g2.dispose();
        }
    }

}
