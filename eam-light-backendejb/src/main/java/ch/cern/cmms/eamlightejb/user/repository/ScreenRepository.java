package ch.cern.cmms.eamlightejb.user.repository;

import ch.cern.cmms.eamlightejb.user.entity.ScreenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScreenRepository extends JpaRepository<ScreenEntity, String> {
}
