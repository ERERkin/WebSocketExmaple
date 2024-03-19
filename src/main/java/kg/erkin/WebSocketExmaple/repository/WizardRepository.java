package kg.erkin.WebSocketExmaple.repository;

import kg.erkin.WebSocketExmaple.model.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

@Repository
public interface WizardRepository extends JpaRepository<WizardEntity, Long> {
    WizardEntity findFirstByName(String name);
    boolean existsByName(String name);
}
