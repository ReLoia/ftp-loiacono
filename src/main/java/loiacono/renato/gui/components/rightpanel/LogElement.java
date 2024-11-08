package loiacono.renato.gui.components.rightpanel;

import loiacono.renato.gui.components.RightPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LogElement extends JPanel {
    public LogElement(String message, RightPanel.Level level, int width) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JTextArea tA = new JTextArea();
        tA.setLineWrap(true);
        tA.setWrapStyleWord(true);
        tA.setOpaque(false);

        setBorder(new EmptyBorder(3, 2, 3, 2));
        setLayout(new BorderLayout());

        switch (level) {
            case INFO -> {
                tA.setForeground(Color.WHITE);
                setOpaque(false);
            }
            case WARNING -> {
                tA.setForeground(Color.YELLOW);
                setBackground(new Color(48, 46, 7));
            }
            case ERROR -> {
                tA.setForeground(Color.RED);
                setBackground(new Color(49, 13, 7));
            }
        }

        tA.setText(message);
        tA.setEditable(false);

        setPreferredSize(new Dimension(width, 20));

        this.add(tA, BorderLayout.CENTER);
    }
}
