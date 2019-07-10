package fr.thomah.roger;

import fr.thomah.roger.clients.BackClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class RogerApplication {

	public static final String SLACK_TOKEN = System.getenv("SlackToken");
	public static final String BASE_URL = System.getenv("BaseURL");
	public static final String THIS_COMPUTER_URL = System.getenv("ComputerURL");
	public static final boolean KAROTZ_AVAILABLE = BASE_URL != null;
	public static String PROXY_HOST = null;
	public static int PROXY_PORT = 0;

	@Autowired
	private BackClient backClient;

	public static void main(String[] args) {
		SpringApplication.run(RogerApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		String proxy = System.getenv("HTTP_PROXY");
		if (proxy != null) {
			proxy = proxy.replaceFirst("http://", "");
			String[] PROXY_VALUES = proxy.split(":");
			PROXY_HOST = PROXY_VALUES[0];
			PROXY_PORT = Integer.valueOf(PROXY_VALUES[1]);
		}

		HttpClient.Builder builder = HttpClient.newBuilder();
		if (PROXY_HOST != null && PROXY_PORT != 0) {
			builder = builder.proxy(ProxySelector.of(new InetSocketAddress(PROXY_HOST, PROXY_PORT)));
		}

		backClient.init(builder.build(), HttpRequest.newBuilder());
	}
}
