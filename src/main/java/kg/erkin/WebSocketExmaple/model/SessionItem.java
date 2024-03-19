package kg.erkin.WebSocketExmaple.model;

import lombok.*;
import org.springframework.web.socket.*;

@Data
@Builder
public class SessionItem {
    private WebSocketSession session;
    private Long id;
}
