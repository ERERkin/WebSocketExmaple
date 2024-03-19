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
    private final WizardService wizardService;

    public String register(String username, String password) {
        if (isWizardAlreadyExist(username)) {
            return ResponseStatus.WIZARD_NAME_IS_ALREADY_TAKEN;
        }

        String encodedPassword = passwordService.encode(password);
        Wizard newWizard = Wizard.builder().name(username).password(encodedPassword).health(100).build();
        newWizard = wizardService.save(newWizard);

        return ResponseStatus.REGISTRATION_SUCCESSFUL;
    }

    public String login(WebSocketSession session, String wizardName, String password) throws IOException {
        if (!wizardService.isWizardAlreadyExist(wizardName)) {
            return ResponseStatus.WIZARD_DOES_NOT_EXIST;
        }
        if (isWizardAlreadyExist(wizardName)) {
            return ResponseStatus.WIZARD_ALREADY_EXISTS;
        }
        Wizard wizard = wizardService.getByName(wizardName);
        if (!passwordService.verify(password, wizard.getPassword())) {
            return ResponseStatus.WRONG_PASSWORD;
        }

        if (wizard.getHealth() <= 0) {
            return ResponseStatus.WIZARD_IS_DEAD;
        }

        if (sessionPool.getSessions().stream().noneMatch(s -> s.getSession().getId().equals(session.getId()))) {
            sessionPool.getSessions().add(SessionItem.builder().session(session).id(wizard.getId()).build());
        } else {
            sessionPool.getSessions().stream()
                    .filter(s -> s.getSession().getId().equals(session.getId()))
                    .findFirst()
                    .ifPresent(s -> s.setId(wizard.getId()));
        }

        notifyService.notifyAllSessions(session,
                new TextMessage("New wizard has joined the game: " + wizard),
                sessionPool.getSessions().stream()
                        .map(SessionItem::getSession)
                        .toList());

        return ResponseStatus.LOGGED_IN;
    }

    private boolean isWizardAlreadyExist(String username) {
        Wizard wizard = wizardService.getByName(username);
        if (wizard == null) {
            return false;
        }
        return sessionPool.getSessions().stream().anyMatch(s -> Objects.equals(s.getId(),wizard.getId()));
    }

    public String getWizards(String wizardName) {
        Wizard wizard = wizardService.getByName(wizardName);
        return sessionPool.getSessions().stream()
                .filter(s -> !Objects.equals(s.getId(), wizard.getId()))
                .filter(s -> s.getId() != null)
                .map(sessionItem -> wizardService
                        .getById(sessionItem.getId())
                        .toString())
                .collect(Collectors.joining("\n"));
    }

    public String logout(WebSocketSession session) throws IOException {
        SessionItem sessionItem = sessionPool.getSessions().stream()
                .filter(s -> s.getSession().getId().equals(session.getId()))
                .findFirst()
                .orElse(null);

        if (sessionItem != null) {
            Wizard wizard = wizardService.getById(sessionItem.getId());
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
