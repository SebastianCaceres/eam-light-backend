package ch.cern.cmms.eamlightejb.user.repository;

import ch.cern.cmms.eamlightejb.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
}
