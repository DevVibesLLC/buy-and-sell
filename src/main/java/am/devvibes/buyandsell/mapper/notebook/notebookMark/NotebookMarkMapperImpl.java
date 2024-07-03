package am.devvibes.buyandsell.mapper.notebook.notebookMark;

import am.devvibes.buyandsell.dto.electronic.electronicMark.ElectronicMarkDto;
import am.devvibes.buyandsell.entity.mobile.MobilePhoneMarkEntity;
import am.devvibes.buyandsell.entity.notebook.NotebookMarkEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotebookMarkMapperImpl implements NotebookMarkMapper {

	@Override
	public ElectronicMarkDto mapEntityToDto(NotebookMarkEntity notebookMarkEntity) {
		return ElectronicMarkDto.builder()
				.id(notebookMarkEntity.getId())
				.mark(notebookMarkEntity.getName())
				.build();
	}

	@Override
	public List<ElectronicMarkDto> mapEntityListToDtoList(List<NotebookMarkEntity> notebookMarkEntities) {
		return notebookMarkEntities.stream().map(this::mapEntityToDto).toList();
	}

}
