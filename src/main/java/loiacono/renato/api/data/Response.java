package loiacono.renato.api.data;

/**
 * Creando un record si crea una classe (che però non può essere modificata) e con alcuni metodi già creati.
 * I metodi sono equals(), hashCode(), toString() e i metodi getter per ogni campo.
 * <p>
 * Metodo getter vuol dire che le proprietà sono private ma accessibili tramite il metodo nomeProprietà().
 * @param code
 * @param message
 */
public record Response(FTPResponseCode code, String message) {
    /**
     * Questa classe conterrà gli enumeratori per i codici più comuni delle risposte FTP. <br>
     * Verra usata in handleResponse() per gestire le risposte più comuni.
     * <p>
     * OVVIAMENTE ha cucinato ChatGPT.  <br>
     * La maggior parte di questi non ha utilizzo.
     */
    public enum FTPResponseCode {
        RESTART_MARKER("110"),
        SERVICE_READY_SOON("120"),
        DATA_CONNECTION_ALREADY_OPEN("125"),
        FILE_STATUS_OK("150"),
        COMMAND_OK("200"),
        COMMAND_NOT_IMPLEMENTED("202"),
        SYSTEM_STATUS("211"),
        DIRECTORY_STATUS("212"),
        FILE_STATUS("213"),
        HELP_MESSAGE("214"),
        NAME_SYSTEM_TYPE("215"),
        SERVICE_READY("220"),
        SERVICE_CLOSING("221"),
        DATA_CONNECTION_OPEN("225"),
        CLOSING_DATA_CONNECTION("226"),
        ENTERING_PASSIVE_MODE("227"),
        ENTERING_LONG_PASSIVE_MODE("228"),
        ENTERING_EXTENDED_PASSIVE_MODE("229"),
        USER_LOGGED_IN("230"),
        USER_LOGGED_IN_SECURE("232"),
        SECURITY_MECHANISM_OK("234"),
        SECURITY_DATA_OK("235"),
        FILE_ACTION_OK("250"),
        USERNAME_OK("331"),
        NEED_ACCOUNT_FOR_LOGIN("332"),
        SECURITY_DATA_NEEDED("334"),
        CHALLENGE_RESPONSE_OK("336"),
        SERVICE_CLOSING_CONTROL("421"),
        CANNOT_OPEN_DATA_CONNECTION("425"),
        CONNECTION_CLOSED("426"),
        INVALID_USERNAME_PASSWORD("430"),
        RESOURCE_UNAVAILABLE("431"),
        HOST_UNAVAILABLE("434"),
        FILE_ACTION_NOT_TAKEN("450"),
        ACTION_ABORTED("451"),
        INSUFFICIENT_STORAGE("452"),
        SYNTAX_ERROR("500"),
        SYNTAX_ERROR_IN_PARAMETERS("501"),
        COMMAND_NOT_IMPLEMENTED_FOR_PARAMETER("504"),
        BAD_SEQUENCE("503"),
        NOT_LOGGED_IN("530"),
        NEED_ACCOUNT_FOR_STORING_FILES("532"),
        PROTECTION_DENIED("533"),
        POLICY_DENIED("534"),
        SECURITY_CHECK_FAILED("535"),
        DATA_PROTECTION_NOT_SUPPORTED("536"),
        COMMAND_PROTECTION_NOT_SUPPORTED("537"),
        FILE_UNAVAILABLE("550"),
        UNKNOWN_PAGE_TYPE("551"),
        EXCEEDED_STORAGE_ALLOCATION("552"),
        FILE_NAME_NOT_ALLOWED("553"),
        INTEGRITY_PROTECTED_REPLY("631"),
        CONFIDENTIALITY_INTEGRITY_PROTECTED_REPLY("632"),
        CONFIDENTIALITY_PROTECTED_REPLY("633"),
        UNKNOWN("");

        private final String code;

        FTPResponseCode(String code) {
            this.code = code;
        }

        public static FTPResponseCode fromCode(String code) {
            for (FTPResponseCode ftpCode : FTPResponseCode.values()) {
                if (ftpCode.code.equals(code)) {
                    return ftpCode;
                }
            }
            return UNKNOWN;
        }

        public String getCode() {
            return code;
        }
    }


    public Response(String code, String message) {
        this(FTPResponseCode.fromCode(code), message);
    }
}