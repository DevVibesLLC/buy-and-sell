package am.devvibes.buyandsell.mapper.notebook.notebookMark;

import am.devvibes.buyandsell.dto.electronic.electronicMark.ElectronicMarkDto;
import am.devvibes.buyandsell.entity.mobile.MobilePhoneMarkEntity;
import am.devvibes.buyandsell.entity.notebook.NotebookMarkEntity;

import java.util.List;

public interface NotebookMarkMapper {

	ElectronicMarkDto mapEntityToDto(NotebookMarkEntity mobileMarkEntity);

	List<ElectronicMarkDto> mapEntityListToDtoList(List<NotebookMarkEntity> mobileMarkEntityList);

}
