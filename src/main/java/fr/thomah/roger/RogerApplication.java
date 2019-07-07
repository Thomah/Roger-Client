package fr.thomah.roger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Timer;
import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
public class RogerApplication {

	public static void main(String[] args) {
		SpringApplication.run(RogerApplication.class, args);
	}

}
