package loiacono.renato.cli;

import loiacono.renato.api.FTPClient;
import loiacono.renato.api.data.Response;

import java.io.Console;
import java.io.IOException;
import java.util.Scanner;

public class FTPClientCLI {
    FTPClient client;
    Scanner scanner = new Scanner(System.in);

    public FTPClientCLI(
        String host,
        int port
    ) {
        try {
            client = new FTPClient(host, port);

            System.out.println("Connected to " + host + ":" + port);

            handleResponse(client.getResponse());

            shell();
        } catch (IOException e) {
            System.err.println("There was an error connecting to the server.");
            e.printStackTrace();
        }
    }

    private void shell() {
        while (true) {
            System.out.print("ftp> ");
            String command = scanner.nextLine();
            if (command.isEmpty()) continue;
            handleResponse(client.sendCommand(command));
            if (command.equalsIgnoreCase("quit")) {
                break;
            }
        }
    }

    private void login() {
        String user;
        do {
            System.out.print("USER> ");
            user = scanner.nextLine();
            if (user.isEmpty()) {
                System.out.println("INFO: Username cannot be empty.");
            }
        } while (user.isEmpty());
        Response.FTPResponseCode responseCode = handleResponse(client.sendCommand("USER " + user));

        switch (responseCode) {
            case USERNAME_OK -> {
                Console cons = System.console();
                Response res;
                if (cons == null) {
                    System.out.println("ATTENTION ---  No console found: the password will be shown in clear text  --- ATTENTION");
                    System.out.print("PASS> ");
                    String password = scanner.nextLine();
                    res = client.sendCommand("PASS " + password);
                } else {
                    System.out.print("PASS> ");
                    char[] password = cons.readPassword();
                    res = client.sendCommand("PASS " + new String(password));
                }
                handleResponse(res);
            }
            default -> System.out.println("Invalid username.");
        }

    }

    /**
     * Allora, questo metodo è un po' strano. <br>
     * In pratica, qui gestisco le risposte più comuni (chiusura connessione, login, ecc). <br>
     * Quelle più specifiche le faccio gestire al metodo chiamante. Quindi ritorno una stringa che indica cosa fare.
     */
    private Response.FTPResponseCode handleResponse(Response response) {
        System.out.println(response.message());

        switch (response.code()) {
            case FILE_STATUS_OK:
                try {
                    System.out.println(client.readData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case NOT_LOGGED_IN:
                System.out.println("ERROR: Not logged in.");
            case SERVICE_READY, INVALID_USERNAME_PASSWORD:
                login();
                break;
            case ENTERING_PASSIVE_MODE:
                // https://datatracker.ietf.org/doc/html/rfc959#section-4 .1.2
                String[] info = response.message().substring(response.message().indexOf("(") + 1, response.message().indexOf(")")).split(",");
                String ip = info[0] + "." + info[1] + "." + info[2] + "." + info[3];
                int port = Integer.parseInt(info[4]) * 256 + Integer.parseInt(info[5]);
                System.out.println("INFO: Opening data connection to " + ip + ":" + port);
                try {
                    client.openDataConnection(ip, port);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            case CLOSING_DATA_CONNECTION:
                try {
                    client.closeDataConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case USER_LOGGED_IN:
                System.out.println("INFO: User logged in.");
                break;
            case SERVICE_CLOSING_CONTROL:
                System.out.println("ERROR: The server is closing the control connection. Exiting.");
                System.exit(0);
                break;
            case CONNECTION_CLOSED:
                System.out.println("ERROR: The server has closed the connection.");
                System.exit(0);
                break;
            case BAD_SEQUENCE:
                System.out.println("ERROR: Bad sequence of commands.");
                break;
            case USERNAME_OK, HELP_MESSAGE, SYNTAX_ERROR:
                // Lista di risposte che non necessitano di azioni particolari
                break;
            default:
                System.out.println("Unhandled response code: " + response.code());
                return response.code();
        }
        return null;
    }
}
