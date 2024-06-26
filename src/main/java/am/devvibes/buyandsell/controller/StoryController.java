package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.entity.story.StoryEntity;
import am.devvibes.buyandsell.service.story.StoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/story")
public class StoryController {

	private final StoryService storyService;

	@PostMapping
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Save Story")
	public ResponseEntity<StoryEntity> createItem(@RequestParam(value = "story") MultipartFile story,
			@RequestParam(value = "caption") String caption) {

		StoryEntity storyEntity = storyService.saveStory(story, caption);
		return ResponseEntity.ok(storyEntity);
	}

	@GetMapping("/{userId}")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Get Users Stories")
	public ResponseEntity<List<StoryEntity>> getUserStories(@PathVariable String userId) {
		List<StoryEntity> storiesByUserId = storyService.getStoriesByUserId(userId);
		return ResponseEntity.ok(storiesByUserId);
	}

	@DeleteMapping("/{storyId}")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Delete Users Story")
	public ResponseEntity<Void> deleteUserStory(@PathVariable Long storyId) {
		storyService.deleteStory(storyId);
		return ResponseEntity.ok().build();
	}

}
