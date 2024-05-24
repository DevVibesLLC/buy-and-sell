package am.devvibes.buyandsell.service.configuration;

import am.devvibes.buyandsell.service.itemForSell.ItemForSellService;
import am.devvibes.buyandsell.service.itemForSell.impl.ItemForSellServiceImpl;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class ItemForSellTestConfiguration {

	@Bean
	public ItemForSellMapper itemForSellMapper() {
		return new ItemForSellMapperImpl();
	}

	@Bean
	public ItemForSellService itemForSellService(ItemForSellRepository itemForSellRepository, ItemForSellMapper itemForSellMapper) {
		return new ItemForSellServiceImpl(itemForSellRepository, itemForSellMapper);
	}
}
