package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.presignedUrl.PresignedUrlDto;
import am.devvibes.buyandsell.service.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/presigned-url")
public class PresignedUrlController {

	private final S3Service s3Service;

	@GetMapping("/image")
	public ResponseEntity<PresignedUrlDto> getPresignedUrlForImages(@RequestParam String resolution) {
		PresignedUrlDto presignedUrlDto = s3Service.getPresignedUrlForImages(resolution);
		return ResponseEntity.ok(presignedUrlDto);
	}

	@GetMapping("/story")
	public ResponseEntity<PresignedUrlDto> getPresignedUrlForStories(@RequestParam String resolution) {
		PresignedUrlDto presignedUrlDto = s3Service.getPresignedUrlForStories(resolution);
		return ResponseEntity.ok(presignedUrlDto);
	}

	@GetMapping("/businessPage/banner")
	public ResponseEntity<PresignedUrlDto> getPresignedUrlForBusinessPageBanner(@RequestParam String resolution) {
		PresignedUrlDto presignedUrlDto = s3Service.getPresignedUrlForBusinessPageBanner(resolution);
		return ResponseEntity.ok(presignedUrlDto);
	}

	@GetMapping("/businessPage/logo")
	public ResponseEntity<PresignedUrlDto> getPresignedUrlForBusinessPageLogo(@RequestParam String resolution) {
		PresignedUrlDto presignedUrlDto = s3Service.getPresignedUrlForBusinessPageLogo(resolution);
		return ResponseEntity.ok(presignedUrlDto);
	}

}