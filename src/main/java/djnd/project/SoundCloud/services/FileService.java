package djnd.project.SoundCloud.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.scheduling.annotation.Scheduled;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileService {
    @Value("${djnd.upload-file.base-uri}")
    private String baseURI;
    public void createFolder(String folder) throws URISyntaxException, IOException {
        var path = Paths.get(baseURI + folder);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            System.out.println(">>> CREATE NEW DIRECTORY SUCCESSFUL, PATH = " + path);
        } else {
            System.out.println(">>> SKIP MAKING DIRECTORY, ALREADY EXISTS");

        }
    }

    public String getFinalFileName(MultipartFile file, String folder) throws URISyntaxException, IOException {
        var uploadPath = baseURI + folder;
        var directoryPath = Paths.get(uploadPath);
        Files.createDirectories(directoryPath);

        var originalName = file.getOriginalFilename();
        if (originalName == null) {
            originalName = "filename";
        }
        var lastName = System.currentTimeMillis() + "-" + StringUtils.cleanPath(originalName);

        var filePath = directoryPath.resolve(lastName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        return lastName;
    }


    public void moveFolderToOtherFolder(String fileName, String from, String to) throws URISyntaxException, IOException {
        var tempPath = Paths.get(baseURI + from).resolve(fileName);
        var audioDirPath = Paths.get(baseURI + to);
        Files.createDirectories(audioDirPath);
        var finalPath = audioDirPath.resolve(fileName);

        if (Files.exists(tempPath)) {
            Files.move(tempPath, finalPath, StandardCopyOption.REPLACE_EXISTING);
        } else {
            throw new IOException("Temp file does not exist: " + tempPath);
        }
    }
    @Value("${djnd.soundcloud.location.folder.temp}")
    private String tempFolder;
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanOldTempTracks() {
        var directoryPath = Paths.get(baseURI + tempFolder);
        if (!Files.exists(directoryPath)) return;

        long twentyFourHoursAgo = Instant.now().minusSeconds(24 * 60 * 60).toEpochMilli();

        try {
            Files.list(directoryPath).forEach(file -> {
                try {
                    BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
                    if (attrs.creationTime().toMillis() <= twentyFourHoursAgo) {
                        Files.delete(file);
                        System.out.println("Deleted old temp file: " + file.getFileName());
                    }
                } catch (IOException e) {
                    System.err.println("Error processing file in " + tempFolder  + file.getFileName());
                }
            });
        } catch (IOException e) {
            System.err.println("Error listing " + tempFolder + " directory.");
        }
    }

}