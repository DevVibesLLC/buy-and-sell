package am.devvibes.buyandsell.service.account.impl;

import am.devvibes.buyandsell.exception.NotFoundException;
import am.devvibes.buyandsell.exception.VerificationException;
import am.devvibes.buyandsell.mapper.UserMapper;
import am.devvibes.buyandsell.model.dto.user.UserResponseDto;
import am.devvibes.buyandsell.model.entity.UserEntity;
import am.devvibes.buyandsell.repository.UserRepository;
import am.devvibes.buyandsell.service.account.AccountService;
import am.devvibes.buyandsell.util.ExceptionConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@RequiredArgsConstructor
@Service
public class AccountServiceImpl implements AccountService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;

	@Override
	@Transactional
	public UserResponseDto login(String email, String password) {
		UserEntity userEntity = userRepository.findByEmailAndPassword(email, password)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.USER_NOT_FOUND));
		return userMapper.mapEntityToDto(userEntity);
	}

	@Override
	@Transactional
	public void verifyAccount(String email, String verificationCode) {
		UserEntity userEntity = userRepository.findByEmail(email)
				.orElseThrow(() -> new NotFoundException(ExceptionConstants.USER_NOT_FOUND));

		if (!Objects.equals(verificationCode, userEntity.getVerificationCode())) {
			throw new VerificationException(ExceptionConstants.INCORRECT_CODE);
		}
		userEntity.setIsVerified(true);
		userRepository.save(userEntity);
	}
}
