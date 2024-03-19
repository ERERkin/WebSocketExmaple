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
        TextMessage message = new TextMessage("Connection established");
        session.sendMessage(message);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        List<String> command = Arrays.stream(message.getPayload().split(" ")).toList();
        TextMessage response = new TextMessage("Your message was received");
        switch (command.get(0).toUpperCase()) {
            case "LOGIN":
                String loginResponse = authService.login(session, command.get(1));
                notifyService.notify(session, new TextMessage(loginResponse));
                if(loginResponse.equals("You are logged in")) {
                    String wizardsResponse = authService.getWizards(command.get(1));
                    session.sendMessage(new TextMessage(wizardsResponse));
                }
                break;
            case "ATTACK":
                damageService.damage(session, command.get(1));
                break;
            case "LOGOUT":
                response = new TextMessage(authService.logout(session));
                notifyService.notify(session, response);
                session.close();
                break;
            default:
                // Do something
                break;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessionPool.removeSession(session);
    }
}