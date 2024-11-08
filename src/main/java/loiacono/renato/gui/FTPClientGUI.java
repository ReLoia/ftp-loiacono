package loiacono.renato.gui;

import loiacono.renato.StringsHandler;
import loiacono.renato.api.FTPClient;
import loiacono.renato.api.data.Response;
import loiacono.renato.gui.components.Content;
import loiacono.renato.gui.components.RightPanel;
import loiacono.renato.gui.components.TopBar;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Locale;

/*
 Allora, la gui fa proprio schifo.
 E' solo un esempio per arricchire il codice e poter utilizzare i thread.
 */
public class FTPClientGUI extends JFrame {
    public StringsHandler stringsHandler = new StringsHandler(Locale.getDefault());

    boolean logging = true;

    // Posizione della finestra
    public int x;
    public int y;
//    public JFrame mainFrame;

    public RightPanel rightPanel;
    public JTextField hostField;
    public JTextField portField;

    // Le variabili andrebbero spostate in un file a parte
    // ma la gui è solo un esempio
    private final int rightPanelWidth = 240;

    private FTPClient client;

    public void sendCommand(String command) {
        if (client == null) {
            rightPanel.log("Connection not established", RightPanel.Level.ERROR);
            return;
        }
        rightPanel.log(command, RightPanel.Level.INFO);
        client._sendCommand(command);
    }

    public void openConnection(String host, int port) {
        new Thread(() -> {
            try {
                client = new FTPClient(host, port);
                subscribeToControlReader();
                rightPanel.log("Connection established", RightPanel.Level.INFO);
                login();
            } catch (IOException e) {
                rightPanel.log(e);
                if (client != null) {
                    try {
                        client.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    } finally {
                        clearLog();
                    }
                }
                e.printStackTrace();
            //            throw new RuntimeException(e);
            }
        }).start();
    }

    // ask username and password using JDialog
    private void login() {
        JDialog dialog = new JDialog(this, "Login", true);
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(3, 2));

        JLabel userLabel = new JLabel("Username");
        JTextField userField = new JTextField();
        JLabel passLabel = new JLabel("Password");
        JPasswordField passField = new JPasswordField();
        JButton loginButton = new JButton("Login");
        JButton cancelButton = new JButton("Cancel");

        loginButton.addActionListener(e -> {
            String user = userField.getText();
            String pass = new String(passField.getPassword());
            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Username and password are required", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Disable the dialog to prevent further input while processing
            loginButton.setEnabled(false);
            cancelButton.setEnabled(false);

            // Process login in a separate thread
            logging = false;
            new Thread(() -> {
                try {
                    Response userResponse = client.sendCommand("USER " + user);
                    if (userResponse.code() == Response.FTPResponseCode.USERNAME_OK) {
                        Response passResponse = client.sendCommand("PASS " + pass);
                        if (passResponse.code() == Response.FTPResponseCode.USER_LOGGED_IN) {
                            SwingUtilities.invokeLater(() -> {
                                dialog.dispose();
                                rightPanel.log("Login successful", RightPanel.Level.INFO);
                            });
                            logging = true;
                        } else {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(dialog, "Invalid password", "Error", JOptionPane.ERROR_MESSAGE);
                                loginButton.setEnabled(true);
                                cancelButton.setEnabled(true);
                            });
                            logging = true;
                        }
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(dialog, "Invalid username", "Error", JOptionPane.ERROR_MESSAGE);
                            loginButton.setEnabled(true);
                            cancelButton.setEnabled(true);
                        });
                        logging = true;
                    }
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        loginButton.setEnabled(true);
                        cancelButton.setEnabled(true);
                    });
                    logging = true;
                }
            }).start();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(userLabel);
        dialog.add(userField);
        dialog.add(passLabel);
        dialog.add(passField);
        dialog.add(loginButton);
        dialog.add(cancelButton);
        dialog.setVisible(true);
    }

    private void clearLog() {
        rightPanel.clearLog();
    }

    private void subscribeToControlReader() {
        new Thread(() -> {
            while (logging) {
                Response response = client.getResponse();
                rightPanel.log(response.message(), RightPanel.Level.INFO);
            }
        }).start();
    }

    public FTPClientGUI() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 600);

        // Con questo il mainFrame viene creato al centro dello schermo.
        this.setLocationRelativeTo(null);
        // Sfondo nero
        this.getContentPane().setBackground(new Color(16, 16, 16));

        // Rimuovo la parte superiore della finestra
        this.setUndecorated(true);

        // Questo è per posizionare i componenti manualmente
        this.setLayout(null);

        /*
         * La struttura è la seguente:
         *
         * SSSSSSSSSS
         * CCCCCCCRRR
         * CCCCCCCRRR
         * CCCCCCCRRR
         */

        // S
        this.add(new TopBar(this));
        // C
        this.add(new Content(this, 800 - rightPanelWidth));
        // R
        rightPanel = new RightPanel(this, rightPanelWidth);
        this.add(rightPanel);

        this.setVisible(true);

        // https://stackoverflow.com/questions/2000218/what-is-inline-thread
      //  SwingUtilities.invokeLater(new Runnable() {
        //    @Override
        //    public void run() {
        //        rightPanel.log("Prova", RightPanel.Level.WARNING);
        //        rightPanel.log("Prova", RightPanel.Level.ERROR);
        //        rightPanel.log("Prova", RightPanel.Level.INFO);
        //    }
        //});
    }
}
