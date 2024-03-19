package kg.erkin.WebSocketExmaple.util;

import java.util.*;
import kg.erkin.WebSocketExmaple.model.*;
import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.web.socket.*;

@Component
@Getter
public class SessionPool {
    private List<SessionItem> sessions = new ArrayList<>();

    public void addSession(WebSocketSession session, Long id) {
        sessions.add(SessionItem.builder().session(session).id(id).build());
    }

    public void removeSession(WebSocketSession session) {
        sessions.removeIf(s -> s.getSession().getId().equals(session.getId()));
    }
}
