package djnd.project.SoundCloud.domain.response.users;

import java.time.Instant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResUser {
    Long id;
    String email;
    String name;
    String createdBy, updatedBy;
    Instant createdAt, updatedAt;
    Role role;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Role {
        private Long id;
        private String name;
    }
}
