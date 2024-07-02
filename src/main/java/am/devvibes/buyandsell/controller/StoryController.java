package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.story.StoryRequestDto;
import am.devvibes.buyandsell.dto.story.StoryResponseDto;
import am.devvibes.buyandsell.service.story.StoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/story")
public class StoryController {

	private final StoryService storyService;

	@PostMapping
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Save Story")
	public ResponseEntity<StoryResponseDto> createItem(@RequestBody StoryRequestDto storyRequestDto) {
		StoryResponseDto storyResponseDto = storyService.saveStory(storyRequestDto);
		return ResponseEntity.ok(storyResponseDto);
	}

	@GetMapping("/{userId}")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Get Users Stories")
	public ResponseEntity<List<StoryResponseDto>> getUserStories(@PathVariable String userId) {
		List<StoryResponseDto> storiesByUserId = storyService.getStoriesByUserId(userId);
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
