package fr.thomah.roger.file;

import fr.thomah.roger.common.Randomizer;
import fr.thomah.roger.http.MimeTypes;
import fr.thomah.roger.http.exception.BadRequestException;
import fr.thomah.roger.http.exception.InternalServerException;
import fr.thomah.roger.http.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;

@Slf4j
@Service
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    @Value("${fr.thomah.roger.storage.root}")
    private String rootStorageFolder;

    @Value("${fr.thomah.roger.storage.data}")
    private String dataStorageFolder;

    public byte[] readOnFilesystem(HttpServletRequest request, String prefix) throws IOException {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String matchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String pathFile = new AntPathMatcher().extractPathWithinPattern(matchPattern, path);
        pathFile = pathFile.replaceAll("/", Matcher.quoteReplacement(File.separator));
        pathFile = prefix.replaceAll("/", Matcher.quoteReplacement(File.separator)) + pathFile;
        InputStream in = new FileInputStream(rootStorageFolder + File.separator + dataStorageFolder + File.separator + pathFile);
        log.debug("Getting file {}", rootStorageFolder + File.separator + dataStorageFolder + File.separator + pathFile);
        byte[] fileContent = IOUtils.toByteArray(in);
        in.close();
        return fileContent;
    }

    public List<FileEntity> list() {
        return fileRepository.findAll(Sort.by("originalName").ascending());
    }

    public List<FileEntity> listSounds() {
        return fileRepository.findAllByDirectory("sounds", Sort.by("originalName").ascending());
    }

    public List<FileEntity> listSnapshots() {
        return fileRepository.findAllByDirectory("snapshots", Sort.by("originalName").ascending());
    }

    public FileEntity getRandomSound() {
        List<FileEntity> files = fileRepository.findAllByDirectory("sounds", Sort.by("originalName").ascending());
        return files.get(Randomizer.generateNumberBetween(0, files.size()));
    }

    public FileEntity saveOnFilesystem(String filename, String mimeType, String directory, String originalName) throws IOException {

        UUID id = UUID.randomUUID();
        String name = id.toString();

        // Extract data from File
        log.debug("contentType: " + mimeType);
        String format = MimeTypes.getDefaultExt(mimeType);
        log.debug("format: " + format);
        String finalFileName = name + "." + format;
        log.debug("saved filename: " + finalFileName);

        // Write file on filesystem
        prepareLocalDirectories(directory);
        String destination = rootStorageFolder + java.io.File.separator + dataStorageFolder + File.separator + directory + java.io.File.separator + finalFileName;
        move(filename, destination);

        // Create entity to return
        FileEntity entity = new FileEntity();
        entity.setId(id);
        entity.setDirectory(directory);
        entity.setName(name);
        entity.setOriginalName(originalName);
        entity.setFormat(format);
        entity.setUrl("/files/" + entity.getDirectory() + "/" + finalFileName);

        return entity;
    }
    public FileEntity saveOnFilesystem(MultipartFile multipartFile) throws IOException {

        String fullPath;
        String finalFileName;
        UUID id = UUID.randomUUID();
        String name = id.toString();
        String directory = "sounds";

        // Extract data from MultipartFile
        InputStream inputStream = multipartFile.getInputStream();
        log.debug("inputStream: " + inputStream);
        String originalName = multipartFile.getOriginalFilename();
        log.debug("originalName: " + originalName);
        String contentType = multipartFile.getContentType();
        log.debug("contentType: " + contentType);
        long size = multipartFile.getSize();
        log.debug("size: " + size);
        String format = MimeTypes.getDefaultExt(contentType);
        log.debug("format: " + format);
        finalFileName = name + "." + format;
        log.debug("saved filename: " + finalFileName);

        // Write file on filesystem
        prepareLocalDirectories(directory);
        fullPath = rootStorageFolder + File.separator + dataStorageFolder + File.separator + directory + File.separator + finalFileName;
        java.io.File file = new java.io.File(fullPath);
        FileOutputStream os = new FileOutputStream(file);
        os.write(multipartFile.getBytes());
        os.close();

        // Create entity to return
        FileEntity entity = new FileEntity();
        entity.setId(id);
        entity.setDirectory(directory);
        entity.setName(name);
        entity.setOriginalName(multipartFile.getOriginalFilename());
        entity.setFormat(format);
        entity.setUrl("/files/" + entity.getDirectory() + "/" + finalFileName);

        return entity;
    }

    public FileEntity saveInDb(FileEntity newEntity) {
        FileEntity entity = null;
        if(newEntity.getId() != null) {
            entity = fileRepository.findById(newEntity.getId()).orElse(null);
        }
        if(entity == null) {
            entity = new FileEntity();
        }
        entity.setId(newEntity.getId());
        entity.setDirectory(newEntity.getDirectory());
        entity.setName(newEntity.getName());
        entity.setOriginalName(newEntity.getOriginalName());
        entity.setFormat(newEntity.getFormat());
        entity.setUrl(newEntity.getUrl());
        return fileRepository.save(entity);
    }

    public void saveAll(List<FileEntity> files) {
        fileRepository.saveAll(files);
    }

    public Boolean deleteById(UUID id) {

        // Check if ID is provided
        if(id == null) {
            log.error("Impossible to delete file : ID is missing");
            throw new BadRequestException();
        }

        // Check if file is registered in DB
        FileEntity entity = fileRepository.findById(id).orElse(null);
        if(entity == null) {
            log.error("Impossible to delete file : file {} does not exist in DB", id);
            throw new NotFoundException();
        }

        // Delete file in filesystem
        java.io.File file = new java.io.File(getPath(entity));
        if(file.exists()) {
            if(!file.delete()) {
                log.error("Impossible to delete file : unknown error");
                throw new InternalServerException();
            }
        } else {
            log.error("Impossible to delete file : file {} does not exist in filesystem", id);
        }

        // Delete file in DB
        fileRepository.delete(entity);

        return Boolean.TRUE;
    }

    public String prepareDirectories(String directoryPath) {
        File directory = new File(rootStorageFolder);
        if (!directory.exists()) {
            log.info("Creating path {}", directory.getPath());
            if (!directory.isDirectory()) {
                log.error("The path {} is not a directory", directory.getPath());
            }
        }
        if (!directory.isDirectory()) {
            log.error("The path {} is not a directory", directory.getPath());
        }

        if (directoryPath != null && !directoryPath.isEmpty()) {
            directory = new File(rootStorageFolder + File.separator + directoryPath.replaceAll("//", File.separator));
            log.debug("Prepare directory {}", directory.getAbsolutePath());
            if (!directory.exists()) {
                log.info("Creating path {}", directory.getPath());
                if(!directory.mkdirs()) {
                    log.error("Cannot create directory {}", directory.getAbsolutePath());
                }
            }
            if (!directory.isDirectory()) {
                log.error("The path {} is not a directory", directory.getPath());
            }
        }
        return directory.getAbsolutePath();
    }

    private void prepareLocalDirectories(String directoryPath) {
        File directory = new File(rootStorageFolder);
        if (!directory.exists()) {
            log.info("Creating path {}", directory.getPath());
            if (!directory.isDirectory()) {
                log.error("The path {} is not a directory", directory.getPath());
            }
        }
        if (!directory.isDirectory()) {
            log.error("The path {} is not a directory", directory.getPath());
        }

        if (directoryPath != null && !directoryPath.isEmpty()) {
            directory = new File(rootStorageFolder + File.separator + dataStorageFolder + File.separator + directoryPath.replaceAll("//", File.separator));
            log.debug("Prepare directory {}", directory.getAbsolutePath());
            if (!directory.exists()) {
                log.info("Creating path {}", directory.getPath());
                if(!directory.mkdirs()) {
                    log.error("Cannot create directory {}", directory.getAbsolutePath());
                }
            }
            if (!directory.isDirectory()) {
                log.error("The path {} is not a directory", directory.getPath());
            }
        }
    }

    public String getPath(FileEntity entity) {
        return rootStorageFolder + java.io.File.separator + dataStorageFolder + File.separator + entity.getDirectory() + java.io.File.separator + entity.getFullname();
    }

    private void move(String source, String destination) {
        try (InputStream is = new FileInputStream(source); OutputStream os = new FileOutputStream(destination)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            if(!new File(source).delete()) {
                log.warn("Cannot delete {}", source);
            }
        } catch (FileNotFoundException e) {
            log.error("File {} or {} not found", source, destination, e);
        } catch (IOException e) {
            log.error("An error occurred when copying {} to {}", source, destination, e);
        }
    }

}
