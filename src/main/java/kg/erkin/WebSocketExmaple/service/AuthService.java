package kg.erkin.WebSocketExmaple.service;

import java.io.*;
import java.util.*;
import java.util.stream.*;
import kg.erkin.WebSocketExmaple.model.*;
import kg.erkin.WebSocketExmaple.util.*;
import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.web.socket.*;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final SessionPool sessionPool;
    private final WizardPool wizardPool;
    private final NotifyService notifyService;

    public String login(WebSocketSession session, String wizardName) throws IOException {
        if (isUsernameTaken(wizardName)) {
            return "Username is already taken";
        }
        if (!sessionPool.getSessions().stream().anyMatch(s -> s.getSession().getId().equals(session.getId()))) {
            sessionPool.getSessions().add(SessionItem.builder().session(session).username(wizardName).build());
        } else {
            sessionPool.getSessions().stream()
                    .filter(s -> s.getSession().getId().equals(session.getId()))
                    .findFirst()
                    .ifPresent(s -> s.setUsername(wizardName));
        }
        if (!isWizardIsExist(wizardName)) {
            wizardPool.getWizards().add(Wizard.builder().name(wizardName).health(100).build());
        }

        Wizard wizard = getWizard(wizardName);

        notifyService.notifyAllSessions(session,
                new TextMessage("New wizard has joined the game: " + wizard),
                sessionPool.getSessions().stream()
                        .map(SessionItem::getSession)
                        .toList());

        return "You are logged in";
    }

    private boolean isUsernameTaken(String username) {
        return sessionPool.getSessions().stream().anyMatch(s -> Objects.equals(s.getUsername(),username));
    }

    private boolean isWizardIsExist(String wizardName) {
        return wizardPool.getWizards().stream().anyMatch(w -> w.getName().equals(wizardName));
    }

    public String getWizards(String wizardName) {
        return sessionPool.getSessions().stream()
                .filter(s -> !Objects.equals(s.getUsername(), wizardName))
                .map(sessionItem -> getWizard(sessionItem.getUsername()).toString())
                .collect(Collectors.joining("\n"));
    }

    private Wizard getWizard(String wizardName) {
        return wizardPool.getWizards().stream()
                .filter(w -> w.getName().equals(wizardName))
                .findFirst().orElse(null);
    }

    public String logout(WebSocketSession session) throws IOException {
        SessionItem sessionItem = sessionPool.getSessions().stream()
                .filter(s -> s.getSession().getId().equals(session.getId()))
                .findFirst()
                .orElse(null);

        if (sessionItem != null) {
            Wizard wizard = getWizard(sessionItem.getUsername());
            String logoutMessage = wizard + " has logged out";
            notifyService.notifyAllSessions(session, new TextMessage(logoutMessage),
                    sessionPool.getSessions().stream()
                            .map(SessionItem::getSession)
                            .toList());
        }

        sessionPool.removeSession(session);
        return "You are logged out";
    }
}
