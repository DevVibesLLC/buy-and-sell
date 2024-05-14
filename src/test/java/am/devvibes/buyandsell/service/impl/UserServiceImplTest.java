package am.devvibes.buyandsell.service.impl;

import am.devvibes.buyandsell.BaseRepositoryTest;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.model.dto.UserResponseDto;
import am.devvibes.buyandsell.model.dto.UserSignUpDto;
import am.devvibes.buyandsell.repository.UserRepository;
import am.devvibes.buyandsell.service.UserService;
import am.devvibes.buyandsell.service.configuration.UserTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

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
		UserResponseDto userResponseDto = userService.saveUser(UserSignUpDto.builder()
				.email("email@email.com")
				.name("name")
				.secondName("secondName")
				.password("password")
				.repeatPassword("password")
				.build());

		UserResponseDto userEntity = userService.findUserById(userResponseDto.getId());

		assertNotNull(userEntity);
		assertEquals(userEntity.getId(), userResponseDto.getId());
	}

	@Test
	void findAllUsers() {
		userService.saveUser(UserSignUpDto.builder()
				.email("email1@email.com")
				.name("name")
				.secondName("secondName")
				.password("password")
				.repeatPassword("password")
				.build());

		userService.saveUser(UserSignUpDto.builder()
				.email("email2@email.com")
				.name("name")
				.secondName("secondName")
				.password("password")
				.repeatPassword("password")
				.build());

		List<UserResponseDto> userEntities = userService.findAllUsers();

		assertNotNull(userEntities);
		assertEquals(userEntities.size(), 2);
	}

	@Test
	void deleteUser() {
		UserResponseDto userResponseDto = userService.saveUser(UserSignUpDto.builder()
				.email("email@email.com")
				.name("name")
				.secondName("secondName")
				.password("password")
				.repeatPassword("password")
				.build());

		userService.deleteUser(userResponseDto.getId());

		NotFoundException notFoundException =
				assertThrows(NotFoundException.class, () -> userService.findUserById(userResponseDto.getId()));
		assertTrue(notFoundException.getMessage().contains("User not found"));
	}

}