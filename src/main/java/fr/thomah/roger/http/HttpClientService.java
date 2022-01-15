package fr.thomah.roger.http;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.http.HttpClient;

@Slf4j
@Service
public class HttpClientService {

    @Value("${fr.thomah.roger.url}")
    private String baseUrl;

    @Value("${server.port}")
    private String port;

    public java.net.http.HttpClient getHttpClient() {
        HttpClient httpClient;
        httpClient = java.net.http.HttpClient.newBuilder()
                .version(java.net.http.HttpClient.Version.HTTP_2)
                .build();
        return httpClient;
    }

    public String getBaseUrl() {

        // If a specific Base URL is set in properties, we return it
        if(baseUrl != null && !baseUrl.isEmpty())
            return baseUrl;

        // Else we return the URL with the host IP
        InetAddress localhost;
        try {
            localhost = InetAddress.getLocalHost();
            return "http://" + localhost.getHostAddress() + ":" + port;
        } catch (UnknownHostException e) {
            log.error("Unable to get host IP address");
        }
        return null;
    }

}