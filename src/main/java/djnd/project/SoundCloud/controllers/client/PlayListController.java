package djnd.project.SoundCloud.controllers.client;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import djnd.project.SoundCloud.domain.entity.Playlist;
import djnd.project.SoundCloud.domain.request.AddTrackToPlaylistDTO;
import djnd.project.SoundCloud.domain.request.PlaylistDTO;
import djnd.project.SoundCloud.services.PlayListService;
import djnd.project.SoundCloud.utils.annotation.ApiMessage;
import djnd.project.SoundCloud.utils.error.PermissionException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/playlists")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PlayListController {
    PlayListService playListService;

    @PostMapping
    @ApiMessage("Create new play list with tracks")
    public ResponseEntity<?> createNewPlaylist(@RequestBody PlaylistDTO dto) throws PermissionException {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.playListService.createNewPlaylist(dto));
    }

    @PatchMapping
    @ApiMessage("Handle onclick track to playlist")
    public ResponseEntity<?> handleOnclickPlaylistExists(@RequestBody AddTrackToPlaylistDTO dto,
            @RequestParam("trackId") Long trackId) {
        return ResponseEntity.ok(this.playListService.handleOnClickTrackToPlaylist(dto, trackId));
    }

    @GetMapping("/exists")
    @ApiMessage("Get all playlist and track id")
    public ResponseEntity<?> getDataPlaylist() throws PermissionException {
        return ResponseEntity.ok(this.playListService.getAllPlaylistAccount());
    }

    @GetMapping("/users")
    @ApiMessage("Get playlist with pagination")
    public ResponseEntity<?> getAllPlayListWithUserId(@Filter Specification<Playlist> spec, Pageable pageable,
            @RequestParam(value = "title", required = false) String title) throws PermissionException {
        return ResponseEntity
                .ok(this.playListService.getAllPlaylistWithPagination(spec, pageable, title != null ? title : ""));
    }
}
