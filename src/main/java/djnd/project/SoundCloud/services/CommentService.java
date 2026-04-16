package djnd.project.SoundCloud.services;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import djnd.project.SoundCloud.domain.entity.Comment;
import djnd.project.SoundCloud.domain.request.CommentDTO;
import djnd.project.SoundCloud.domain.response.ResComment;
import djnd.project.SoundCloud.domain.response.ResultPaginationDTO;
import djnd.project.SoundCloud.repositories.CommentRepository;
import djnd.project.SoundCloud.utils.error.PermissionException;
import djnd.project.SoundCloud.utils.error.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CommentService {
    CommentRepository commentRepository;
    TrackService trackService;
    UserService userService;

    public void create(CommentDTO dto) throws PermissionException {
        var comment = new Comment();
        comment.setContent(dto.content());
        comment.setMoment(dto.moment());
        comment.setTrack(this.trackService.getTrackOrThrow(dto.trackId()));
        comment.setUser(this.userService.getUserLoggedOrThrow());
        this.commentRepository.save(comment);
    }

    public ResComment fetchById(Long id) {
        var comment = this.commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment ID", id + ""));
        return this.toRes(comment);

    }

    public ResultPaginationDTO fetchAllWithPaginationDTO(Specification<Comment> spec, Pageable pageable) {
        var res = new ResultPaginationDTO();
        var meta = new ResultPaginationDTO.Meta();
        var page = this.commentRepository.findAll(spec, pageable);
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);
        res.setResult(page.getContent().stream().map(this::toRes).toList());
        return res;
    }

    private ResComment toRes(Comment comment) {
        var res = new ResComment();
        res.setContent(comment.getContent());
        res.setLikesCount(comment.getLikesCount());
        res.setId(comment.getId());
        res.setTrackTitle(comment.getTrack().getTitle());
        res.setUserEmail(comment.getUser().getEmail());
        res.setUpdatedAt(comment.getUpdatedAt());
        res.setCreatedAt(comment.getCreatedAt());
        res.setUpdatedBy(comment.getUpdatedBy());
        res.setCreatedBy(comment.getCreatedBy());
        return res;
    }

    public void deleteById(Long id) {
        var comment = this.commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment ID", id));
        this.commentRepository.delete(comment);
    }
}
