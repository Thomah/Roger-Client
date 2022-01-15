package fr.thomah.roger;

import fr.thomah.roger.common.MethodPoller;
import fr.thomah.roger.karotz.KarotzService;
import fr.thomah.roger.karotz.KarotzTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Timer;

@SpringBootApplication
public class RogerApplication {

	@Autowired
	private KarotzService karotzService;

	@Autowired
	private KarotzTask karotzTask;

	@Value("${fr.thomah.roger.karotz.automate.interval}")
	private int interval;

	public static void main(String[] args) {
		SpringApplication.run(RogerApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		MethodPoller<HttpResponse<String>> poller = new MethodPoller<>();

		// Wait for Karotz to be accessible
		poller.method(karotzService::status)
				.until(karotzService::statusValidation)
				.poll(Duration.ofHours(1), 1000)
				.execute();

		Timer timer = new Timer(true);
		timer.scheduleAtFixedRate(karotzTask, 0, interval * 1000L);
	}
}
