package loiacono.renato.gui.components;

import loiacono.renato.api.FTPClient;
import loiacono.renato.gui.FTPClientGUI;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Content extends JPanel {
    public Content(FTPClientGUI INSTANCE, int width) {
        this.setBackground(new Color(25, 25, 25));
        this.setSize(width, 565);
        this.setLocation(0, 35);
        this.setLayout(null);

        // Mini Titolo
        JLabel miniTitle = new JLabel(INSTANCE.stringsHandler.getString("connection").toUpperCase());
        miniTitle.setForeground(new Color(255, 255, 255));
        miniTitle.setBackground(new Color(32, 32, 32));
        miniTitle.setOpaque(true);
        miniTitle.setFont(new Font("Arial", Font.PLAIN, 14));
        miniTitle.setSize(width, 20);
        miniTitle.setLocation(0, 0);
        miniTitle.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(miniTitle);

        // Host
        JLabel hostLabel = new JLabel(INSTANCE.stringsHandler.getString("host"));
        hostLabel.setForeground(new Color(255, 255, 255));
        hostLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        hostLabel.setSize(100, 20);
        hostLabel.setLocation(10, 30);
        this.add(hostLabel);

        JTextField hostField = new JTextField();
        hostField.setBackground(new Color(32, 32, 32));
        hostField.setForeground(new Color(255, 255, 255));
        hostField.setFont(new Font("Arial", Font.PLAIN, 14));
        hostField.setSize(180, 30);
        hostField.setLocation(10, 50);
        hostField.setBorder(BorderFactory.createCompoundBorder(
                null,
                BorderFactory.createEmptyBorder(0, 5, 0, 5)
        ));
        this.add(hostField);
        INSTANCE.hostField = hostField;

        // Port
        JLabel portLabel = new JLabel(INSTANCE.stringsHandler.getString("port"));
        portLabel.setForeground(new Color(255, 255, 255));
        portLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        portLabel.setSize(100, 20);
        portLabel.setLocation(210, 30);
        this.add(portLabel);

        JTextField portField = new JTextField();
        portField.setBackground(new Color(32, 32, 32));
        portField.setForeground(new Color(255, 255, 255));
        portField.setFont(new Font("Arial", Font.PLAIN, 14));
        portField.setSize(100, 30);
        portField.setLocation(210, 50);
        portField.setBorder(BorderFactory.createCompoundBorder(
                null,
                BorderFactory.createEmptyBorder(0, 5, 0, 5)
        ));
        this.add(portField);
        INSTANCE.portField = portField;

        // Connect
        JButton connectButton = new JButton(INSTANCE.stringsHandler.getString("connect"));
        connectButton.setBackground(new Color(32, 32, 32));
        connectButton.setForeground(new Color(255, 255, 255));
        connectButton.setFont(new Font("Arial", Font.PLAIN, 14));
        connectButton.setSize(100, 30);
        connectButton.setLocation(320, 50);
        connectButton.setFocusPainted(false);
        connectButton.addActionListener(e -> {
            String host = hostField.getText();
            int port = Integer.parseInt(portField.getText());
            try {
                INSTANCE.client = new FTPClient(host, port);
            } catch (IOException ex) {
                // TODO: mandare in Log il messaggio di errore.
                throw new RuntimeException(ex);
            }
        });
        this.add(connectButton);
    }
}
