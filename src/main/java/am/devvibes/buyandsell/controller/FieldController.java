package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.field.FieldRequestDto;
import am.devvibes.buyandsell.dto.item.ItemRequestDto;
import am.devvibes.buyandsell.entity.FieldEntity;
import am.devvibes.buyandsell.service.field.FieldService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/fields")
public class FieldController {

	private final FieldService fieldService;

	@PostMapping
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<FieldEntity> createField(@RequestBody @Valid FieldRequestDto fieldRequestDto) {
		return ResponseEntity.ok(fieldService.addField(fieldRequestDto));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<FieldEntity> getFieldById(@PathVariable Long id) {
		return ResponseEntity.ok(fieldService.findFieldById(id));
	}

	@GetMapping
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<List<FieldEntity>> getAllFields() {
		return ResponseEntity.ok(fieldService.findAllFields());
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<List<FieldEntity>> deleteFieldById(@PathVariable Long id) {
		fieldService.deleteFieldById(id);
		return ResponseEntity.ok().build();
	}

}
