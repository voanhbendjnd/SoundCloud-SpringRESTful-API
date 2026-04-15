package djnd.project.SoundCloud.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import djnd.project.SoundCloud.domain.entity.Track;

import java.util.List;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long>, JpaSpecificationExecutor<Track> {
    boolean existsByTitleAndIdNot(String title, Long id);

    boolean existsByTitle(String tile);

}
