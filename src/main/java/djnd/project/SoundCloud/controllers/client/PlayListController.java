package djnd.project.SoundCloud.controllers.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
