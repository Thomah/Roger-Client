package fr.thomah.roger;

import java.util.HashMap;
import java.util.Map;

public class Command {

    public String endpoint;

    public Map<String, String> params = new HashMap<>();

    public Command(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(endpoint);
        String key = null, value;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (key == null) {
                stringBuilder.append("?");
            } else {
                stringBuilder.append("&");
            }
            key = entry.getKey();
            value = entry.getValue();
            stringBuilder.append(key);
            stringBuilder.append("=");
            stringBuilder.append(value.replaceAll(" ", "%20"));
        }
        return stringBuilder.toString();
    }

}
