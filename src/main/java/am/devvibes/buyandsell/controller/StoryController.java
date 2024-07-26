package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.story.StoryRequestDto;
import am.devvibes.buyandsell.dto.story.StoryResponseDto;
import am.devvibes.buyandsell.entity.story.StoryEntity;
import am.devvibes.buyandsell.mapper.story.StoryMapper;
import am.devvibes.buyandsell.service.story.StoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/story")
public class StoryController {

	private final StoryService storyService;
	private final StoryMapper storyMapper;

	@PostMapping
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Save Story")
	public ResponseEntity<StoryResponseDto> createItem(@RequestBody StoryRequestDto storyRequestDto) {
		StoryEntity storyEntity = storyService.saveStory(storyRequestDto);
		return ResponseEntity.ok(storyMapper.mapEntityToDto(storyEntity));
	}

	@GetMapping("/{userId}")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Get Users Stories")
	public ResponseEntity<List<StoryResponseDto>> getUserStories(@PathVariable String userId) {
		List<StoryEntity> storyEntities = storyService.getStoriesByUserId(userId);
		return ResponseEntity.ok(storyMapper.mapEntityListToDtoList(storyEntities));
	}

	@DeleteMapping("/{storyId}")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Delete Users Story")
	public ResponseEntity<Void> deleteUserStory(@PathVariable Long storyId) {
		storyService.deleteStory(storyId);
		return ResponseEntity.ok().build();
	}

}