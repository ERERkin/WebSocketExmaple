package kg.erkin.WebSocketExmaple.service;

import java.io.*;
import java.util.*;
import org.springframework.stereotype.*;
import org.springframework.web.socket.*;

@Service
public class NotifyService {
    public void notifyAllSessions(WebSocketSession sender, TextMessage message, List<WebSocketSession> sessions) throws IOException {
        for (WebSocketSession webSocketSession : sessions) {
            if (webSocketSession.isOpen() && !webSocketSession.getId().equals(sender.getId())) {
                webSocketSession.sendMessage(message);
            }
        }
    }

    public void notify(WebSocketSession session, TextMessage message) throws IOException {
        session.sendMessage(message);
    }
}
