package am.devvibes.buyandsell.service.user.impl;

import am.devvibes.buyandsell.dto.user.UserRequestDto;
import am.devvibes.buyandsell.dto.user.UserResponseDto;
import am.devvibes.buyandsell.entity.MyUserPrincipal;
import am.devvibes.buyandsell.entity.UserEntity;
import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.exception.SomethingWentWrongException;
import am.devvibes.buyandsell.mapper.UserMapper;
import am.devvibes.buyandsell.repository.UserRepository;
import am.devvibes.buyandsell.service.user.UserService;
import am.devvibes.buyandsell.util.ExceptionConstants;
import am.devvibes.buyandsell.util.RandomGenerator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserDetailsService, UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String email) {
		var appUser = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));
		return new MyUserPrincipal(appUser);

	}

	@Override
	@Transactional
	public UserResponseDto saveUser(UserRequestDto signUpDto) {
		validateUser(signUpDto);
		UserEntity userEntity = userMapper.mapDtoToEntity(signUpDto);
		UserEntity savedUser = setVerificationCodeAndSendMail(userEntity);
		return userMapper.mapEntityToDto(savedUser);
	}

	@Override
	@Transactional
	public UserResponseDto findUserById(Long id) {
		UserEntity user =
				userRepository.findById(id).orElseThrow(() -> new NotFoundException(ExceptionConstants.USER_NOT_FOUND));
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
	@Transactional
	public UserResponseDto changePassword(String email, String newPassword, String repeatNewPassword) {

		UserEntity userEntity = userRepository.findByEmail(email)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.USER_NOT_FOUND));
		//TODO here must be mail sending.
		comparePasswordsAndValidate(newPassword, repeatNewPassword);
		userEntity.setPassword(passwordEncoder.encode(newPassword));
		return userMapper.mapEntityToDto(userRepository.save(userEntity));
	}

	private UserEntity setVerificationCodeAndSendMail(UserEntity userEntity) {
		userEntity.setVerificationCode(RandomGenerator.generateNumericString());
		/*emailService.sendMessage(userEntity.getEmail(), "Verification",
				"Your verification code is: " + userEntity.getVerificationCode());*/
		return userRepository.save(userEntity);
	}

	private void validateUser(UserRequestDto signUpDto) {
		if (userRepository.existsUserEntityByEmail(signUpDto.getEmail())) {
			throw new SomethingWentWrongException(ExceptionConstants.USER_WITH_THIS_EMAIL_ALREADY_EXISTS);
		}
		comparePasswordsAndValidate(signUpDto.getPassword(), signUpDto.getRepeatPassword());
		EmailValidator.getInstance().isValid(signUpDto.getEmail());
	}

	private void comparePasswordsAndValidate(String password1, String password2) {
		if (password1.length() < 8 || password2.length() < 8) {
			throw new SomethingWentWrongException(ExceptionConstants.PASSWORD_LENGTH_IS_LESS_THEN_8);
		}
		if (!Objects.equals(password1, password2)) {
			throw new SomethingWentWrongException(ExceptionConstants.PASSWORDS_ARE_DIFFERENT);
		}
	}

}
