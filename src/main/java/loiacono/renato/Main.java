package loiacono.renato;
import loiacono.renato.api.FTPClient;
import loiacono.renato.cli.FTPClientCLI;
import loiacono.renato.gui.FTPClientGUI;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.UnrecognizedOptionException;

import java.util.Locale;

/*
ftp.dlptest.com 21
dlpuser rNrKYTX9g7z3RgJRmxWuGHbeu
 */

/**
 * Entry point del programma.
 * Gestisce le opzioni da riga di comando.
 */
public class Main {
    public static void main(String[] args) {
        StringsHandler stringsHandler = new StringsHandler(Locale.getDefault());

        Options options = new Options();
        options.addOption("H", "help", false, "Print this help message");
        options.addOption("G", "gui", false, "Start the GUI");

        options.addOption("host", true, "The host to connect to");
        options.addOption("P", "port", true, "The port to connect to");


        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (UnrecognizedOptionException e) {
            System.out.println(stringsHandler.getString("unrecognized_option", e.getOption()));
            System.exit(1);
            return;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        // Posso usare 'cmd' qui perchè se è null il programma è già terminato.

        if (cmd.hasOption("H")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("ftp-client [OPTIONS]", options);
            System.exit(0); // Chiudo il programma dato che l'utente ha usato -H
        }

        if (cmd.hasOption("G")) {
            new FTPClientGUI().start();
            return;
        } else if (!cmd.hasOption("host") || !cmd.hasOption("port")) {
            System.out.println(stringsHandler.getString("missing_host_port"));
            System.exit(1);
        }

        String host = cmd.getOptionValue("host");
        int port = Integer.parseInt(cmd.getOptionValue("port"));

        new FTPClientCLI(host, port);
    }
}