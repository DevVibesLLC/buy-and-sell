package am.devvibes.buyandsell.service.impl;

import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.exception.SomethingWentWrong;
import am.devvibes.buyandsell.mapper.UserMapper;
import am.devvibes.buyandsell.model.dto.UserResponseDto;
import am.devvibes.buyandsell.model.dto.UserSignUpDto;
import am.devvibes.buyandsell.model.entity.UserEntity;
import am.devvibes.buyandsell.repository.UserRepository;
import am.devvibes.buyandsell.service.UserService;
import am.devvibes.buyandsell.util.Constants;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public UserResponseDto saveUser(UserSignUpDto signUpDto) {
		validateUser(signUpDto);
		UserEntity userEntity = userMapper.mapDtoToEntity(signUpDto);
		UserEntity saved = userRepository.save(userEntity);
		return userMapper.mapEntityToDto(saved);
	}

	@Override
	@Transactional
	public UserResponseDto findUserById(Long id) {
		UserEntity user =
				userRepository.findById(id).orElseThrow(() -> new NotFoundException(Constants.USER_NOT_FOUND));
		return userMapper.mapEntityToDto(user);
	}

	@Override
	@Transactional
	public List<UserResponseDto> findAllUsers() {
		List<UserEntity> allUsers = userRepository.findAll();
		return userMapper.mapEntityListToDtoList(allUsers);
	}

	@Override
	@Transactional
	public void deleteUser(Long id) {
		userRepository.deleteById(id);
	}

	@Override
	public UserResponseDto changePassword(String email,
			String oldPassword,
			String newPassword,
			String repeatNewPassword) {

		UserEntity userEntity = userRepository.findByEmailAndPassword(email, passwordEncoder.encode(oldPassword))
				.orElseThrow(() -> new NotFoundException(Constants.USER_NOT_FOUND));
		comparePasswordsAndValidate(newPassword, repeatNewPassword);
		userEntity.setPassword(passwordEncoder.encode(newPassword));
		return userMapper.mapEntityToDto(userRepository.save(userEntity));
	}

	private void validateUser(UserSignUpDto signUpDto) {
		if (userRepository.existsUserEntityByEmail(signUpDto.getEmail())) {
			throw new SomethingWentWrong(Constants.USER_WITH_THIS_EMAIL_ALREADY_EXISTS);
		}
		comparePasswordsAndValidate(signUpDto.getPassword(), signUpDto.getRepeatPassword());
		EmailValidator.getInstance().isValid(signUpDto.getEmail());
	}

	private void comparePasswordsAndValidate(String password1, String password2) {
		if (password1.length() < 8 || password2.length() < 8) {
			throw new SomethingWentWrong(Constants.PASSWORD_LENGTH_IS_LESS_THEN_8);
		}
		if (!Objects.equals(password1, password2)) {
			throw new SomethingWentWrong(Constants.PASSWORDS_ARE_DIFFERENT);
		}
	}

}
