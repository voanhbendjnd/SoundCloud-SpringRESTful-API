package djnd.project.SoundCloud.controllers.admin;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import djnd.project.SoundCloud.domain.entity.Comment;
import djnd.project.SoundCloud.domain.entity.Track;
import djnd.project.SoundCloud.domain.request.TrackDTO;
import djnd.project.SoundCloud.services.CommentService;
import djnd.project.SoundCloud.services.TrackService;
import djnd.project.SoundCloud.utils.annotation.ApiMessage;
import djnd.project.SoundCloud.utils.error.PermissionException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/tracks")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TrackController {
    TrackService trackService;
    CommentService commentService;

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getMyTrackUploaded(@Filter Specification<Track> spec, Pageable pageable,
            @PathVariable("id") Long userId) {
        return ResponseEntity.ok(this.trackService.getMyTrackUploaded(spec, pageable, userId));
    }

    @PostMapping("/upload-temp")
    public ResponseEntity<?> uploadTemp(@RequestPart("track") MultipartFile track)
            throws URISyntaxException, IOException {
        return ResponseEntity.ok(this.trackService.uploadTempTrack(track));
    }

    @PostMapping
    public ResponseEntity<?> createTrackByUser(@Valid @ModelAttribute TrackDTO dto,
            @RequestPart(value = "img", required = false) MultipartFile img,
            @RequestParam(value = "trackUrl", required = false) String trackFileName)
            throws URISyntaxException, Exception, PermissionException {
        this.trackService.createByUser(dto, img, trackFileName);
        return ResponseEntity.status(HttpStatus.CREATED).body("Create new track success");
    }

    @PostMapping("/admin")
    public ResponseEntity<?> createTrackByAdmin(@Valid @ModelAttribute TrackDTO dto,
            @RequestPart(value = "img", required = false) MultipartFile img,
            @RequestParam(value = "trackUrl", required = false) MultipartFile trackUrl)
            throws URISyntaxException, IOException, PermissionException {
        this.trackService.createTrackByAdmin(dto, img, trackUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body("Create new track success");
    }

    @PutMapping

    public ResponseEntity<?> update(@Valid @ModelAttribute TrackDTO dto,
            @RequestPart(value = "img", required = false) MultipartFile img,
            @RequestPart(value = "track", required = false) MultipartFile track)
            throws URISyntaxException, IOException, PermissionException {
        this.trackService.update(dto, img, track);
        return ResponseEntity.ok("Update track success");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) throws IOException {
        this.trackService.delete(id);
        return ResponseEntity.ok("Delete track success");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> fetchById(@PathVariable Long id) {
        return ResponseEntity.ok(this.trackService.fetchById(id));
    }

    @GetMapping
    public ResponseEntity<?> fetchAllWithPagination(@Filter Specification<Track> spec, Pageable pageable,
            @RequestParam(value = "category", required = false) String category) {
        return ResponseEntity.ok(this.trackService.fetchAllWithPagination(spec, pageable, category));
    }

    @GetMapping("/comments")
    @ApiMessage("Fetch All Comment By Track ID")
    public ResponseEntity<?> getAllCommentByTrackID(@Filter Specification<Comment> spec, Pageable pageable,
            @RequestParam(value = "trackId", required = false) String trackIdStr) {
        Long trackId = null;
        if (trackIdStr != null) {
            try {
                trackId = Long.parseLong(trackIdStr);

            } catch (NumberFormatException ne) {
                return ResponseEntity.badRequest().body("Track ID not number!");
            }
        }
        return ResponseEntity.ok(this.commentService.fetchAllWithPaginationDTO(spec, pageable, trackId));

    }

    @GetMapping("/avatar")
    @ApiMessage("Get avatar uploader")
    public ResponseEntity<?> getAvatarUrlUploader(@RequestParam("trackId") Long trackId) {
        return ResponseEntity.ok(this.trackService.getUrlAvatarUploaderByTrackID(trackId));
    }

    @PostMapping("/likes")
    @ApiMessage("Hanlde count likes track")
    public ResponseEntity<?> handleCountLikesTrack(@RequestBody Map<String, Long> request)
            throws PermissionException {
        var trackId = request.get("trackId");
        return ResponseEntity.status(HttpStatus.CREATED).body(this.trackService.handleCountLikeTrack(trackId));
    }

    @PatchMapping("/view/increase")
    @ApiMessage("Count play track")
    public ResponseEntity<?> increamentCountPlayTrack(@RequestBody Map<String, Long> mpRequest) {
        var trackId = mpRequest.get("trackId");
        this.trackService.increamentCountPlayTrackToRedis(trackId);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/likes")
    @ApiMessage("Get track my like")
    public ResponseEntity<?> getMyLikeTrack(@Filter Specification<Track> spec, Pageable pageable)
            throws PermissionException {
        return ResponseEntity.ok(this.trackService.getMyLikeTrack(spec, pageable));
    }

    @GetMapping("/isExists")
    @ApiMessage("Exist URL track and ID")
    public ResponseEntity<?> checkTrackExist(@RequestParam("trackId") Long trackId,
            @RequestParam("lastId") Long trackIdLast) {

        this.trackService.checkIdAndAudioFile(trackId, trackIdLast);
        return ResponseEntity.ok(true);
    }

    @GetMapping("/audio")
    @ApiMessage("Get url track by track Id")
    public ResponseEntity<?> getURlTrack(@RequestParam("trackId") String trackIdStr) {
        Long trackId = null;
        if (trackIdStr != null) {
            try {
                trackId = Long.parseLong(trackIdStr);

            } catch (NumberFormatException ne) {
                return ResponseEntity.badRequest().body("Track ID not number!");
            }
        }
        return ResponseEntity.ok(this.trackService.getTrackUrlById(trackId));
    }
}
