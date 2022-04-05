package fr.thomah.roger.karotz;

import fr.thomah.roger.common.Randomizer;
import fr.thomah.roger.file.FileEntity;
import fr.thomah.roger.file.FileService;
import fr.thomah.roger.http.HttpClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;
import java.util.TimerTask;

@Slf4j
@Component
public class KarotzTask extends TimerTask {

    @Autowired
    private FileService fileService;

    @Autowired
    private HttpClientService httpClientService;

    @Autowired
    private KarotzService karotzService;

    // Periodic Reboot
    @Value("${fr.thomah.roger.karotz.automate.reboot.delay}")
    private int rebootDelay;
    private LocalTime rebootLast = LocalTime.now();

    // Random Sound
    @Value("${fr.thomah.roger.karotz.automate.rmood.maxduration}")
    private int maxDuration;
    private int probaMin = 0;

    @Override
    public void run() {

        // Run periodic reboot
        LocalTime now = LocalTime.now();
        log.debug("Time since last reboot = " + Duration.between(rebootLast, now));
        if(Duration.between(rebootLast, now).compareTo(Duration.ofSeconds(rebootDelay)) > 0) {
            log.debug("Rebooting Karotz...");
            karotzService.reboot();
            rebootLast = LocalTime.now();
        }

        // Play random sound
        int random = Randomizer.generateNumberBetween(probaMin, maxDuration);
        log.debug("min = " + probaMin);
        log.debug("random = " + random);
        if (random == maxDuration) {
            probaMin = 0;
            FileEntity file = fileService.getRandomSound();
            karotzService.sound(httpClientService.getBaseUrl() + file.getUrl());
        } else {
            probaMin++;
        }
    }
}
