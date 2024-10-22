package loiacono.renato.gui;

import loiacono.renato.StringsHandler;
import loiacono.renato.api.FTPClient;

import javax.swing.*;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.*;
import java.io.IOException;
import java.util.Locale;

/*
 Allora, la gui fa proprio schifo.
 E' solo un esempio per arricchire il codice e poter utilizzare i thread.
 */
public class FTPClientGUI {
    // Posizione della finestra
    int x;
    int y;
    JFrame mainFrame;
    JTextArea logArea;
    JTextField hostField;
    JTextField portField;

    // Le variabili andrebbero spostate in un file a parte
    // ma la gui è solo un esempio
    int rightPanelWidth = 240;

    StringsHandler stringsHandler = new StringsHandler(Locale.getDefault());

    FTPClient client;

    private void connect() {

    }

    public void start() {
        mainFrame = new JFrame("FTP Client");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(800, 600);

        // Con questo il mainFrame viene creato al centro dello schermo.
        mainFrame.setLocationRelativeTo(null);
        // Sfondo nero
        mainFrame.getContentPane().setBackground(new Color(16, 16, 16));

        // Rimuovo la parte superiore della finestra
        mainFrame.setUndecorated(true);

        // Questo è per posizionare i componenti manualmente
        mainFrame.setLayout(null);
        mainFrame.add(topBar());

        mainFrame.add(content(800 - rightPanelWidth));
        mainFrame.add(rightPanel(rightPanelWidth));

        mainFrame.setVisible(true);
    }

    private JPanel topBar() {
        JPanel topBar = new JPanel();
        topBar.setBackground(new Color(32, 32, 32));
        topBar.setSize(800, 35);
        topBar.setLocation(0, 0);
        topBar.setLayout(null);

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
        topBar.add(closeButton);

        topBar.add(title);

        // https://stackoverflow.com/questions/32159065/how-to-grab-a-mouse-in-java-swing
        topBar.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                x = evt.getX();
                y = evt.getY();
            }
        });
        topBar.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent evt) {
                mainFrame.setLocation(evt.getXOnScreen() - x, evt.getYOnScreen() - y);
                mainFrame.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            public void mouseMoved(MouseEvent evt) {
                mainFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        return topBar;
    }

    private JPanel content(int width) {
        JPanel content = new JPanel();
        content.setBackground(new Color(25, 25, 25));
        content.setSize(width, 565);
        content.setLocation(0, 35);
        content.setLayout(null);

        // Mini Titolo
        JLabel miniTitle = new JLabel(stringsHandler.getString("connection").toUpperCase());
        miniTitle.setForeground(new Color(255, 255, 255));
        miniTitle.setBackground(new Color(32, 32, 32));
        miniTitle.setOpaque(true);
        miniTitle.setFont(new Font("Arial", Font.PLAIN, 14));
        miniTitle.setSize(width, 20);
        miniTitle.setLocation(0, 0);
        miniTitle.setHorizontalAlignment(SwingConstants.CENTER);
        content.add(miniTitle);

        // Host
        JLabel hostLabel = new JLabel(stringsHandler.getString("host"));
        hostLabel.setForeground(new Color(255, 255, 255));
        hostLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        hostLabel.setSize(100, 20);
        hostLabel.setLocation(10, 30);
        content.add(hostLabel);

        hostField = new JTextField();
        hostField.setBackground(new Color(32, 32, 32));
        hostField.setForeground(new Color(255, 255, 255));
        hostField.setFont(new Font("Arial", Font.PLAIN, 14));
        hostField.setSize(180, 30);
        hostField.setLocation(10, 50);
        hostField.setBorder(BorderFactory.createCompoundBorder(
                null,
                BorderFactory.createEmptyBorder(0, 5, 0, 5)
        ));
        content.add(hostField);

        // Port
        JLabel portLabel = new JLabel(stringsHandler.getString("port"));
        portLabel.setForeground(new Color(255, 255, 255));
        portLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        portLabel.setSize(100, 20);
        portLabel.setLocation(210, 30);
        content.add(portLabel);

        portField = new JTextField();
        portField.setBackground(new Color(32, 32, 32));
        portField.setForeground(new Color(255, 255, 255));
        portField.setFont(new Font("Arial", Font.PLAIN, 14));
        portField.setSize(100, 30);
        portField.setLocation(210, 50);
        portField.setBorder(BorderFactory.createCompoundBorder(
                null,
                BorderFactory.createEmptyBorder(0, 5, 0, 5)
        ));
        content.add(portField);

        // Connect
        JButton connectButton = new JButton(stringsHandler.getString("connect"));
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
                client = new FTPClient(host, port);
            } catch (IOException ex) {
                // TODO: mandare in Log il messaggio di errore.
                throw new RuntimeException(ex);
            }
        });
        content.add(connectButton);



        return content;
    }

    private JPanel rightPanel(int width) {
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(new Color(32, 32, 32));
        rightPanel.setSize(width, 565);
        rightPanel.setLocation(800 - width, 35);
        rightPanel.setLayout(null);

        // Mini Titolo
        JLabel miniTitle = new JLabel(stringsHandler.getString("logs").toUpperCase());
        miniTitle.setForeground(new Color(255, 255, 255));
        miniTitle.setFont(new Font("Arial", Font.PLAIN, 14));
        miniTitle.setSize(width, 20);
        miniTitle.setLocation(0, 0);
        miniTitle.setHorizontalAlignment(SwingConstants.CENTER);
        rightPanel.add(miniTitle);

        // Logs
        logArea = new JTextArea();
        logArea.setBackground(new Color(54, 54, 54));
        logArea.setForeground(new Color(255, 255, 255));
        logArea.setFont(new Font("Arial", Font.PLAIN, 14));
        logArea.setSize(width, 510);
        logArea.setLocation(0, 20);
        logArea.setEditable(false);
        rightPanel.add(logArea);

        // Input
        JTextField inputField = new JTextField(stringsHandler.getString("type_command"));
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
                if (inputField.getText().equals(stringsHandler.getString("type_command"))) {
                    inputField.setText("");
                }
            }
            public void focusLost(FocusEvent evt) {
                if (inputField.getText().isEmpty()) {
                    inputField.setText(stringsHandler.getString("type_command"));
                }
            }
        });
        rightPanel.add(inputField);

        return rightPanel;
    }
}
