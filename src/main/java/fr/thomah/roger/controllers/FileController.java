package fr.thomah.roger.controllers;

import fr.thomah.roger.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/files")
public class FileController {

    public static final File FILES_DIR = new File("./files");

    @Autowired
    private Environment env;

    @PostConstruct
    public void init() {
        if (!FILES_DIR.exists()) {
            if (!FILES_DIR.mkdirs()) {
                throw new RuntimeException("Error creating files directory");
            }
        }
    }

    @RequestMapping(value = "/{fileName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public FileSystemResource get(@PathVariable("fileName") String fileName) {
        Path p = Paths.get(FILES_DIR.getPath(), fileName);
        if (p.toFile().exists()) {
            return new FileSystemResource(p);
        } else {
            throw new NotFoundException();
        }
    }

}
