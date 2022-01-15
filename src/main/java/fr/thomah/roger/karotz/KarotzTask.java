package fr.thomah.roger.karotz;

import fr.thomah.roger.common.Randomizer;
import fr.thomah.roger.file.FileEntity;
import fr.thomah.roger.file.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.TimerTask;

@Slf4j
@Component
public class KarotzTask extends TimerTask {

    @Autowired
    private FileService fileService;

    @Autowired
    private KarotzService karotzService;

    @Value("${fr.thomah.roger.karotz.automate.maxduration}")
    private int maxduration;

    private int probaMin = 0;

    @Override
    public void run() {
        int random = Randomizer.generateNumberBetween(probaMin, maxduration);
        log.debug("min = " + probaMin);
        log.debug("random = " + random);
        if (random == maxduration) {
            probaMin = 0;
            FileEntity file = fileService.getRandomSound();
            karotzService.sound(file.getUrl());
        } else {
            probaMin++;
        }
    }
}
