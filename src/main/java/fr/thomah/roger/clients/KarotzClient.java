package fr.thomah.roger.clients;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.thomah.roger.entities.Command;
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
    private Integer leftEarPosition = 0;
    private Integer rightEarPosition = 0;

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

    public void send(Command command) {
        String endpointAndParams = command.toString();
        System.out.println("Sending : " + endpointAndParams);
        if(command.endpoint.equals("/ears")) {
            incEars(command);
            endpointAndParams = command.toString();
        } else if(command.endpoint.equals("/sound")) {
            endpointAndParams = endpointAndParams.replaceAll("<THIS_COMPUTER_URL>", RogerApplication.THIS_COMPUTER_URL);
        }
        try {
            String url;
            if (RogerApplication.KAROTZ_AVAILABLE) {
                url = RogerApplication.BASE_URL + "/cgi-bin" + endpointAndParams;
            } else {
                url = "http://www.mocky.io/v2/5d19ee532f00000e00fd7339";
            }
            HttpRequest request = builder
                    .uri(URI.create(url)).build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Sent");
    }

    private void incEars(Command command) {
        leftEarPosition += Integer.valueOf(command.params.get("left"));
        command.params.put("left", leftEarPosition.toString());
        rightEarPosition += Integer.valueOf(command.params.get("right"));
        command.params.put("right", rightEarPosition.toString());
    }
}
