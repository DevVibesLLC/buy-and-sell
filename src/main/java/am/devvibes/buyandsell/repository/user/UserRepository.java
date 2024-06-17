package am.devvibes.buyandsell.repository.user;

import am.devvibes.buyandsell.entity.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {


	Optional<UserEntity> findByEmail(String email);

	boolean existsUserEntityByEmail(String email);


}