package fr.thomah.roger.clients;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.TimerTask;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;

import fr.thomah.roger.*;
import org.springframework.stereotype.Component;

@Component
public class SlackClient extends TimerTask {

    private HttpClient client;
    private HttpRequest.Builder builder;
    private String channelId;
    private Db db;
    private KarotzClient karotzClient;
    private List<Correspondance> listCorrespondances = new ArrayList<Correspondance>();
    private int probaMin = 0;
    private final int probaMax = 1800;
    boolean etatRadio = false;

    public void init(HttpClient client, HttpRequest.Builder builder) {
        this.client = client;
        this.builder = builder;
    }

    public void connect() {
        try {

            HttpRequest request = builder
                    .uri(URI.create(BackClient.ROGER_BACK_URL + "/api/files"))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonArray bodyJson = new JsonParser().parse(response.body()).getAsJsonArray();
            bodyJson.forEach(fileDataJson -> {
                JsonObject fileData = fileDataJson.getAsJsonObject();
                listCorrespondances.add(new Correspondance(fileData.get("matches").getAsString(), fileData.get("fileName").getAsString()));
            });

            request = builder
                    .uri(URI.create("https://slack.com/api/auth.test?token=" + RogerApplication.SLACK_TOKEN))
                    .build();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(System.out::println)
                    .join();
            request = builder
                    .uri(URI.create("https://slack.com/api/channels.join?name=nabz&token=" + RogerApplication.SLACK_TOKEN))
                    .build();
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonObject = new JsonParser().parse(response.body()).getAsJsonObject();
            if (jsonObject.get("ok").getAsBoolean()) {
                channelId = jsonObject.getAsJsonObject("channel").get("id").getAsString();
                karotzClient.TTS("Bonjour. Je suis Roger et je suis là pour vous aider.",1);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public HttpResponse<String> ping() {
        HttpRequest request = builder
                .uri(URI.create("https://slack.com/api/api.test?token=" + RogerApplication.SLACK_TOKEN))
                .build();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return response;
    }

    public boolean pingValidation(HttpResponse<String> response) {
        boolean success = response.statusCode() == 200;
        if (!success) {
            return false;
        }
        JsonObject jsonObject = new JsonParser().parse(response.body()).getAsJsonObject();
        return jsonObject != null && jsonObject.get("ok") != null && jsonObject.get("ok").getAsBoolean();
    }

    @Override
    public void run() {
        try {
            HttpRequest request = builder
                    .uri(URI.create("https://slack.com/api/channels.history?channel=" + channelId + "&token=" + RogerApplication.SLACK_TOKEN))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonObject = new JsonParser().parse(response.body()).getAsJsonObject();
            List<Message> messages = db.syncMessages(jsonObject.getAsJsonArray("messages"));
            int nbMessages = messages.size();

            // Si messages à traiter
            for (Message message : messages) {

                String textSlack = message.getText();

                if (textSlack.startsWith("ears:")) {
                    karotzClient.ears(textSlack.replace("ears:", ""));
                }
                if (textSlack.startsWith("radio:")) {
                    if (textSlack.contains("nrj")) {
                        if (!etatRadio) {
                            String url = "http://185.52.127.155/fr/30001/mp3_128.mp3?origine=fluxradios";
                            karotzClient.radio(url);
                            etatRadio = true;
                        } else if (etatRadio) {
                            karotzClient.stopSound();
                            etatRadio = false;
                        }

                    }
                    if (textSlack.contains("rtl2")) {
                        if (!etatRadio) {
                            String url = "http://streaming.radio.rtl2.fr/rtl2-1-44-96";
                            karotzClient.radio(url);
                            etatRadio = true;
                        } else if (etatRadio) {
                            karotzClient.stopSound();
                            etatRadio = false;
                        }

                    }

                    if (textSlack.contains("metal")) {
                        if (!etatRadio) {
                            String url = "";
                            karotzClient.radio(url);
                            etatRadio = true;
                        } else if (etatRadio) {
                            karotzClient.stopSound();
                            etatRadio = false;
                        }

                    }
                }

                if (textSlack.startsWith("dire:")) {
                    karotzClient.TTS(textSlack.replace("dire:", ""), 1);
                }
                if (textSlack.startsWith("say:")) {
                    karotzClient.TTS(textSlack.replace("say:", ""), 5);
                }
                if (textSlack.startsWith("habla:")) {
                    karotzClient.TTS(textSlack.replace("habla:", ""), 13);
                } else {
                    listCorrespondances.stream()
                            .filter(correspondance -> textSlack.contains(correspondance.getKey()))
                            .findAny().ifPresent(c -> karotzClient.sound(c.getFileName()));
                }
            }

            // Si aucun message, faible probabilité de dire quelque chose
            if (nbMessages == 0) {
                int random = generateRandomBetween(probaMin, probaMax);
                System.out.println("min = " + probaMin);
                System.out.println("random = " + random);
                if (random == probaMax) {
                    probaMin = 0;
                    int numSound = generateRandomBetween(0, listCorrespondances.size());
                    karotzClient.sound(listCorrespondances.get(numSound).getFileName());
                } else {
                    probaMin++;
                }
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    int generateRandomBetween(int min, int max) {
        return min + (int) (Math.random() * ((max - min) + 1));
    }

    public void setDb(Db db) {
        this.db = db;
    }

    public void setKarotzClient(KarotzClient karotzClient) {
        this.karotzClient = karotzClient;
    }
}
