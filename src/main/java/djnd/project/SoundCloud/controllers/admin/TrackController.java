package djnd.project.SoundCloud.controllers.admin;

import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.turkraft.springfilter.boot.Filter;

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
    public ResponseEntity<?> create(@ModelAttribute TrackDTO dto, @RequestPart("img") MultipartFile img,
            @RequestPart("track") MultipartFile track) throws URISyntaxException, IOException, PermissionException {
        this.trackService.create(dto, img, track);
        return ResponseEntity.status(HttpStatus.CREATED).body("Create new track success");
    }

    @GetMapping
    public ResponseEntity<?> fetchAllWithPagination(@Filter Specification<Track> spec, Pageable pageable) {
        return ResponseEntity.ok(this.trackService.fetchAllWithPagination(spec, pageable));
    }
}
