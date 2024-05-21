package am.devvibes.buyandsell.repository;

import am.devvibes.buyandsell.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {


	Optional<UserEntity> findByEmail(String email);

	boolean existsUserEntityByEmail(String email);


}