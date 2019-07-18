package fr.thomah.roger.clients;

import fr.thomah.roger.socket.RogerMessageConverter;
import fr.thomah.roger.socket.RogerSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

@Component
public class BackClient extends TimerTask {

    @Autowired
    private RogerSocketHandler socketHandler;

    @Autowired
    private RogerMessageConverter messageConverter;

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

    public void syncFileData() {

    }

    public void connect() {
        int MAX_TEXT_MESSAGE_BUFFER_SIZE = 20 * 1024 * 1024;
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.setDefaultMaxTextMessageBufferSize(MAX_TEXT_MESSAGE_BUFFER_SIZE);
        WebSocketClient webSocketClient = new StandardWebSocketClient(container);
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(webSocketClient));
        SockJsClient sockJsClient = new SockJsClient(transports);
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.afterPropertiesSet();
        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(messageConverter);
        stompClient.setTaskScheduler(taskScheduler);
        stompClient.setDefaultHeartbeat(new long[] {0, 0});
        stompClient.connect("ws://roger-karotz.herokuapp.com/socket", socketHandler);
        stompClient.setTaskScheduler(taskScheduler);
    }

    @Override
    public void run() {
        health();
    }

}
