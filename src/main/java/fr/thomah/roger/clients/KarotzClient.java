package fr.thomah.roger.clients;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.thomah.roger.RogerApplication;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class KarotzClient {

    private HttpClient client;
    private HttpRequest.Builder builder;
    private int earsPosition = 0;

    public void init(HttpClient client, HttpRequest.Builder builder) {
        this.client = client;
        this.builder = builder;
    }

    public HttpResponse<String> status() {
        String url;
        if (RogerApplication.KAROTZ_AVAILABLE) {
            url = RogerApplication.BASE_URL + "/cgi-bin/status";
        } else {
            url = "http://www.mocky.io/v2/5d126f9631000018c208d35c";
        }
        HttpRequest request = builder
                .uri(URI.create(url)).build();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return response;
    }

    public boolean statusValidation(HttpResponse<String> response) {
        boolean success = response.statusCode() == 200;
        if (!success)
            return false;
        JsonObject jsonObject = new JsonParser().parse(response.body()).getAsJsonObject();
        return jsonObject != null && jsonObject.get("version") != null;
    }

    void sound(String file) {
        System.out.println("Playing : " + file);
        try {
            String url;
            if (RogerApplication.KAROTZ_AVAILABLE) {
                url = RogerApplication.BASE_URL + "/cgi-bin/sound?url=" + RogerApplication.THIS_COMPUTER_URL + "/public/music/" + file.replaceAll(" ", "%20");
            } else {
                url = "http://www.mocky.io/v2/5d1279923100001ec508d38e";
            }
            HttpRequest request = builder
                    .uri(URI.create(url)).build();
            System.out.println(URI.create(RogerApplication.BASE_URL + "/cgi-bin/sound?url=" + RogerApplication.THIS_COMPUTER_URL + "/public/music/" + file.replaceAll(" ", "%20")));
            ears("40");
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    void radio(String radio) {
        System.out.println("Playing : " + radio);
        try {
            String url;
            if (RogerApplication.KAROTZ_AVAILABLE) {
                url = RogerApplication.BASE_URL + "/cgi-bin/sound?url=" + radio.replaceAll(" ", "%20");
            } else {
                url = "http://www.mocky.io/v2/5d1279923100001ec508d38e";
            }
            HttpRequest request = builder
                    .uri(URI.create(url)).build();
            System.out.println(URI.create(RogerApplication.BASE_URL + "/cgi-bin/sound?url=" + radio.replaceAll(" ", "%20")));
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    void stopSound() {
        System.out.println("Stoping : ");
        try {
            String url;
            if (RogerApplication.KAROTZ_AVAILABLE) {
                url = RogerApplication.BASE_URL + "/cgi-bin/sound_control?cmd=quit";
            } else {
                url = "http://www.mocky.io/v2/5d19ee0f2f00004c00fd7336";
            }
            HttpRequest request = builder
                    .uri(URI.create(url)).build();
            System.out.println(URI.create(RogerApplication.BASE_URL + "/cgi-bin/sound_control?cmd=quit"));
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    void TTS(String text, int langue) {
        System.out.println("Saying : " + text);
        try {
            String url;
            if (RogerApplication.KAROTZ_AVAILABLE) {
                url = RogerApplication.BASE_URL + "/cgi-bin/tts?voice="+langue+"&text=" +text.replaceAll(" ", "%20") +"&nocache=0";
            } else {
                url = "http://www.mocky.io/v2/5d19ee532f00000e00fd7339";
            }
            HttpRequest request = builder
                    .uri(URI.create(url)).build();
            System.out.println(URI.create(RogerApplication.BASE_URL + "/cgi-bin/tts?voice=" + langue + "&text=" + text.replaceAll(" ", "%20") + "&nocache=0"));
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    void ears(String move) {
        int ears = Integer.parseInt(move);
        earsPosition+= ears;
        System.out.println("Moving : " + move);
        try {
            String url;
            if (RogerApplication.KAROTZ_AVAILABLE) {
                url = RogerApplication.BASE_URL + "/cgi-bin/ears?left=" + earsPosition + "&right=" + earsPosition + "&noreset=1";
            } else {
                url = "http://www.mocky.io/v2/5d19ef0b2f0000a148fd733f";
            }
            HttpRequest request = builder
                    .uri(URI.create(url)).build();
            System.out.println(URI.create(RogerApplication.BASE_URL + "/cgi-bin/ears?left=" + move + "&right=" + move + "&noreset=1"));
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
