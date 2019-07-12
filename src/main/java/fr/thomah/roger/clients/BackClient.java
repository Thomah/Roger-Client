package fr.thomah.roger.clients;

import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import java.util.TimerTask;

@Component
public class BackClient extends TimerTask implements StompSessionHandler {

    public static final String ROGER_BACK_URL = System.getenv("ROGER_BACK_URL");

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

    public void connect() {
        WebSocketClient client = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        stompClient.connect("ws://roger-karotz.herokuapp.com/socket", this);
        new Scanner(System.in).nextLine(); // Don't close immediately.
    }

    @Override
    public void run() {
        health();
    }

    @Override
    public void afterConnected(StompSession stompSession, StompHeaders stompHeaders) {
        stompSession.subscribe("/command", this);
        //stompSession.send("/app/chat", getSampleMessage());
    }

    @Override
    public void handleException(StompSession stompSession, StompCommand stompCommand, StompHeaders stompHeaders, byte[] bytes, Throwable throwable) {

    }

    @Override
    public void handleTransportError(StompSession stompSession, Throwable throwable) {

    }

    @Override
    public Type getPayloadType(StompHeaders stompHeaders) {
        return null;
    }

    @Override
    public void handleFrame(StompHeaders stompHeaders, Object payload) {
        System.out.println("Received : " + payload.toString());
    }
}
