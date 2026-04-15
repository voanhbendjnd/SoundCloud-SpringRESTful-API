package djnd.project.SoundCloud.services;

import java.io.IOException;
import java.net.URISyntaxException;

import djnd.project.SoundCloud.domain.entity.Category;
import djnd.project.SoundCloud.domain.entity.User;
import jakarta.persistence.criteria.Join;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
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

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Service
public class TrackService {
    final TrackRepository trackRepository;
    final CategoryRepository categoryRepository;
    final UserRepository userRepository;
    final FileService fileService;
    final UserService userService;

    @Value("${djnd.soundcloud.location.folder.img}")
    private String imgFolder;
    @Value("${djnd.soundcloud.location.folder.temp}")
    private String tempFolder;
    @Value("${djnd.soundcloud.location.folder.audio}")
    private String audioFolder;

    /*
    * Save track audio before save infomation track!
    * */
    public String uploadTempTrack(MultipartFile trackUrl) throws URISyntaxException, IOException {
        return this.fileService.getFinalFileName(trackUrl, tempFolder);
    }

    private Track toTrack(TrackDTO dto) {
        var track = new Track();
        var category = this.categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category ID", "#" + dto.getCategoryId()));
        track.setCategory(category);
        track.setDescription(dto.getDescription());
        track.setTitle(dto.getTitle());
        return track;
    }

    public void createTrackByAdmin(TrackDTO dto, MultipartFile imgUrl, MultipartFile trackUrl) throws URISyntaxException, IOException, PermissionException{
        var track = this.toTrack(dto);
        var user = this.userService.getUserLoggedOrThrow();
        track.setUser(user);
        track.setImgUrl(this.fileService.getFinalFileName(imgUrl, imgFolder));
        track.setTrackUrl(this.fileService.getFinalFileName(trackUrl, audioFolder));
        this.trackRepository.save(track);
    }

    public void createByUser(TrackDTO dto, MultipartFile imgUrl, String trackFileName)
            throws URISyntaxException, IOException, PermissionException {
        var track = this.toTrack(dto);
        var user = this.userService.getUserLoggedOrThrow();
        track.setUser(user);
        track.setImgUrl(this.fileService.getFinalFileName(imgUrl, imgFolder));
        this.fileService.moveFolderToOtherFolder(trackFileName, tempFolder, audioFolder);
        track.setTrackUrl(trackFileName);
        this.trackRepository.save(track);
    }

    public void update(TrackDTO dto, MultipartFile imgUrl, MultipartFile trackUrl)
            throws URISyntaxException, IOException, PermissionException {
        var track = this.trackRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Track ID", "#" + dto.getId()));

        var category = this.categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category ID", "#" + dto.getCategoryId()));
        track.setCategory(category);
        track.setDescription(dto.getDescription());
        track.setTitle(dto.getTitle());

        if (imgUrl != null && !imgUrl.isEmpty()) {
            track.setImgUrl(this.fileService.getFinalFileName(imgUrl, imgFolder));
        }
        if (trackUrl != null && !trackUrl.isEmpty()) {
            track.setTrackUrl(this.fileService.getFinalFileName(trackUrl, audioFolder));
        }

        this.trackRepository.save(track);
    }

    public void delete(Long id) {
        var track = this.trackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Track ID", "#" + id));
        this.trackRepository.delete(track);
    }

    public TrackResponse fetchById(Long id) {
        var track = this.trackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Track ID", "#" + id));
        return convertToResponse(track);
    }

    public ResultPaginationDTO fetchAllWithPagination(Specification<Track> spec, Pageable pageable, String category) {
        var res = new ResultPaginationDTO();
        var meta = new ResultPaginationDTO.Meta();

        if (category != null && !category.isEmpty()) {
            Specification<Track> categorySpec = (root, query, cb) -> {
                Join<Track, Category> joinCategory = root.join("category");
                return cb.equal(joinCategory.get("name"), category);
            };
            spec = spec.and(categorySpec);
        }

        var page = this.trackRepository.findAll(spec, pageable);
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);
        res.setResult(page.getContent().stream().map(this::convertToResponse).toList());
        return res;
    }

    private TrackResponse convertToResponse(Track x) {
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
        if (user.getRole() != null) {
            uploader.setRole(user.getRole().getName());
        }
        result.setUploader(uploader);
        return result;
    }

    public ResultPaginationDTO getMyTrackUploaded(Specification<Track> spec, Pageable pageable, Long userId){
        var res = new ResultPaginationDTO();
        var meta = new ResultPaginationDTO.Meta();
        Specification<Track> ps = (r, q ,c ) ->{
          Join<Track, User> joinUser = r.join("user");
          return c.equal(joinUser.get("id"), userId);
        };
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        Page<Track> page = this.trackRepository.findAll(spec.and(ps), pageable);
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);
        res.setResult(page.getContent().stream().map(this::convertToResponse).toList());
        return res;
    }
}
