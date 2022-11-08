package fr.thomah.roger.karotz;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class KarotzController {

    @Autowired
    private KarotzService karotzService;

    @RequestMapping(value = "/api/karotz/sound", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public void sound(@RequestParam(name = "url") String url) {
        karotzService.sound(url);
    }

    @RequestMapping(value = "/api/karotz/snapshot", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public void snapshot() {
        karotzService.tts("Dans 5 secondes je vais prendre une photo. Préparez-vous.");
        try {
            Thread.sleep(5000);
            karotzService.takeSnapshot();
            karotzService.downloadSnapshot(karotzService.listSnapshots());
            karotzService.clearSnapshots();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
