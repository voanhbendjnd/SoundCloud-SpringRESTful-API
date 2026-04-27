package djnd.project.SoundCloud.services;

import org.springframework.stereotype.Service;

import djnd.project.SoundCloud.domain.entity.Playlist;
import djnd.project.SoundCloud.domain.request.PlaylistDTO;
import djnd.project.SoundCloud.domain.response.ResPlaylist;
import djnd.project.SoundCloud.repositories.PlaylistRepository;
import djnd.project.SoundCloud.repositories.TrackRepository;
import djnd.project.SoundCloud.utils.error.PermissionException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PlayListService {
    PlaylistRepository playlistRepository;
    UserService userService;
    TrackRepository trackRepository;
    TrackService trackService;

    public ResPlaylist createNewPlaylist(PlaylistDTO dto) throws PermissionException {
        var playlist = new Playlist();
        var currentUserLogin = this.userService.getUserLoggedOrThrow();
        playlist.setIsPublic(dto.getIsPublic());
        playlist.setTitle(dto.getTitle());
        playlist.setUser(currentUserLogin);
        if (dto.getTrackIds() != null && !dto.getTrackIds().isEmpty()) {
            var tracks = this.trackRepository.findByIdIn(dto.getTrackIds());
            if (!tracks.isEmpty()) {
                playlist.setTracks(tracks);
            }
        }
        var savePlaylist = this.playlistRepository.save(playlist);
        return this.toResPlaylist(this.playlistRepository.findWithDetailsById(savePlaylist.getId()).get());
    }

    public ResPlaylist toResPlaylist(Playlist playlist) {
        var res = new ResPlaylist();
        res.setCreatedAt(playlist.getCreatedAt());
        res.setCreatedBy(playlist.getCreatedBy());
        res.setDescription(playlist.getDescription());
        res.setId(playlist.getId());
        res.setImgUrl(playlist.getImgUrl());
        res.setIsPublic(playlist.getIsPublic());
        res.setTitle(playlist.getTitle());
        res.setTotalTracks(playlist.getTotalTracks());
        res.setUpdatedAt(playlist.getUpdatedAt());
        res.setUpdatedBy(playlist.getUpdatedBy());
        var userPlaylist = new ResPlaylist.User();
        var user = playlist.getUser();
        userPlaylist.setAvatar(user.getAvatar());
        userPlaylist.setId(user.getId());
        userPlaylist.setName(user.getName());
        userPlaylist.setRole(user.getRole().getName());
        res.setUser(userPlaylist);
        var playListTracks = playlist.getTracks().stream().map(x -> {
            var track = new ResPlaylist.PlaylistTrack();
            track.setId(x.getId());
            track.setCountLikes(x.getCountLike());
            track.setCountPlays(x.getCountPlay());
            track.setImgUrl(x.getImgUrl());
            track.setTitle(x.getTitle());
            track.setTrackUrl(this.trackService.getResTrackUrlId(x.getTrackUrl()));
            var uploader = x.getUser();
            var resUploader = new ResPlaylist.PlaylistTrack.Uploader();
            resUploader.setId(uploader.getId());
            resUploader.setAvatar(uploader.getAvatar());
            resUploader.setName(uploader.getName());
            resUploader.setRole(uploader.getRole().getName());
            track.setUploader(resUploader);
            return track;
        }).toList();
        res.setPlaylistTracks(playListTracks);
        res.setTotalTracks(playListTracks.size());
        return res;
    }
}
