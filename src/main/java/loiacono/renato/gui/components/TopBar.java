package loiacono.renato.gui.components;

import loiacono.renato.gui.FTPClientGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class TopBar extends JPanel {
    public TopBar(FTPClientGUI INSTANCE) {
        this.setBackground(new Color(32, 32, 32));
        this.setSize(800, 35);
        this.setLocation(0, 0);
        this.setLayout(null);

        JLabel title = new JLabel("FTP Client");
        title.setForeground(new Color(255, 255, 255));
        title.setFont(new Font("Arial", Font.PLAIN, 20));
        title.setSize(200, 35);
        title.setLocation(10, 0);

        // bottone di chiusura
        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("Arial", Font.PLAIN, 26));
        closeButton.setBackground(new Color(255, 32, 32));
        closeButton.setForeground(new Color(255, 255, 255));
        closeButton.setBorder(null);
        closeButton.setFocusPainted(false);
        closeButton.setSize(35, 35);
        closeButton.setLocation(765, 0);
        closeButton.addActionListener(e -> System.exit(0));
        this.add(closeButton);

        this.add(title);

        // https://stackoverflow.com/questions/32159065/how-to-grab-a-mouse-in-java-swing
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                INSTANCE.x = evt.getX();
                INSTANCE.y = evt.getY();
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent evt) {
                INSTANCE.setLocation(evt.getXOnScreen() - INSTANCE.x, evt.getYOnScreen() - INSTANCE.y);
                INSTANCE.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            public void mouseMoved(MouseEvent evt) {
                INSTANCE.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }
}
