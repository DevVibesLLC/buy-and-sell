package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.presignedUrl.PresignedUrlDto;
import am.devvibes.buyandsell.service.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/presigned-url")
public class PresignedUrlController {

	private final S3Service s3Service;

	@GetMapping("/image/{resolution}")
	public ResponseEntity<PresignedUrlDto> getPresignedUrlForImages(@RequestParam Map<String, String> metadata,
			@PathVariable String resolution) {
		PresignedUrlDto presignedUrlDto = s3Service.getPresignedUrlForImages(metadata, resolution);
		return ResponseEntity.ok(presignedUrlDto);
	}

	@GetMapping("/story/{resolution}")
	public ResponseEntity<PresignedUrlDto> getPresignedUrlForStories(@RequestParam Map<String, String> metadata,
			@PathVariable String resolution) {
		PresignedUrlDto presignedUrlDto = s3Service.getPresignedUrlForStories(metadata, resolution);
		return ResponseEntity.ok(presignedUrlDto);
	}

}