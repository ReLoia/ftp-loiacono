package loiacono.renato.cli;

import loiacono.renato.StringsHandler;
import loiacono.renato.api.FTPClient;
import loiacono.renato.api.data.Response;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;

import static loiacono.renato.Utils.log;

public class FTPClientCLI {
    private FTPClient client;
    private final Scanner scanner = new Scanner(System.in);
    private final StringsHandler stringsHandler = new StringsHandler(Locale.getDefault());

    private String currentLocalDirectory = System.getProperty("user.dir");

    public FTPClientCLI(
        String host,
        int port
    ) {
        try {
            client = new FTPClient(host, port);

            log(stringsHandler.getString("cli_info_connected_control_connection", host, port));

            handleResponse(client.getResponse());

            log(stringsHandler.getString("cli_local_help"));

            shell();
        } catch (IOException e) {
            System.err.println(stringsHandler.getString("cli_error_connecting"));
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
                    log(stringsHandler.getString("cli_local_help"));
                    continue;
                case "!list":
                    list();
                    continue;
                case "!nlst":
                    nlst();
                    continue;
                case "mv": {
                    // mv <remote_file> <new_remote_file>
                    if (args.length < 3) {
                        log(stringsHandler.getString("cli_missing_arguments"));
                        continue;
                    }

                    handleResponse(client.sendCommand("RNFR " + args[1]));
                    handleResponse(client.sendCommand("RNTO " + args[2]));
                    continue;
                }
                case "download": {
                    // download <remote_file> <local_file>
                    if (args.length < 2) {
                        log(stringsHandler.getString("cli_missing_arguments"));
                        continue;
                    }
                    String local_file = args[1];
                    if (args.length == 3)
                        local_file = args[2];

                    try {
                        if (!local_file.startsWith("/"))
                            local_file = currentLocalDirectory + "/" + local_file;

                        retr(args[1], local_file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    continue;
                }
                case "upload": {
                    // upload <local_file> <remote_file>
                    if (args.length < 2) {
                        log(stringsHandler.getString("cli_missing_arguments"));
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
                }
                case "!cd": {
                    // LOCAL
                    // cd <directory>
                    if (args.length < 2) {
                        log(stringsHandler.getString("cli_missing_arguments"));
                        continue;
                    }

                    if (args[1].startsWith("/")) {
                        currentLocalDirectory = args[1];
                    } else if (args[1].equals("..")) {
                        currentLocalDirectory = currentLocalDirectory.substring(0, currentLocalDirectory.lastIndexOf("/"));
                    } else {
                        currentLocalDirectory += "/" + args[1];
                    }

                    log(stringsHandler.getString("cli_info_dir_changed", currentLocalDirectory));
                    continue;
                }
                case "!pwd":
                    System.out.println(currentLocalDirectory);
                    continue;
                case "!ls":
                    System.out.println(Arrays.toString(Objects.requireNonNull(new File(currentLocalDirectory).list())));
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
                log(stringsHandler.getString("cli_warning_empty_username"));
            }
        } while (user.isEmpty());

        if (handleResponse(client.sendCommand("USER " + user)) == Response.FTPResponseCode.USERNAME_OK) {
            Console cons = System.console();
            Response res;
            if (cons == null) {
                log(stringsHandler.getString("cli_password_warning"));
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
            log(stringsHandler.getString("invalid_username"));
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

                log(stringsHandler.getString("cli_info_closed_data_connection"));
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

                log(stringsHandler.getString("cli_info_closed_data_connection"));
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
            log(stringsHandler.getString("cli_info_downloading"));
            byte[] data = client.readDataConnection();
            handleResponse(client.getResponse());

            log(stringsHandler.getString("cli_info_closed_data_connection"));
            log(stringsHandler.getString("cli_info_file_saving"));
            client.saveFile(local_file, data);
            log(stringsHandler.getString("cli_info_file_saved"));
        } else {
            log(stringsHandler.getString("cli_error_file_not_found"));
            System.out.println(response);
        }
    }

    private void stor(String local_file, String remote_file) throws IOException {
        if (!client.isDataConnectionOpen()) {
            handleResponse(client.sendCommand("PASV"));
        }
        Response response = client.sendCommand("STOR " + remote_file);
        if (response.code() == Response.FTPResponseCode.FILE_STATUS_OK) {
            log(stringsHandler.getString("cli_info_uploading"));
            File file = new File(local_file);
            if (!file.exists()) {
                log(stringsHandler.getString("cli_error_file_does_not_exist"));
                return;
            }
            if (file.isDirectory()) {
                log(stringsHandler.getString("cli_error_cant_send_dir"));
                return;
            }
            byte[] data = client.readLocalFile(local_file);
            client.sendOnDataConnection(data);
            handleResponse(client.getResponse());
            client.closeDataConnection();

            log(stringsHandler.getString("cli_info_closed_data_connection"));
        } else {
            log(stringsHandler.getString("cli_error_file_not_found"));
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
                    log(stringsHandler.getString("cli_info_closed_data_connection"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            // 2XX
            case SERVICE_READY, INVALID_USERNAME_PASSWORD, NOT_LOGGED_IN:
                login();
                break;
            case SERVICE_CLOSING:
                log(stringsHandler.getString("quitting"));
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
                log(stringsHandler.getString("cli_info_opening_data_connection", ip, port));
                try {
                    client.openDataConnection(ip, port);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            case USER_LOGGED_IN:
                log(stringsHandler.getString("cli_info_user_logged_in"));
                break;
            // 3XX
            case NEED_ACCOUNT_FOR_LOGIN:
                log(stringsHandler.getString("cli_info_account_not_supported"));
                System.exit(1);
                break;
            // 4XX
            case SERVICE_CLOSING_CONTROL:
                log(stringsHandler.getString("cli_closing_control_connection"));
                System.exit(0);
                break;
            case CONNECTION_CLOSED:
                log(stringsHandler.getString("cli_closed_connection"));
                System.exit(1);
                break;
            case BAD_SEQUENCE:
                log(stringsHandler.getString("cli_error_bad_sequence"));
                break;
            // Other
            case USERNAME_OK, HELP_MESSAGE, SYNTAX_ERROR, SYNTAX_ERROR_IN_PARAMETERS, UNKNOWN, COMMAND_NOT_IMPLEMENTED:
                // Lista di risposte che non necessitano di azioni particolari
                // Verranno gestite dal chiamante
                return response.code();
            default:
                log(stringsHandler.getString("cli_error_unhandled_response_code", response.code()));
                return response.code();
        }
        return null;
    }
}
