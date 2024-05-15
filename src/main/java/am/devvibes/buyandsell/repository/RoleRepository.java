package am.devvibes.buyandsell.repository;

import am.devvibes.buyandsell.entity.RoleEntity;
import am.devvibes.buyandsell.util.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Integer> {

	RoleEntity findByRole(Role role);

}