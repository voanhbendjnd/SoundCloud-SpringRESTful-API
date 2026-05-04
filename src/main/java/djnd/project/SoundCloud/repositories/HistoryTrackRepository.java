package djnd.project.SoundCloud.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import djnd.project.SoundCloud.domain.entity.HistoryTrack;

@Repository
public interface HistoryTrackRepository
        extends JpaRepository<HistoryTrack, Long>, JpaSpecificationExecutor<HistoryTrack> {
    @Query(value = "select h.track.id from HistoryTrack h join h.user u where u.id= :userId")
    List<Long> getTrackIdsByUserId(@Param("userId") Long userId);
}
