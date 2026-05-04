package djnd.project.SoundCloud.domain.it;

public interface ResHistoryInter {
    Long getId();

    String getTitle();

    String getImgUrl();

    String getTrackUrl();

    Integer getCountLikes();

    Long getCountPlays();

    String getUploader();
}
