package djnd.project.SoundCloud.domain.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "playlists")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class Playlist extends BaseEntity {
    String title;
    String imgUrl;
    @Column(columnDefinition = "MEDIUMTEXT")
    String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;
    @ManyToMany
    @JoinTable(name = " playlist_tracks", joinColumns = @JoinColumn(name = "playlist_id"), inverseJoinColumns = @JoinColumn(name = "track_id"))
    List<Track> tracks;
    Integer totalTracks;
    Boolean isPublic;

}
