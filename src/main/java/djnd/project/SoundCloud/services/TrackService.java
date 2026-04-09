package djnd.project.SoundCloud.services;

import java.io.IOException;
import java.net.URISyntaxException;

import djnd.project.SoundCloud.domain.entity.Category;
import jakarta.persistence.criteria.Join;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import djnd.project.SoundCloud.domain.entity.Track;
import djnd.project.SoundCloud.domain.request.TrackDTO;
import djnd.project.SoundCloud.domain.response.ResultPaginationDTO;
import djnd.project.SoundCloud.domain.response.TrackResponse;
import djnd.project.SoundCloud.repositories.CategoryRepository;
import djnd.project.SoundCloud.repositories.TrackRepository;
import djnd.project.SoundCloud.repositories.UserRepository;
import djnd.project.SoundCloud.utils.SecurityUtils;
import djnd.project.SoundCloud.utils.error.DuplicateResourceException;
import djnd.project.SoundCloud.utils.error.PermissionException;
import djnd.project.SoundCloud.utils.error.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Service
public class TrackService {
    TrackRepository trackRepository;
    CategoryRepository categoryRepository;
    UserRepository userRepository;
    FileService fileService;

    public void create(TrackDTO dto, MultipartFile imgUrl, MultipartFile trackUrl)
            throws URISyntaxException, IOException, PermissionException {
        var track = new Track();
        if (this.trackRepository.existsByTitle(dto.getTitle())) {
            throw new DuplicateResourceException("Track Title", dto.getTitle());
        }
        var category = this.categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category ID", "#" + dto.getCategoryId()));
        track.setCategory(category);
        track.setDescription(dto.getDescription());
        track.setTitle(dto.getTitle());
        var email = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new PermissionException("You do not have permission!"));
        var user = this.userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("Email User", email);
        }
        track.setUser(user);
        track.setImgUrl(this.fileService.getFinalNameTrackImgFile(imgUrl));
        track.setTrackUrl(this.fileService.getFinalNameTrackFile(trackUrl));
        this.trackRepository.save(track);
    }

    public ResultPaginationDTO fetchAllWithPagination(Specification<Track> spec, Pageable pageable, String category) {
        var res = new ResultPaginationDTO();
        var meta = new ResultPaginationDTO.Meta();
        Specification<Track> sp = (r, q, c) -> {
            Join<Track, Category> joinCategory = r.join("category");
            return c.equal(joinCategory.get("name"), "POP");

        };
        var page = this.trackRepository.findAll(Specification.allOf(spec.and(sp)), pageable);
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);
        res.setResult(page.getContent().stream().map(x -> {
            var result = new TrackResponse();
            result.setCategory(x.getCategory().getName());
            result.setCountLike(x.getCountLike());
            result.setCountPlay(x.getCountPlay());
            result.setCreatedAt(x.getCreatedAt());
            result.setDescription(x.getDescription());
            result.setId(x.getId());
            result.setImgUrl(x.getImgUrl());
            result.setTitle(x.getTitle());
            result.setTrackUrl(x.getTrackUrl());
            result.setUpdatedAt(x.getUpdatedAt());
            var user = x.getUser();
            var uploader = new TrackResponse.Uploader();
            uploader.setEmail(user.getEmail());
            uploader.setId(user.getId());
            uploader.setName(user.getName());
            uploader.setRole(user.getRole().getName());
            result.setUploader(uploader);
            return result;
        }).toList());
        return res;
    }
}
