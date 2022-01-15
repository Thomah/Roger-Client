package fr.thomah.roger.karotz;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.thomah.roger.http.HttpClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Service
public class KarotzService {

    @Autowired
    private HttpClientService httpClientService;

    @Value("${fr.thomah.roger.karotz.url}")
    private String karotzBaseUrl;

    @Value("${fr.thomah.roger.karotz.testmode}")
    private boolean testmode;

    private final HttpRequest.Builder httpBuilder;

    private int earsPosition = 0;

    public KarotzService() {
        httpBuilder = HttpRequest.newBuilder();
    }

    public HttpResponse<String> status() {
        String url;
        if (!testmode) {
            url = karotzBaseUrl + "/cgi-bin/status";
        } else {
            url = "http://www.mocky.io/v2/5d126f9631000018c208d35c";
        }
        HttpRequest request = httpBuilder
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> response = null;
        try {
            log.info("GET : {}", url);
            response = httpClientService.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            log.error("Unable to get Karotz status");
        }
        return response;
    }

    public boolean statusValidation(HttpResponse<String> response) {
        if(response == null)
            return false;
        boolean success = response.statusCode() == 200;
        if (!success)
            return false;
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(response.body(), JsonObject.class);
        return json != null && json.get("version") != null;
    }

    void sound(String fileUrl) {
        try {
            String url;
            if (!testmode) {
                url = karotzBaseUrl + "/cgi-bin/sound?url=" + fileUrl.replaceAll(" ", "%20");
            } else {
                url = "http://www.mocky.io/v2/5d1279923100001ec508d38e";
            }
            HttpRequest request = httpBuilder
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .build();
            ears("40");
            log.info("GET : {}", url);
            httpClientService.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    void ears(String move) {
        int ears = Integer.parseInt(move);
        earsPosition+= ears;
        try {
            String url;
            if (!testmode) {
                url = karotzBaseUrl + "/cgi-bin/ears?left=" + earsPosition + "&right=" + earsPosition + "&noreset=1";
            } else {
                url = "http://www.mocky.io/v2/5d19ef0b2f0000a148fd733f";
            }
            HttpRequest request = httpBuilder
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .build();
            log.info("GET : {}", url);
            httpClientService.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
