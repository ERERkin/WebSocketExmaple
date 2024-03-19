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
    private final PasswordService passwordService;
    private final NotifyService notifyService;

    public String register(String username, String password) {
        if (isWizardAlreadyExist(username)) {
            return ResponseStatus.WIZARD_NAME_IS_ALREADY_TAKEN;
        }

        String encodedPassword = passwordService.encode(password);
        Wizard newWizard = Wizard.builder().name(username).password(encodedPassword).health(100).build();
        wizardPool.getWizards().add(newWizard);

        return ResponseStatus.REGISTRATION_SUCCESSFUL;
    }

    public String login(WebSocketSession session, String wizardName, String password) throws IOException {
        if (!isWizardIsExist(wizardName)) {
            return ResponseStatus.WIZARD_DOES_NOT_EXIST;
        }
        if (isWizardAlreadyExist(wizardName)) {
            return ResponseStatus.WIZARD_ALREADY_EXISTS;
        }
        Wizard wizard = getWizard(wizardName);
        if (!passwordService.verify(password, wizard.getPassword())) {
            return ResponseStatus.WRONG_PASSWORD;
        }

        if (wizard.getHealth() <= 0) {
            return ResponseStatus.WIZARD_IS_DEAD;
        }

        if (sessionPool.getSessions().stream().noneMatch(s -> s.getSession().getId().equals(session.getId()))) {
            sessionPool.getSessions().add(SessionItem.builder().session(session).username(wizardName).build());
        } else {
            sessionPool.getSessions().stream()
                    .filter(s -> s.getSession().getId().equals(session.getId()))
                    .findFirst()
                    .ifPresent(s -> s.setUsername(wizardName));
        }

        notifyService.notifyAllSessions(session,
                new TextMessage("New wizard has joined the game: " + wizard),
                sessionPool.getSessions().stream()
                        .map(SessionItem::getSession)
                        .toList());

        return ResponseStatus.LOGGED_IN;
    }

    private boolean isWizardAlreadyExist(String username) {
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
        return ResponseStatus.LOGGED_OUT;
    }
}
