package am.devvibes.buyandsell.service.account;

import am.devvibes.buyandsell.model.dto.user.UserResponseDto;

public interface AccountService {

	UserResponseDto login(String email, String password);

	void verifyAccount(String email, String verificationCode);

}
