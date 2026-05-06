package djnd.project.SoundCloud.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import djnd.project.SoundCloud.domain.entity.HistoryTrack;
import djnd.project.SoundCloud.domain.it.ResHistoryInter;

@Repository
public interface HistoryTrackRepository
                extends JpaRepository<HistoryTrack, Long>, JpaSpecificationExecutor<HistoryTrack> {
        @Query(value = "select h.track.id from HistoryTrack h join h.user u where u.id= :userId")
        List<Long> getTrackIdsByUserId(@Param("userId") Long userId);

        @Query(value = """
                        select ht.track.id as id, ht.track.title as title, ht.track.imgUrl as imgUrl, ht.track.trackUrl as trackUrl,ht.track.countPlay as countPlays, ht.track.countLike as countLikes, ht.user.name as uploader
                        from HistoryTrack ht
                        join ht.user u
                        where u.id = :userId
                        order by ht.listenedAt desc
                        """)
        List<ResHistoryInter> getMyTrackListened(@Param("userId") Long userId, Pageable pageable);

        boolean existsByUserIdAndTrackId(Long userId, Long trackId);

        HistoryTrack findByUserIdAndTrackId(Long userId, Long trackId);

        @Modifying
        @Query(value = """
                        INSERT INTO history_tracks (user_id, track_id, duration_listened, listened_at)
                        VALUES (:userId, :trackId, :duration, NOW())
                        ON DUPLICATE KEY UPDATE
                        duration_listened = GREATEST(duration_listened, :duration),
                        listened_at = NOW()
                        """, nativeQuery = true)
        void upsertHistory(Long userId, Long trackId, Integer duration);
}
