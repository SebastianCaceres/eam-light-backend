package ch.cern.cmms.eamlightejb.base.repository;

import ch.cern.cmms.eamlightejb.base.entity.CommentEntity;
import ch.cern.cmms.eamlightejb.base.entity.CommentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, CommentId> {
    List<CommentEntity> findByEntityCodeAndKeyValueOrderByLineAsc(String entityCode, String keyValue);
}
