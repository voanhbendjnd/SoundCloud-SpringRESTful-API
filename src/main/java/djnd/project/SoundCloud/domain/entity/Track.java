package djnd.project.SoundCloud.domain.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tracks")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Track {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String title;
    @Column(columnDefinition = "MEDIUMTEXT")
    String description;
    String imgUrl;
    String trackUrl;
    String trackPublicId;
    String imgPublicId;
    Integer countLike = 0;
    Long countPlay = 0L;
    @ColumnDefault("false")
    boolean deleted;
    LocalDateTime createdAt, updatedAt;
    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;
    @ManyToOne
    @JoinColumn(name = "category_id")
    Category category;
    @OneToMany(mappedBy = "track", cascade = CascadeType.ALL)
    List<Comment> comments;
    @OneToMany(mappedBy = "track", cascade = CascadeType.ALL)
    List<TrackLike> trackLikes;

    @PrePersist
    public void handleBeforeCreateAt() {
        this.updatedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void handleBeforeUpdateAt() {
        this.updatedAt = LocalDateTime.now();
    }

}
