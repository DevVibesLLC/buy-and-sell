package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.businessPage.BusinessPageRequestDto;
import am.devvibes.buyandsell.dto.businessPage.BusinessPageResponseDto;
import am.devvibes.buyandsell.dto.businessPage.BusinessPageUpdateDto;
import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.dto.item.ItemUpdateDto;
import am.devvibes.buyandsell.mapper.businessPage.BusinessPageMapper;
import am.devvibes.buyandsell.service.businessPage.BusinessPageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/businessPages")
public class BusinessPageController {

	private final BusinessPageService businessPageService;
	private final BusinessPageMapper businessPageMapper;

	@PostMapping("/{categoryId}")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Register Business Page")
	public ResponseEntity<BusinessPageResponseDto> registerPage(@PathVariable Long categoryId,
			@RequestBody BusinessPageRequestDto businessPageRequestDto) {
		return ResponseEntity.ok(businessPageMapper.mapEntityToDto(
				businessPageService.registerBusinessPage(businessPageRequestDto, categoryId)));
	}

	@GetMapping("/{pageId}")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Register Business Page")
	public ResponseEntity<BusinessPageResponseDto> getPage(@PathVariable Long pageId) {
		return ResponseEntity.ok(businessPageMapper.mapEntityToDto(businessPageService.findBusinessPageById(pageId)));
	}

	@PostMapping("add/{businessPageId}")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Create Item from Business Page")
	public ResponseEntity<BusinessPageResponseDto> createItem(@PathVariable Long businessPageId,
			@RequestBody ItemRequestDto itemRequestDto) {
		return ResponseEntity.ok(businessPageMapper.mapEntityToDto(
				businessPageService.addItemFromBusinessPage(itemRequestDto, businessPageId)));
	}

	@PutMapping("/{businessPageId}/{itemId}")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Update Item from Business Page")
	public ResponseEntity<BusinessPageResponseDto> updateItem(@PathVariable Long businessPageId,
			@PathVariable Long itemId,
			@RequestBody ItemUpdateDto itemUpdateDto) {
		return ResponseEntity.ok(businessPageMapper.mapEntityToDto(
				businessPageService.updateItemFromBusinessPage(itemUpdateDto, businessPageId, itemId)));
	}

	@PutMapping("/{businessPageId}")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Update Business Page")
	public ResponseEntity<BusinessPageResponseDto> updateBusinessPage(@PathVariable Long businessPageId,
			@RequestBody BusinessPageUpdateDto businessPageUpdateDto) {
		return ResponseEntity.ok(businessPageMapper.mapEntityToDto(
				businessPageService.updateBusinessPage(businessPageUpdateDto, businessPageId)));
	}

	@DeleteMapping("/{businessPageId}/{itemId}")
	@PreAuthorize("hasRole('ROLE_USER')")
	@Operation(summary = "Delete Item from Business Page")
	public ResponseEntity<Void> deleteItem(@PathVariable Long businessPageId, @PathVariable Long itemId) {
		businessPageService.deleteItemFromBusinessPage(businessPageId, itemId);
		return ResponseEntity.ok().build();
	}

}
