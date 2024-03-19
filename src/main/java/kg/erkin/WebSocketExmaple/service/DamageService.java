package kg.erkin.WebSocketExmaple.service;


import java.io.*;
import java.util.*;
import kg.erkin.WebSocketExmaple.model.*;
import kg.erkin.WebSocketExmaple.util.*;
import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.web.socket.*;

@Service
@RequiredArgsConstructor
public class DamageService {
    private final SessionPool sessionPool;
    private final WizardPool wizardPool;
    private final NotifyService notifyService;
    private final WizardService wizardService;
    public void damage(WebSocketSession sender, String attacktedWizard) throws IOException {
        WebSocketSession session = getSession(attacktedWizard);
        Wizard wizard = wizardService.getByName(attacktedWizard);
        if (session == null) {
            return;
        }
        if (wizard == null) {
            return;
        }
        wizard.setHealth(wizard.getHealth() - 10);
        wizardService.save(wizard);

        if (wizard.getHealth() <= 0) {
            Wizard finalWizard = wizard;
            sessionPool.getSessions().removeIf(s -> s.getId().equals(finalWizard.getId()));
            notifyService.notifyAllSessions(session,
                    new TextMessage("Wizard " + attacktedWizard + " has been killed"),
                    sessionPool.getSessions().stream()
                            .map(SessionItem::getSession)
                            .toList());
            notifyService.notify(session,
                    new TextMessage("You have been killed"));
            session.close();
            return;
        }

        notifyService.notifyAllSessions(session,
                new TextMessage("Wizard " + attacktedWizard + " has been attacked. Health: " + wizard.getHealth()),
                sessionPool.getSessions().stream()
                        .map(SessionItem::getSession)
                        .toList());
        notifyService.notify(session,
                new TextMessage("You have been attacked. Health: " + wizard.getHealth()));
    }

    private WebSocketSession getSession(String username) {
        Wizard wizard = wizardService.getByName(username);
        return sessionPool.getSessions().stream()
                .filter(s -> Objects.equals(s.getId(),wizard.getId()))
                .findFirst()
                .map(SessionItem::getSession)
                .orElse(null);
    }

}