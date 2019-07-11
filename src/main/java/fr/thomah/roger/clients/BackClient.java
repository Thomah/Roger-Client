package fr.thomah.roger.clients;

import fr.thomah.roger.Application;
import fr.thomah.roger.RogerApplication;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class BackClient {

    private static final String ROGER_BACK_URL = System.getenv("ROGER_BACK_URL");

    private HttpClient client;
    private HttpRequest.Builder builder;

    public void init(HttpClient client, HttpRequest.Builder builder) {
        this.client = client;
        this.builder = builder;
    }

    public HttpResponse<String> health() {
        HttpRequest request = builder
                .uri(URI.create(ROGER_BACK_URL + "/api/health"))
                .build();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return response;
    }

    public boolean healthValidation(HttpResponse<String> response) {
        return response.statusCode() == 200;
    }

}
