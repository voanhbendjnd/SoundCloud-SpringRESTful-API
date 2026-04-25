package djnd.project.SoundCloud.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import djnd.project.SoundCloud.domain.entity.Track;
import djnd.project.SoundCloud.domain.it.TrackUploader;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long>, JpaSpecificationExecutor<Track> {
    boolean existsByTitleAndIdNot(String title, Long id);

    boolean existsByTitle(String tile);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "update Track t set t.countLike = t.countLike + 1 where t.id = :trackId")
    void increamentCountLikes(@Param("trackId") Long trackId);

    /**
     * Modify for spring know this method write data instead of read data
     * clearAuto delete cache in JPA, because data response track old iof track
     * update
     * 
     * @param trackId
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "update Track t set t.countLike = t.countLike - 1 where t.id = :trackId")
    void decreamentCountLikes(@Param("trackId") Long trackId);

    @Query(value = "select t.countLike from Track t where t.id = :trackId")
    Integer getCountLike(@Param("trackId") Long id);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "update Track t set t.countPlay = t.countPlay + 1 where t.id = :trackId")
    void increamentCountPlay(@Param("trackId") Long trackId);

    @Query(value = "select t.countPlay from Track t where t.id = :trackId")
    Long getCountPlayTrack(@Param("trackId") Long id);

    @Query(value = "select u.id as id, u.name as name, u.avatar as avatar from Track t join t.user u where t.id = :trackId")
    TrackUploader getUploader(@Param("trackId") Long id);

    boolean existsByTrackUrlAndId(String trackUrl, Long trackId);

    @Query(value = "select t.trackUrl from Track t where t.id = :trackId")
    String getUrlTrackById(@Param("trackId") Long trackId);
}
