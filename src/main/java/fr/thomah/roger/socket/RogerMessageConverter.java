package fr.thomah.roger.socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.thomah.roger.Command;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

import java.nio.charset.StandardCharsets;

@Component
public class RogerMessageConverter extends AbstractMessageConverter {

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public RogerMessageConverter() {
        super(new MimeType("application", "json", StandardCharsets.UTF_8));
    }

    @Override
    protected boolean supports(Class<?> aClass) {
        return true;
    }

    @Nullable
    protected Object convertFromInternal(Message<?> message, Class<?> targetClass, @Nullable Object conversionHint) {
        byte[] payload = (byte[]) message.getPayload();
        String jsonStr = new String(payload);
        return gson.fromJson(jsonStr, Command.class);
    }
}
