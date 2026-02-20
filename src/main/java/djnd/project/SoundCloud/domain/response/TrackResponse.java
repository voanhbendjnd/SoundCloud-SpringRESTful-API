package djnd.project.SoundCloud.domain.response;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TrackResponse {
    Long id;
    String title;
    String description;
    String category;
    String imgUrl;
    String trackUrl;
    Integer countLike;
    Long countPlay;
    Uploader uploader;
    LocalDateTime createdAt, updatedAt;

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Uploader {
        Long id;
        String email;
        String name;
        String role;
    }
}
