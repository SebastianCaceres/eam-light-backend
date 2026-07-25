package ch.cern.cmms.eamlightejb.watchers.repository;

import ch.cern.cmms.eamlightejb.watchers.entity.WatcherEntity;
import ch.cern.cmms.eamlightejb.watchers.entity.WatcherId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WatcherRepository extends JpaRepository<WatcherEntity, WatcherId> {
    List<WatcherEntity> findByWorkOrderCode(String workOrderCode);
    void deleteByWorkOrderCodeAndPerson(String workOrderCode, String person);
}
