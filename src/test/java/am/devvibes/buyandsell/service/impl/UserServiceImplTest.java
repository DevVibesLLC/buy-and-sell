package am.devvibes.buyandsell.service.impl;

import am.devvibes.buyandsell.BaseRepositoryTest;
import am.devvibes.buyandsell.TestBuyAndSellApplication;
import am.devvibes.buyandsell.model.dto.UserResponseDto;
import am.devvibes.buyandsell.model.dto.UserSignUpDto;
import am.devvibes.buyandsell.model.entity.UserEntity;
import am.devvibes.buyandsell.repository.UserRepository;
import am.devvibes.buyandsell.service.UserService;
import am.devvibes.buyandsell.service.configuration.UserTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ContextConfiguration(classes = UserTestConfiguration.class)
class UserServiceImplTest extends BaseRepositoryTest {

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Test
	void saveUser() {
		UserResponseDto userResponseDto = userService.saveUser(UserSignUpDto.builder()
						.email("email@email.com")
						.name("name")
						.secondName("secondName")
						.password("password")
						.repeatPassword("password")
						.build());

		assertNotNull(userResponseDto);
		assertTrue(userRepository.existsUserEntityByEmail(userResponseDto.getEmail()));
	}

	@Test
	void findUserById() {
	}

	@Test
	void findAllUsers() {
	}

	@Test
	void deleteUser() {
	}

}