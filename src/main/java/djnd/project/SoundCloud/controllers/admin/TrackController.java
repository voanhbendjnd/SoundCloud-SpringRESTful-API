package djnd.project.SoundCloud.controllers.admin;

import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import djnd.project.SoundCloud.domain.entity.Track;
import djnd.project.SoundCloud.domain.request.TrackDTO;
import djnd.project.SoundCloud.services.TrackService;
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

    @PostMapping
    public ResponseEntity<?> create(@Valid @ModelAttribute TrackDTO dto, @RequestPart("img") MultipartFile img,
            @RequestPart("track") MultipartFile track) throws URISyntaxException, IOException, PermissionException {
        this.trackService.create(dto, img, track);
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
    public ResponseEntity<?> delete(@PathVariable Long id) {
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
}
