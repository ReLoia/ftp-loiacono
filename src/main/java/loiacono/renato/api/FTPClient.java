package loiacono.renato.api;

import loiacono.renato.api.data.Response;

import java.io.*;
import java.net.Socket;

/**
 * Client FTP secondo il <a href="https://datatracker.ietf.org/doc/html/rfc959">RFC 959</a>.<br>
 *
 * ATTENZIONE: Il client non segue alla lettera le specifiche del RFC 959, ma si limita a implementare le funzionalità di base.
 *             Inoltre, il client non è scritto in maniera ottimale.
 *
 */
public class FTPClient {
    private Socket controlSocket;
    private Socket dataSocket;

    /*
     * Allora, per qualche motivo ObjectOutput/InputStream non funzionano con i socket FTP.
     * Quindi, per inviare i dati, si usa un OutputStream e per ricevere i dati, si usa un BufferedReader.
     * Il buffered reader si comporta in modo simile a un Scanner.
     *
     * L'errore che mi da è "java.io.StreamCorruptedException: invalid stream header: 32323020"
     */

    // Da qui si inviano i comandi al server
    private OutputStream controlWriter = null;
    // Da qui si ricevono le risposte del server
    private BufferedReader controlReader = null;

    // Da qui si inviano i dati al server
    private OutputStream dataWriter = null;
    // Da qui si ricevono i dati dal server
    private BufferedInputStream dataReader = null;

    // Ho spostato la gestione degli errori alle classi che usano il Client
    public FTPClient(String host, int port) throws IOException {
        controlSocket = new Socket(host, port);
        controlWriter = controlSocket.getOutputStream();
        controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
    }

    public void close() throws IOException {
        if (controlWriter != null) {
            controlWriter.close();
        }
        if (controlReader != null) {
            controlReader.close();
        }
        if (controlSocket != null) {
            controlSocket.close();
            controlSocket = null;
        }
    }

    /**
     * Questo metodo ottiene la risposta del server dopo un comando. <br>
     * Controlla se la risposta è multi-linea e la concatena in un'unica stringa.
     */
    public Response getResponse() {
        String responseCode = null;
        String responseMessage = null;

        try {
            responseMessage = controlReader.readLine();
            if (responseMessage == null) {
                throw new RuntimeException("Server closed the connection.");
            }
            responseCode = responseMessage.substring(0, 3);

            if (responseMessage.charAt(3) == '-') {
                StringBuilder sb = new StringBuilder();
                sb.append(responseMessage);
                do {
                    responseMessage = controlReader.readLine();
                    sb.append("\n").append(responseMessage);
                } while (!responseMessage.startsWith(responseCode + " "));
                responseMessage = sb.toString();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new Response(responseCode, responseMessage);
    }

    /**
     * Questo metodo invia un comando al server senza attendere la risposta.
     * Utile per alcune comunicazioni via GUI.
     */
    public void _sendCommand(String command) {
        try {
            // L'RFC 959 dice che i comandi devono essere terminati con CRLF
            controlWriter.write((command + "\r\n").getBytes());
            controlWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Response sendCommand(String command) {
        _sendCommand(command);
        return getResponse();
    }

    public void openDataConnection(String ip, int port) throws IOException {
        dataSocket = new Socket(ip, port);
        dataWriter = dataSocket.getOutputStream();
        dataReader = new BufferedInputStream(dataSocket.getInputStream());
    }

    public boolean isDataConnectionOpen() {
        return dataSocket != null && dataSocket.isConnected() && !dataSocket.isClosed();
    }

    public byte[] readDataConnection() throws IOException {
        byte[] buffer = new byte[4096]; // 4KB
        int bytesRead;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            while ((bytesRead = dataReader.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
        } finally {
            closeDataConnection();
        }

        return byteArrayOutputStream.toByteArray();
    }

    public void sendOnDataConnection(byte[] data) throws IOException {
        try {
            dataWriter.write(data);
            dataWriter.flush();
        } finally {
            closeDataConnection();
        }
    }

    public void closeDataConnection() throws IOException {
        if (dataWriter != null) dataWriter.close();
        if (dataReader != null) dataReader.close();
        if (dataSocket != null) dataSocket.close();

    }

    public void saveFile(String path, byte[] data) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(data);
        }
    }

    public byte[] readLocalFile(String path) {
        try (FileInputStream fis = new FileInputStream(path)) {
            byte[] buffer = new byte[4096]; // 4KB
            int bytesRead;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            while ((bytesRead = fis.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
