package kg.erkin.WebSocketExmaple.handler;

import java.io.*;
import java.util.*;
import kg.erkin.WebSocketExmaple.model.*;
import kg.erkin.WebSocketExmaple.service.*;
import kg.erkin.WebSocketExmaple.util.*;
import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.*;

@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {
    private final SessionPool sessionPool;
    private final WizardPool wizardPool;
    private final AuthService authService;
    private final DamageService damageService;
    private final NotifyService notifyService;


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        if (!sessionPool.getSessions().stream().anyMatch(s -> s.getSession().getId().equals(session.getId()))) {
            sessionPool.getSessions().add(SessionItem.builder().session(session).build());
        }
        TextMessage message = new TextMessage(ResponseStatus.CONNECTION_ESTABLISHED);
        session.sendMessage(message);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        List<String> command = Arrays.stream(message.getPayload().split(" ")).toList();
        TextMessage response = new TextMessage("Your message was received");
        switch (command.get(0).toUpperCase()) {
            case "REGISTER":
                if (command.size() < 3) {
                    notifyService.notify(session, new TextMessage("Error: Invalid REGISTER command. Usage: register {wizard name} {password}"));
                    return;
                }
                response = new TextMessage(authService.register(command.get(1), command.get(2)));
                notifyService.notify(session, response);
                break;
            case "LOGIN":
                if (command.size() < 3) {
                    notifyService.notify(session, new TextMessage("Error: Invalid LOGIN command. Usage: login {wizard name} {password}"));
                    return;
                }
                String loginResponse = authService.login(session, command.get(1), command.get(2));
                notifyService.notify(session, new TextMessage(loginResponse));
                if (loginResponse.equals(ResponseStatus.LOGGED_IN)) {
                    String wizardsResponse = authService.getWizards(command.get(1));
                    session.sendMessage(new TextMessage(wizardsResponse));
                }
                break;
            case "ATTACK":
                if (command.size() < 2) {
                    notifyService.notify(session, new TextMessage("Error: Invalid ATTACK command. Usage: attack {wizard name}"));
                    return;
                }
                damageService.damage(session, command.get(1));
                break;
            case "LOGOUT":
                if (command.size() > 1) {
                    notifyService.notify(session, new TextMessage("Error: Invalid LOGOUT command. Usage: logout"));
                    return;
                }
                response = new TextMessage(authService.logout(session));
                notifyService.notify(session, response);
                session.close();
                break;
            default:
                notifyService.notify(session, new TextMessage("Unknown command"));
                break;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessionPool.removeSession(session);
    }
}