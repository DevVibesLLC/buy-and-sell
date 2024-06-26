package am.devvibes.buyandsell.service;

import am.devvibes.buyandsell.entity.story.StoryEntity;
import am.devvibes.buyandsell.repository.story.StoryRepository;
import am.devvibes.buyandsell.service.story.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduledTasksService {

    private final StoryRepository storyRepository;
    private final StoryService storyService;

    @Scheduled(fixedRate = 30000)
    public void deleteOldRecords() {
        ZonedDateTime threshold = ZonedDateTime.now().minusMinutes(2);
        List<StoryEntity> oldRecords = storyRepository.findByCreatedAtBefore(threshold);
        
        oldRecords.forEach(s -> storyService.deleteStory(s.getId()));
    }
}
