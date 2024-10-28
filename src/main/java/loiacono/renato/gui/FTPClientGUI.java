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

    public void openConnection(String host, int port) {
        new Thread(() -> {
            try {
                client = new FTPClient(host, port);
                subscribeToControlReader();
            } catch (IOException e) {
                rightPanel.log(e);
                e.printStackTrace();
            //            throw new RuntimeException(e);
            }
        }).start();
    }

    private void subscribeToControlReader() {
        new Thread(() -> {
            while (true) {
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
