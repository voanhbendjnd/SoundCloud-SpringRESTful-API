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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
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

    public String getFinalNameAvatarFile(MultipartFile file) throws URISyntaxException, IOException {
        var uploadPath = baseURI + "user";
        var directoryPath = Paths.get(uploadPath);
        Files.createDirectories(directoryPath);

        var originalName = file.getOriginalFilename();
        if (originalName == null) {
            originalName = "unnanamed";
        }
        var lastName = System.currentTimeMillis() + "-" + StringUtils.cleanPath(originalName);

        var filePath = directoryPath.resolve(lastName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        return lastName;
    }
}