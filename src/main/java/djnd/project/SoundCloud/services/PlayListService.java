package djnd.project.SoundCloud.services;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import djnd.project.SoundCloud.domain.entity.Playlist;
import djnd.project.SoundCloud.domain.entity.PlaylistTrack;
import djnd.project.SoundCloud.domain.it.PlaylistKey;
import djnd.project.SoundCloud.domain.request.AddTrackToPlaylistDTO;
import djnd.project.SoundCloud.domain.request.PlaylistDTO;
import djnd.project.SoundCloud.domain.response.ResAddToPlaylist;
import djnd.project.SoundCloud.domain.response.ResAllPlaylist;
import djnd.project.SoundCloud.domain.response.ResPlaylist;
import djnd.project.SoundCloud.repositories.PlaylistRepository;
import djnd.project.SoundCloud.repositories.PlaylistTrackRepository;
import djnd.project.SoundCloud.repositories.TrackRepository;
import djnd.project.SoundCloud.utils.SecurityUtils;
import djnd.project.SoundCloud.utils.error.PermissionException;
import djnd.project.SoundCloud.utils.error.ResourceNotFoundException;
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
    PlaylistTrackRepository playlistTrackRepository;

    public ResPlaylist createNewPlaylist(PlaylistDTO dto) throws PermissionException {
        var playlist = new Playlist();
        var currentUserLogin = this.userService.getUserLoggedOrThrow();
        playlist.setIsPublic(dto.getIsPublic());
        playlist.setTitle(dto.getTitle());
        playlist.setUser(currentUserLogin);

        if (dto.getTrackIds() != null && !dto.getTrackIds().isEmpty()) {
            var tracks = this.trackRepository.findByIdIn(dto.getTrackIds());
            if (!tracks.isEmpty()) {
                playlist.setPlaylistTracks(tracks.stream().map(x -> {
                    var playlistTrack = new PlaylistTrack();
                    playlistTrack.setPlaylist(playlist);
                    playlistTrack.setTrack(x);
                    playlistTrack.setIsAdded(true);
                    return playlistTrack;
                }).toList());
                playlist.setTotalTracks(tracks.size());
            }
        }
        var savePlaylist = this.playlistRepository.save(playlist);

        return this.toResPlaylist(this.playlistRepository.findWithDetailsById(savePlaylist.getId()).get());
    }

    @Transactional
    public ResAddToPlaylist handleOnClickTrackToPlaylist(AddTrackToPlaylistDTO dto, Long trackId) {
        var playlist = this.playlistRepository.findWithDetailsById(dto.getPlaylistId())
                .orElseThrow(() -> new ResourceNotFoundException("Playlist ID", dto.getPlaylistId()));
        boolean existsTrackInPlaylist = this.playlistTrackRepository.existsByPlaylistIdAndTrackId(dto.getPlaylistId(),
                trackId);

        if (dto.getIsAdded() && !existsTrackInPlaylist) {
            var trackProxy = this.trackRepository.getReferenceById(trackId);
            var playlistTrack = new PlaylistTrack();
            playlistTrack.setPlaylist(playlist);
            playlistTrack.setTrack(trackProxy);
            playlistTrack.setIsAdded(dto.getIsAdded());
            this.playlistTrackRepository.save(playlistTrack);
            this.playlistRepository.incremementTrackInPlaylist(playlist.getId(), 1);
            var currentTotalTracks = playlist.getTotalTracks() + 1;
            playlist.setTotalTracks(currentTotalTracks);
            return this.toResAddToPlaylist(playlist.getId(), existsTrackInPlaylist, currentTotalTracks);
        }
        if (!dto.getIsAdded()) {
            var playlistTrackDB = this.playlistTrackRepository.findByPlaylistIdAndTrackId(dto.getPlaylistId(),
                    trackId);
            if (playlistTrackDB != null) {
                this.playlistRepository.decremementTrackInPlaylist(playlist.getId(), 1);
                this.playlistTrackRepository.deleteByPlaylistIdAndTrackId(playlist.getId(), trackId);
                var currentTotalTracks = playlist.getTotalTracks() - 1;
                if (currentTotalTracks < 0) {
                    throw new DataAccessResourceFailureException("Data Access Resource Failure!");
                }
                playlist.setTotalTracks(currentTotalTracks);

                return this.toResAddToPlaylist(playlist.getId(), existsTrackInPlaylist, currentTotalTracks);
            }
        }

        throw new ResourceNotFoundException("Action invalid", "nil");
    }

    private ResAddToPlaylist toResAddToPlaylist(Long playlistId, boolean isAdded, int total) {
        var res = new ResAddToPlaylist();
        res.setId(playlistId);
        res.setIsAdded(!isAdded);
        res.setTotalTracks(total);
        return res;
    }

    public List<ResAllPlaylist> getAllPlaylistAccount() throws PermissionException {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId != null) {
            return this.playlistRepository.getAllPlaylistExistsByUserId(userId).stream()
                    .collect(Collectors.groupingBy(x -> new PlaylistKey(x.getId(), x.getTitle()),
                            Collectors.mapping(x -> x.getTrackId(), Collectors.toList())))
                    .entrySet().stream().map(entry -> {
                        var trackIds = entry.getValue().stream().filter(Objects::nonNull).toList();
                        return new ResAllPlaylist(entry.getKey().id(), entry.getKey().title(), trackIds.size(),
                                trackIds);
                    }).toList();
        }
        throw new PermissionException("You do not have permission!");

    }

    public ResPlaylist toResPlaylist(Playlist playlist) {
        var res = new ResPlaylist();
        res.setCreatedAt(playlist.getCreatedAt());
        res.setCreatedBy(playlist.getCreatedBy());
        res.setDescription(playlist.getDescription());
        res.setId(playlist.getId());
        res.setImgUrl(playlist.getImgUrl() != null ? playlist.getImgUrl()
                : playlist.getPlaylistTracks().getLast().getTrack().getImgUrl());
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
        var playlistTracks = playlist.getPlaylistTracks().stream().map(x -> {
            var resPlaylistTrack = new ResPlaylist.ResPlaylistTrack();
            var track = x.getTrack();
            resPlaylistTrack.setId(track.getId());
            resPlaylistTrack.setCountLikes(track.getCountLike());
            resPlaylistTrack.setCountPlays(track.getCountPlay());
            resPlaylistTrack.setImgUrl(track.getImgUrl());
            resPlaylistTrack.setTitle(track.getTitle());
            resPlaylistTrack.setTrackUrl(this.trackService.getResTrackUrlId(track.getTrackUrl()));
            var uploader = track.getUser();
            var resUploader = new ResPlaylist.ResPlaylistTrack.Uploader();
            resUploader.setAvatar(uploader.getAvatar());
            resUploader.setId(uploader.getId());
            resUploader.setName(uploader.getName());
            resPlaylistTrack.setUploader(resUploader);
            return resPlaylistTrack;
        }).toList();
        res.setPlaylistTracks(playlistTracks);
        res.setTotalTracks(playlistTracks.size());
        return res;
    }
}
