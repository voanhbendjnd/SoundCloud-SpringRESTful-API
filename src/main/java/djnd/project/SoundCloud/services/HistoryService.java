package djnd.project.SoundCloud.services;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import djnd.project.SoundCloud.domain.entity.HistoryTrack;
import djnd.project.SoundCloud.domain.it.ResHistoryInter;
import djnd.project.SoundCloud.domain.request.HistoryDTO;
import djnd.project.SoundCloud.repositories.HistoryTrackRepository;
import djnd.project.SoundCloud.repositories.TrackRepository;
import djnd.project.SoundCloud.repositories.UserRepository;
import djnd.project.SoundCloud.utils.SecurityUtils;
import djnd.project.SoundCloud.utils.error.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class HistoryService {
    UserRepository userRepository;
    TrackRepository trackRepository;
    HistoryTrackRepository historyTrackRepository;

    public ResHistoryInter saveHistoryTrackUserListened(HistoryDTO dto) {
        var userId = SecurityUtils.getCurrentUserIdOrNull();
        var user = this.userRepository.getReferenceById(userId);
        var track = this.trackRepository.getReferenceById(dto.trackId());
        if (user != null && track != null) {
            var historyTrack = new HistoryTrack();
            historyTrack.setDurationListened(dto.durationListend());
            historyTrack.setTrack(track);
            historyTrack.setUser(user);
            this.historyTrackRepository.save(historyTrack);
            return this.trackRepository.getTrackForHistoryById(dto.trackId());
        }
        throw new ResourceNotFoundException("Track ID or User ID", dto.trackId() + " " + userId);
    }

    public List<ResHistoryInter> getHistoryTrackListened() {
        var userId = SecurityUtils.getCurrentUserIdOrNull();
        Pageable pageable = PageRequest.of(0, 5);
        var trackIds = this.historyTrackRepository.getTrackIdsByUserId(userId);
        return this.trackRepository.getTracksForHistoryIdIn(trackIds, pageable);
    }
}
