package fr.thomah.roger.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
public class FileController {

    @Autowired
    private FileService fileService;

    @RequestMapping(value = "/files/**", method = RequestMethod.GET)
    public @ResponseBody byte[] getFile(HttpServletRequest request) throws IOException {
        return fileService.readOnFilesystem(request, "/");
    }

    @RequestMapping(value = "/files/snapshots/**", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    public @ResponseBody byte[] getSnapshot(HttpServletRequest request) throws IOException {
        return fileService.readOnFilesystem(request, "/snapshots/");
    }

    @RequestMapping(value = "/api/sounds", method = RequestMethod.GET)
    public List<FileEntity> sounds() {
        return fileService.listSounds();
    }

    @RequestMapping(value = "/api/snapshots", method = RequestMethod.GET)
    public List<FileEntity> snapshots() {
        return fileService.listSnapshots();
    }

    @RequestMapping(value = "/api/files", method = RequestMethod.GET)
    public List<FileEntity> list() {
        return fileService.list();
    }

    @RequestMapping(value = "/api/files/upload", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public FileEntity upload(
            @RequestParam(name = "matches", required = false) String matches,
            @RequestParam("file") MultipartFile multipartFile) {

        FileEntity entity = new FileEntity();
        entity.setMatches(matches);
        try {
            entity = fileService.saveOnFilesystem(multipartFile);
            entity = fileService.saveInDb(entity);
        } catch (IOException e) {
            log.error("Cannot save file {}", multipartFile.getName(), e);
        }

        return entity;
    }

    @RequestMapping(value = "/api/files", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void update(@RequestBody List<FileEntity> files) {
        fileService.saveAll(files);
    }

    @RequestMapping(value = "/api/files/{id}", method = RequestMethod.DELETE)
    public Boolean deleteByUrl(@PathVariable("id") UUID id) {
        return fileService.deleteById(id);
    }
}
