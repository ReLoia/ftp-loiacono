package loiacono.renato;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class StringsHandler {
    private ResourceBundle strings;

    public StringsHandler(Locale locale) {
        try {
            strings = ResourceBundle.getBundle("strings", locale);
        } catch (MissingResourceException e) {
            strings = ResourceBundle.getBundle("strings", Locale.ENGLISH);
        }
    }

    public String getString(String key) {
        return strings.getString(key);
    }

    // https://stackoverflow.com/questions/17837117/java-sending-multiple-parameters-to-method
    public String getString(String key, Object... args) {
        return String.format(strings.getString(key), args);
    }
}
