package loiacono.renato;

import loiacono.renato.ftp.FTPClient;

import java.util.Scanner;

/**
 * CLI program for the FTP client.
 */
public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println(new Main().help());
            System.exit(1);
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        FTPClient c = new FTPClient(host, port);
    }

    public String help() {
        return """
            Loiacono's FTP Client
            
            Usage:
                java -jar ftp-client.jar <host> <port>
            """;
    }
}