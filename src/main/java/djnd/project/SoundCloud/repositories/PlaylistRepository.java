package djnd.project.SoundCloud.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import djnd.project.SoundCloud.domain.entity.Playlist;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long>, JpaSpecificationExecutor<Playlist> {
    // @EntityGraph(attributePaths = { "user", "tracks", "tracks.user" })
    // Optional<Playlist> findById(Long id);

    @EntityGraph(attributePaths = { "user", "tracks", "tracks.user" })
    @Query("SELECT p FROM Playlist p WHERE p.id = :id")
    Optional<Playlist> findWithDetailsById(Long id);
}
