package djnd.project.SoundCloud.controllers.client;

import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import djnd.project.SoundCloud.services.UserService;
import djnd.project.SoundCloud.utils.annotation.ApiMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileController {
    UserService userService;

    /*
     * dto: email
     * file
     */
    @PatchMapping("/avatar/upload")
    @ApiMessage("Update avatar for user")
    public ResponseEntity<?> updateAvatar(@RequestPart("avatar") MultipartFile file)
            throws URISyntaxException, IOException {
        if (this.userService.updateAvatarUser(file)) {
            return ResponseEntity.ok("Upload avatar successfull");
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload avatar");
    }
}
