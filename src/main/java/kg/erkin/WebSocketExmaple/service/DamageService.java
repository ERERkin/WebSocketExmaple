package kg.erkin.WebSocketExmaple.service;


import java.io.*;
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
    public void damage(WebSocketSession sender, String attacktedWizard) throws IOException {
        WebSocketSession session = getSession(attacktedWizard);
        Wizard wizard = findWizard(attacktedWizard);
        if (session == null) {
            return;
        }
        if (wizard == null) {
            return;
        }
        wizard.setHealth(wizard.getHealth() - 10);

        if (wizard.getHealth() <= 0) {
            sessionPool.getSessions().removeIf(s -> s.getUsername().equals(attacktedWizard));
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
        return sessionPool.getSessions().stream().filter(s -> s.getUsername().equals(username)).findFirst().map(SessionItem::getSession).orElse(null);
    }

    private Wizard findWizard(String username) {
        return wizardPool.getWizards().stream().filter(w -> w.getName().equals(username)).findFirst().orElse(null);
    }
}