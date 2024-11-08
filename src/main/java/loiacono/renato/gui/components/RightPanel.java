package loiacono.renato.gui.components;

import loiacono.renato.gui.FTPClientGUI;
import loiacono.renato.gui.components.rightpanel.LogElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class RightPanel extends JPanel {
    private final JPanel logArea;

    public enum Level {
        INFO,
        WARNING,
        ERROR
    }

    private final int width;

    public RightPanel(FTPClientGUI INSTANCE, int width) {
        this.width = width;

        this.setBackground(new Color(32, 32, 32));
        this.setSize(width, 565);
        this.setLocation(800 - width, 35);
        this.setLayout(null);

        // Mini Titolo
        JLabel miniTitle = new JLabel(INSTANCE.stringsHandler.getString("logs").toUpperCase());
        miniTitle.setForeground(new Color(255, 255, 255));
        miniTitle.setFont(new Font("Arial", Font.PLAIN, 14));
        miniTitle.setSize(width, 20);
        miniTitle.setLocation(0, 0);
        miniTitle.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(miniTitle);

        // Logs
        logArea = new JPanel();
        logArea.setBackground(new Color(54, 54, 54));
        logArea.setForeground(new Color(255, 255, 255));
        logArea.setFont(new Font("Arial", Font.PLAIN, 14));
        logArea.setLayout(new BoxLayout(logArea, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setSize(width, 510);
        scrollPane.setLocation(0, 20);
//        logArea.setEditable(false);

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        this.add(scrollPane);

        // Input
        JTextField inputField = new JTextField(INSTANCE.stringsHandler.getString("type_command"));
        inputField.setBackground(new Color(32, 32, 32));
        inputField.setForeground(new Color(255, 255, 255));
        inputField.setFont(new Font("Arial", Font.PLAIN, 14));
        inputField.setSize(width, 35);
        inputField.setLocation(0, 530);
        // https://stackoverflow.com/questions/8792651/how-can-i-add-padding-to-a-jtextfield
        inputField.setBorder(BorderFactory.createCompoundBorder(
                null,
                BorderFactory.createEmptyBorder(0, 5, 0, 5)
        ));
        // https://stackoverflow.com/questions/16213836/java-swing-jtextfield-set-placeholder
        inputField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                if (inputField.getText().equals(INSTANCE.stringsHandler.getString("type_command"))) {
                    inputField.setText("");
                }
            }
            public void focusLost(FocusEvent evt) {
                if (inputField.getText().isEmpty()) {
                    inputField.setText(INSTANCE.stringsHandler.getString("type_command"));
                }
            }
        });
        inputField.addActionListener(e -> {
            String command = inputField.getText();
            if (command.isEmpty()) return;
            INSTANCE.sendCommand(command);
            inputField.setText("");
        });
        this.add(inputField);
    }

    public void clearLog() {
        logArea.removeAll();
        this.revalidate();
        this.repaint();
    }

    public void log(String message, Level level) {
        logArea.add(new LogElement(message, level, width));
        logArea.add(Box.createRigidArea(new Dimension(0, 5)));
        this.revalidate();
        this.repaint();
    }

    public void log(Exception e) {
        log("%s: %s".formatted(e.getClass().getName(), e.getLocalizedMessage()), Level.ERROR);
    }
}
