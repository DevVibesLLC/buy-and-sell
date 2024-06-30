package am.devvibes.buyandsell.repository;

import am.devvibes.buyandsell.BaseRepositoryTest;
import am.devvibes.buyandsell.entity.category.CategoryEntity;
import am.devvibes.buyandsell.entity.description.DescriptionEntity;
import am.devvibes.buyandsell.entity.field.FieldEntity;
import am.devvibes.buyandsell.entity.field.FieldNameEntity;
import am.devvibes.buyandsell.entity.item.ItemEntity;
import am.devvibes.buyandsell.repository.category.CategoryRepository;
import am.devvibes.buyandsell.repository.item.ItemRepository;
import am.devvibes.buyandsell.util.CategoryEnum;
import am.devvibes.buyandsell.util.Status;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.Arrays;

import static am.devvibes.buyandsell.util.DescriptionNameEnum.SPECIFICATIONS;

@DataJpaTest
class ItemForSellTest extends BaseRepositoryTest {

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private CategoryRepository categoryRepository;



	@Test
	void updateValueItemForSell() {
		var categoryEntity = categoryRepository.save(CategoryEntity.builder()
				.descriptions(Arrays.asList(DescriptionEntity.builder()
								.header(SPECIFICATIONS)
								.fields(Arrays.asList(
										FieldNameEntity.builder()
												.fieldName("mark")
												.measurement(null)
												.build()
								))
						.build()))
				.name(CategoryEnum.CARS).build());

		var laavBmw = itemRepository.save(ItemEntity.builder()
				.status(Status.CREATED)
				.title("LAAV BMW")
				.category(categoryEntity)
						.fields(Arrays.asList(
								FieldEntity.builder()
										.fieldName(categoryEntity.getDescriptions().get(0).getFields().get(0))
										.fieldValue("BMW")
										.build()
						))
				.userEntity(null)
				.build());
		Assertions.assertNotNull(categoryEntity.getId());

		ItemEntity itemForUpdate = itemRepository.findById(laavBmw.getId()).orElseThrow();
		var mers = FieldEntity.builder()
				.fieldName(categoryEntity.getDescriptions().get(0).getFields().get(0))
				.fieldValue("MERS")
				.build();
		ArrayList<FieldEntity> objects = new ArrayList<>();
		objects.add(mers);
		itemForUpdate.setFields(objects);
		ItemEntity save = itemRepository.save(itemForUpdate);
		Assertions.assertEquals(save.getFields().get(0).getFieldValue(),"MERS");


	}


}