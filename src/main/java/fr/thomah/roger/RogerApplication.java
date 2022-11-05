package fr.thomah.roger;

import fr.thomah.roger.common.MethodPoller;
import fr.thomah.roger.http.HttpClientService;
import fr.thomah.roger.karotz.KarotzRoutine;
import fr.thomah.roger.karotz.KarotzService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Timer;

@Slf4j
@SpringBootApplication
public class RogerApplication {

	@Autowired
	private HttpClientService httpClientService;

	@Autowired
	private KarotzService karotzService;

	@Autowired
	private KarotzRoutine karotzRoutine;

	@Value("${fr.thomah.roger.karotz.automate.interval}")
	private int interval;

	public static void main(String[] args) {
		SpringApplication.run(RogerApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		log.info("Roger is listening on {}", httpClientService.getBaseUrl());

		MethodPoller<HttpResponse<String>> poller = new MethodPoller<>();

		// Wait for Karotz to be accessible
		poller.method(karotzService::status)
				.until(karotzService::statusValidation)
				.poll(Duration.ofHours(1), 1000)
				.execute();

		Timer timer = new Timer(true);
		timer.scheduleAtFixedRate(karotzRoutine, 0, interval * 1000L);

	}
}
