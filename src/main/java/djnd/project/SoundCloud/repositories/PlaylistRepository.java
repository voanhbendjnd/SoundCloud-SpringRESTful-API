package djnd.project.SoundCloud.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import djnd.project.SoundCloud.domain.entity.Playlist;
import djnd.project.SoundCloud.domain.it.PlaylistTrackInterface;
import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long>, JpaSpecificationExecutor<Playlist> {
    // @EntityGraph(attributePaths = { "user", "tracks", "tracks.user" })
    // Optional<Playlist> findById(Long id);

    @EntityGraph(attributePaths = { "user", "playlistTracks", "playlistTracks.track", "playlistTracks.track.user" })
    @Query("SELECT p FROM Playlist p WHERE p.id = :id")
    Optional<Playlist> findWithDetailsById(Long id);

    /**
     * Modify for spring know this method write data instead of read data
     * clearAuto delete cache in JPA, because data response track old iof track
     * update
     * 
     * @param trackId
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "update Playlist p set p.totalTracks = p.totalTracks + :qtyTrack where p.id = :playlistId")
    void incremementTrackInPlaylist(@Param("playlistId") Long playlistId, @Param("qtyTrack") Integer qtyTrack);

    /**
     * Modify for spring know this method write data instead of read data
     * clearAuto delete cache in JPA, because data response track old iof track
     * update
     * 
     * @param trackId
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "update Playlist p set p.totalTracks = p.totalTracks - :qtyTrack where p.id = :playlistId")
    void decremementTrackInPlaylist(@Param("playlistId") Long playlistId, @Param("qtyTrack") Integer qtyTrack);

    @Query(value = "select p.totalTracks from Playlist p where p.id = :playlistId")
    Integer getTotalTrackById(@Param("playlistId") Long playlistId);

    @Query(value = "select p.id as id, p.title as title, pt.track.id as trackId from Playlist p left join p.playlistTracks pt where p.user.id = :userId")
    List<PlaylistTrackInterface> getAllPlaylistExistsByUserId(@Param("userId") Long userId);

}
