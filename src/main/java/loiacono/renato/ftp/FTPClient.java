package loiacono.renato.ftp;

import loiacono.renato.ftp.data.Response;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Client FTP creato secondo le specifiche del <a href="https://datatracker.ietf.org/doc/html/rfc959">RFC 959</a>.
 * <p>
 *  DONE List:
 *  - Login di Page 57 (USER, PASS e ACCT) - ACCT non implementato :(
 *
 * <p>
 *  TODO List:
 * <p>
 * Autore: Renato Loiacono
 */

public class FTPClient {
    Socket controlSocket;
    BufferedReader controlReader;
    Scanner scanner = new Scanner(System.in);

    public FTPClient(String host, int port) {
        try {
            controlSocket = new Socket(host, port);
            controlReader = new BufferedReader(new java.io.InputStreamReader(controlSocket.getInputStream()));

            System.out.println(getResponse().message());

            // Accesso
            do {
                System.out.print("USER> ");
                String username = scanner.nextLine();
                sendCommand("USER " + username + "\r\n");

                Response userResponse = getResponse();
                System.out.println(userResponse.message());

                if (userResponse.code().startsWith("332")) {
                    System.out.println("The server requires a ACCounT (ACCT) to be provided. But this feature is not implemented.");
                    System.exit(1);
                }

                Console cons = System.console();
                if (cons == null) {
                    System.out.println("No console found: the password will be shown in clear text.");
                    System.out.print("PASS> ");
                    String password = scanner.nextLine();
                    sendCommand("PASS " + password + "\r\n");
                } else {
                    System.out.print("PASS> ");
                    char[] password = cons.readPassword();
                    sendCommand("PASS " + new String(password) + "\r\n");
                }

                Response passResponse = getResponse();
                System.out.println(passResponse.message());

                if (passResponse.code().startsWith("230"))
                    break;
                else if (userResponse.code().startsWith("332")) {
                    System.out.println("The server requires a ACCounT (ACCT) to be provided. But this feature is not implemented.");
                    System.exit(1);
                }
            } while (true);

            shell();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendCommand(String command) {
        try {
            controlSocket.getOutputStream().write(command.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Response getResponse() {
        String response_code;
        String response_text;
        try {
            response_text = controlReader.readLine();
            response_code = response_text.substring(0, 4);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Se la risposta è multi-linea
        if (response_code.charAt(3) == '-') { // Quindi la prima risposta finisce con '-'
            response_text += "\n" + getResponse().message();
        }
        else if (response_code.charAt(0) == ' ') { // e le successive con ' ' - le carica tutte
            response_text += "\n" + getResponse().message();
        }

        return new Response(response_code, response_text);
    }

    private void shell() {
        while (true) {
            System.out.print("ftp> ");
            String command = scanner.nextLine();
            if (command.isEmpty()) continue;
            sendCommand(command + "\r\n");
            handleResponse(getResponse());
        }
    }

    private void handleResponse(Response response) {
        System.out.println(response.message());

        switch (response.code().trim()) {
            case "221":
                System.exit(0);
            case "530":
                System.exit(1);
        }
    }

}
