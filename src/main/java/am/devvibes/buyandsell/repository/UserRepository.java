package am.devvibes.buyandsell.repository;

import am.devvibes.buyandsell.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

	Optional<UserEntity> findByEmailAndPassword(String username, String password);

	boolean existsUserEntityByEmail(String email);

}