package fr.thomah.roger;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.rtm.RTMClient;
import com.github.seratch.jslack.api.rtm.RTMMessageHandler;
import com.github.seratch.jslack.api.rtm.message.Typing;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Timer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class Application implements CommandLineRunner {

    static String SLACK_TOKEN = null;
    static String BASE_URL = null;
    static String THIS_COMPUTER_URL = null;
    static String PROXY_HOST = null;
    static int PROXY_PORT = 0;
    static int REFRESH_MS = 2000;
    static boolean KAROTZ_AVAILABLE = false;

    @Autowired
    private Environment env;

    @Autowired
    private SlackClient slackClient;

    private HttpClient httpClient;

    @Override
    public void run(String... args) throws Exception {
        SLACK_TOKEN = env.getProperty("SlackToken");
        BASE_URL = env.getProperty("BaseURL");
        THIS_COMPUTER_URL = env.getProperty("ComputerURL");
        KAROTZ_AVAILABLE = BASE_URL != null;

        String proxy = env.getProperty("HTTP_PROXY");
        if (proxy != null) {
            proxy = proxy.replaceFirst("http://", "");
            String[] PROXY_VALUES = proxy.split(":");
            PROXY_HOST = PROXY_VALUES[0];
            PROXY_PORT = Integer.valueOf(PROXY_VALUES[1]);
        }

        Db db = new Db();
        MethodPoller<HttpResponse<String>> poller = new MethodPoller<>();
        HttpClient.Builder builder = HttpClient.newBuilder();

        if (PROXY_HOST != null && PROXY_PORT != 0) {
            builder = builder.proxy(ProxySelector.of(new InetSocketAddress(PROXY_HOST, PROXY_PORT)));
        }
        httpClient = builder.build();
        KarotzClient karotzClient = new KarotzClient(httpClient);
        slackClient.springInit();

        // Wait for Slack to be accessible
        poller.method(slackClient::ping)
                .until(slackClient::pingValidation)
                .poll(Duration.ofHours(1), 1000)
                .execute();

        // Wait for Karotz to be accessible
        poller.method(karotzClient::status)
                .until(karotzClient::statusValidation)
                .poll(Duration.ofHours(1), 1000)
                .execute();

        slackClient.setKarotzClient(karotzClient);
        slackClient.setDb(db);
        slackClient.init();
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(slackClient, 0, REFRESH_MS);
        try {
            Thread.sleep(36000000); // Wait 10h
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
         
    }

}
