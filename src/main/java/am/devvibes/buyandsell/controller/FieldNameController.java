package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.dto.field.FieldRequestDto;
import am.devvibes.buyandsell.entity.FieldNameEntity;
import am.devvibes.buyandsell.service.field.FieldService;
import am.devvibes.buyandsell.service.fieldName.FieldNameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/fieldNames")
public class FieldNameController {

	private final FieldNameService fieldNameService;

	@PostMapping("/{fieldName}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<FieldNameEntity> createFieldName(@PathVariable @Valid String fieldName) {
		return ResponseEntity.ok(fieldNameService.addFieldName(fieldName));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<FieldNameEntity> getFieldNameById(@PathVariable Long id) {
		return ResponseEntity.ok(fieldNameService.findFieldNameById(id));
	}

	@GetMapping
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<List<FieldNameEntity>> getAllFieldNames() {
		return ResponseEntity.ok(fieldNameService.findAllFieldNames());
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<List<FieldNameEntity>> deleteFieldNameById(@PathVariable Long id) {
		fieldNameService.deleteFieldNameById(id);
		return ResponseEntity.ok().build();
	}

}
