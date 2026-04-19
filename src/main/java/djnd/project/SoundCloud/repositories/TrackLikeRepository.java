package djnd.project.SoundCloud.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import djnd.project.SoundCloud.domain.entity.TrackLike;

@Repository
public interface TrackLikeRepository extends JpaRepository<TrackLike, Long> {
    boolean existsByUserIdAndTrackId(Long userId, Long trackId);

    // void deleteByUserIdAndTrackId(Long userId, Long trackId);

    TrackLike findByUserIdAndTrackId(Long userId, Long trackId);

    /**
     * flushAuto delete done call db tức thời
     * 
     * @param userId
     * @param trackId
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM TrackLike tl WHERE tl.user.id = :userId AND tl.track.id = :trackId")
    void deleteByUserIdAndTrackId(@Param("userId") Long userId, @Param("trackId") Long trackId);
}
