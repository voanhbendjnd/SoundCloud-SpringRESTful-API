package djnd.project.SoundCloud.domain.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TrackDTO {
    Long id;
    String title;
    String description;
    Long categoryId;
}
