package loiacono.renato.cli;

import loiacono.renato.StringsHandler;
import loiacono.renato.api.FTPClient;
import loiacono.renato.api.data.Response;

import java.io.Console;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;

public class FTPClientCLI {
    FTPClient client;
    Scanner scanner = new Scanner(System.in);
    StringsHandler stringsHandler = new StringsHandler(Locale.getDefault());

    String currentLocalDirectory = System.getProperty("user.dir");

    public FTPClientCLI(
        String host,
        int port
    ) {
        try {
            client = new FTPClient(host, port);

            System.out.println(stringsHandler.getString("connected_to", host, port));

            handleResponse(client.getResponse());

            System.out.println(stringsHandler.getString("local_help"));

            shell();
        } catch (IOException e) {
            System.err.println(stringsHandler.getString("connection_error"));
            e.printStackTrace();
        }
    }

    private void shell() {
        while (true) {
            System.out.print("ftp> ");
            String command = scanner.nextLine();
            if (command.isEmpty()) continue;

            String[] args = command.split(" ");

            // Comandi custom del client
            switch (args[0].toLowerCase()) {
                case "!help":
                    System.out.println(stringsHandler.getString("local_help"));
                    continue;
                case "!list":
                    list();
                    continue;
                case "!nlst":
                    nlst();
                    continue;
                case "mv":
                    // mv <remote_file> <new_remote_file>
                    if (args.length < 3) {
                        System.out.println(stringsHandler.getString("missing_arguments"));
                        continue;
                    }

                    handleResponse(client.sendCommand("RNFR " + args[1]));
                    handleResponse(client.sendCommand("RNTO " + args[2]));
                    continue;
                case "download":
                    // download <remote_file> <local_file>
                    if (args.length < 3) {
                        System.out.println(stringsHandler.getString("missing_arguments"));
                        continue;
                    }

                    try {
                        String local_file = args[2];
                        if (!local_file.startsWith("/"))
                            local_file = currentLocalDirectory + "/" + local_file;

                        retr(args[1], local_file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    continue;
                case "upload":
                    // upload <local_file> <remote_file>
                    if (args.length < 2) {
                        System.out.println(stringsHandler.getString("missing_arguments"));
                        continue;
                    }
                    String remote_file = args[1];
                    if (args.length == 3)
                        remote_file = args[2];

                    String local_file = args[1];
                    if (!local_file.startsWith("/"))
                        local_file = currentLocalDirectory + "/" + local_file;

                    try {
                        stor(local_file, remote_file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    continue;
                case "!cd":
                    // LOCAL
                    // cd <directory>
                    if (args.length < 2) {
                        System.out.println(stringsHandler.getString("missing_arguments"));
                        continue;
                    }

                    if (args[1].startsWith("/")) {
                        currentLocalDirectory = args[1];
                    } else if (args[1].equals("..")) {
                        currentLocalDirectory = currentLocalDirectory.substring(0, currentLocalDirectory.lastIndexOf("/"));
                    } else {
                        currentLocalDirectory += "/" + args[1];
                    }

                    System.out.println("INFO: Changed directory to " + currentLocalDirectory);
                    continue;
                case "!pwd":
                    System.out.println(currentLocalDirectory);
                    continue;
                case "!ls":
                    System.out.println(Arrays.toString(Objects.requireNonNull(new java.io.File(currentLocalDirectory).list())));
                    continue;
            }

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
                System.out.println(stringsHandler.getString("empty_username"));
            }
        } while (user.isEmpty());

        if (handleResponse(client.sendCommand("USER " + user)) == Response.FTPResponseCode.USERNAME_OK) {
            Console cons = System.console();
            Response res;
            if (cons == null) {
                System.out.println(stringsHandler.getString("password_warning"));
                System.out.print("PASS> ");
                String password = scanner.nextLine();
                res = client.sendCommand("PASS " + password);
            } else {
                System.out.print("PASS> ");
                char[] password = cons.readPassword();
                res = client.sendCommand("PASS " + new String(password));
            }
            handleResponse(res);
        } else {
            // 530, 332, 421, 500, 501
            System.out.println(stringsHandler.getString("invalid_username"));
        }
    }

    /*
    Questo metodo fa:
    controlla che la data connection sia aperta (altrimenti la apre),
    invia il comando al server,
    attende risposta dal server finchè il server non chiude la connessione dati, (usando client.getDataConnection())
    chiude la connessione dati, salva tutto
     */
    private void list() {
        try {
            if (!client.isDataConnectionOpen()) {
                handleResponse(client.sendCommand("PASV"));
            }
            Response response = client.sendCommand("LIST");
            if (response.code() == Response.FTPResponseCode.FILE_STATUS_OK) {
                System.out.println(new String(client.readDataConnection(), StandardCharsets.UTF_8));
                // read 1 line from control connection to get the 226 response and close the data connection
                handleResponse(client.getResponse());

                System.out.println("INFO: Data connection closed.");
            } else handleResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void nlst() {
        try {
            if (!client.isDataConnectionOpen()) {
                handleResponse(client.sendCommand("PASV"));
            }
            Response response = client.sendCommand("NLST");
            if (response.code() == Response.FTPResponseCode.FILE_STATUS_OK) {
                System.out.println(new String(client.readDataConnection(), StandardCharsets.UTF_8));
                // read 1 line from control connection to get the 226 response and close the data connection
                handleResponse(client.getResponse());

                System.out.println("INFO: Data connection closed.");
            } else handleResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void retr(String remote_file, String local_file) throws IOException {
        System.out.println(client.isDataConnectionOpen());
        if (!client.isDataConnectionOpen()) {
            handleResponse(client.sendCommand("PASV"));
        }
        Response response = client.sendCommand("RETR " + remote_file);
        if (response.code() == Response.FTPResponseCode.FILE_STATUS_OK) {
            System.out.println("INFO: Downloading file...");
            byte[] data = client.readDataConnection();
            handleResponse(client.getResponse());

            System.out.println("INFO: Data connection closed.");
            System.out.println("INFO: Saving file...");
            client.saveFile(local_file, data);
            System.out.println("INFO: File saved.");
        } else {
            System.out.println("ERROR: File not found.");
            System.out.println(response);
        }
    }

    private void stor(String local_file, String remote_file) throws IOException {
        if (!client.isDataConnectionOpen()) {
            handleResponse(client.sendCommand("PASV"));
        }
        Response response = client.sendCommand("STOR " + remote_file);
        if (response.code() == Response.FTPResponseCode.FILE_STATUS_OK) {
            System.out.println("INFO: Uploading file...");
            byte[] data = client.readLocalFile(local_file);
            client.sendOnDataConnection(data);
            handleResponse(client.getResponse());
            client.closeDataConnection();

            System.out.println("INFO: Data connection closed.");
        } else {
            System.out.println("ERROR: File not found.");
            System.out.println(response);
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
            // 1XX
            // Questo serve per il supporto del comando PASV
            case FILE_STATUS_OK:
                try {
                    System.out.println(new String(client.readDataConnection(), StandardCharsets.UTF_8));
                    // read 1 line from control connection to get the 226 response and close the data connection
                    handleResponse(client.getResponse());
                    client.closeDataConnection();
                    System.out.println("INFO: Data connection closed.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            // 2XX
            case SERVICE_READY, INVALID_USERNAME_PASSWORD, NOT_LOGGED_IN:
                login();
                break;
            case SERVICE_CLOSING:
                System.out.println(stringsHandler.getString("service_closing"));
                System.exit(0);
                break;
            case CLOSING_DATA_CONNECTION:
                try {
                    client.closeDataConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
            case USER_LOGGED_IN:
                System.out.println(stringsHandler.getString("user_logged_in"));
                break;
            // 3XX
            case NEED_ACCOUNT_FOR_LOGIN:
                System.out.println(stringsHandler.getString("account_not_supported"));
                System.exit(1);
                break;
            // 4XX
            case SERVICE_CLOSING_CONTROL:
                System.out.println(stringsHandler.getString("closing_control_connection"));
                System.exit(0);
                break;
            case CONNECTION_CLOSED:
                System.out.println(stringsHandler.getString("server_closed_connection"));
                System.exit(1);
                break;
            case BAD_SEQUENCE:
                System.out.println(stringsHandler.getString("bad_sequence"));
                break;
            // Other
            case USERNAME_OK, HELP_MESSAGE, SYNTAX_ERROR, SYNTAX_ERROR_IN_PARAMETERS, UNKNOWN, COMMAND_NOT_IMPLEMENTED:
                // Lista di risposte che non necessitano di azioni particolari
                // Verranno gestite dal chiamante
                return response.code();
            default:
                System.out.println("Unhandled response code: " + response.code());
                return response.code();
        }
        return null;
    }
}
