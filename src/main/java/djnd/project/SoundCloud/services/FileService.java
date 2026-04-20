package djnd.project.SoundCloud.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.scheduling.annotation.Scheduled;
import java.nio.file.attribute.BasicFileAttributes;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileService {
    @Value("${djnd.upload-file.base-uri}")
    private String baseURI;

    @Autowired
    private Cloudinary cloudinary;

    @Data
    public static class UploadResult {
        private String secureUrl;
        private String publicId;
    }

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

    public void moveFolderToOtherFolder(String fileName, String from, String to)
            throws URISyntaxException, IOException {
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
        if (!Files.exists(directoryPath))
            return;

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
                    System.err.println("Error processing file in " + tempFolder + file.getFileName());
                }
            });
        } catch (IOException e) {
            System.err.println("Error listing " + tempFolder + " directory.");
        }
    }

    public String uploadToTemp(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        if (originalName == null)
            originalName = "filename";
        String publicId = System.currentTimeMillis() + "-" + StringUtils.cleanPath(originalName);

        Map params = ObjectUtils.asMap(
                "public_id", publicId,
                "folder", tempFolder,
                "resource_type", "auto");

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

        return uploadResult.get("public_id").toString();
    }

    public String moveCloudinaryFile(String currentPublicId, String targetFolder) throws Exception {
        String fileName = currentPublicId.substring(currentPublicId.lastIndexOf("/") + 1);
        String newPublicId = targetFolder + "/" + fileName;
        if (currentPublicId.equals(newPublicId)) {
            Map resource = cloudinary.api().resource(newPublicId, ObjectUtils.asMap("resource_type", "video"));
            return resource.get("secure_url").toString();
        }
        cloudinary.uploader().rename(currentPublicId, newPublicId, ObjectUtils.asMap(
                "resource_type", "video"));
        Map resource = cloudinary.api().resource(newPublicId, ObjectUtils.asMap("resource_type", "video"));
        return resource.get("secure_url").toString();
    }

    public String getCloudinaryUrl(String publicId, String resourceType) {
        return cloudinary.url().publicId(publicId).resourceType(resourceType).format("mp3").secure(true).toString();
    }

    public UploadResult uploadToCloudinary(MultipartFile file, String folder) throws IOException {
        String originalName = file.getOriginalFilename();
        if (originalName == null)
            originalName = "filename";

        String cleanName = StringUtils.cleanPath(originalName);
        String publicId = System.currentTimeMillis() + "-" + cleanName;

        Map params = ObjectUtils.asMap(
                "public_id", publicId,
                "folder", folder,
                "resource_type", "auto");

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

        UploadResult result = new UploadResult();
        result.setSecureUrl(uploadResult.get("secure_url").toString());
        result.setPublicId(publicId);

        return result;
    }

    public void deleteCloudinaryFile(String publicId, String resourceType) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));
    }
}