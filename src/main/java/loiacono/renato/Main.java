package loiacono.renato;

import loiacono.renato.cli.FTPClientCLI;
import loiacono.renato.gui.FTPClientGUI;
import org.apache.commons.cli.*;

import java.util.Locale;

import static loiacono.renato.Utils.log;

/*
CREDENZIALI GRATUITE DI TEST **NON MIE**

ftp.dlptest.com 21
dlpuser rNrKYTX9g7z3RgJRmxWuGHbeu
 */

/*
 - https://sftpcloud.io/tools/free-ftp-server
    eu-central-1.sftpcloud.io 21
 */

/**
 * Entry point del programma.
 * Gestisce le opzioni da riga di comando.
 */
public class Main {
    public static void main(String[] args) {
        StringsHandler stringsHandler = new StringsHandler(Locale.getDefault());

        Options options = new Options();
        options.addOption("H", "help", false, stringsHandler.getString("main_help_H"));
        options.addOption("G", "gui", false, stringsHandler.getString("main_help_G"));

        options.addOption("host", true, stringsHandler.getString("main_help_h"));
        options.addOption("P", "port", true, stringsHandler.getString("main_help_P"));


        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (UnrecognizedOptionException e) {
            log(stringsHandler.getString("main_help_unrecognized_option", e.getOption()));
            System.exit(1);
            return;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        // Posso usare 'cmd' qui perchè se è null il programma è già terminato.

        if (cmd.hasOption("G")) {
            new FTPClientGUI();
            return;
        }

        if (cmd.hasOption("H") || !cmd.hasOption("host") || !cmd.hasOption("port")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("ftp-client [OPTIONS]", options);
            System.exit(0); // Chiudo il programma dato che l'utente ha usato -H
        }

        String host = cmd.getOptionValue("host");
        int port = Integer.parseInt(cmd.getOptionValue("port"));

        new FTPClientCLI(host, port);
    }
}