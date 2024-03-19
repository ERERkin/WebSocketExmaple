package kg.erkin.WebSocketExmaple.service;

import kg.erkin.WebSocketExmaple.model.*;
import kg.erkin.WebSocketExmaple.model.entity.*;
import kg.erkin.WebSocketExmaple.repository.*;
import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

@Service
@RequiredArgsConstructor
public class WizardService {
    private final WizardRepository wizardRepository;

    public Wizard getById(Long id) {
        if (id == null) {
            return null;
        }
        var wizardEntity = wizardRepository.findById(id).orElse(null);
        if (wizardEntity == null) {
            return null;
        }
        return Wizard.builder()
                .id(wizardEntity.getId())
                .name(wizardEntity.getName())
                .health(wizardEntity.getHealth())
                .password(wizardEntity.getPassword())
                .build();
    }

    @Transactional
    public Wizard save(Wizard wizard) {
        var wizardEntity = WizardEntity.builder()
                .id(wizard.getId())
                .name(wizard.getName())
                .password(wizard.getPassword())
                .health(wizard.getHealth())
                .password(wizard.getPassword())
                .build();
        wizardEntity = wizardRepository.save(wizardEntity);
        return Wizard.builder()
                .id(wizardEntity.getId())
                .name(wizardEntity.getName())
                .health(wizardEntity.getHealth())
                .password(wizardEntity.getPassword())
                .build();
    }

    public Wizard getByName(String wizardName) {
        WizardEntity wizardEntity = wizardRepository.findFirstByName(wizardName);
        if (wizardEntity == null) {
            return null;
        }
        return Wizard.builder()
                .id(wizardEntity.getId())
                .name(wizardEntity.getName())
                .health(wizardEntity.getHealth())
                .password(wizardEntity.getPassword())
                .build();
    }

    public boolean isWizardAlreadyExist(String wizardName) {
        return wizardRepository.existsByName(wizardName);
    }
}
