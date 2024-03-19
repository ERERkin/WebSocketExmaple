package kg.erkin.WebSocketExmaple.config;

import kg.erkin.WebSocketExmaple.handler.*;
import lombok.*;
import org.springframework.context.annotation.*;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final WebSocketHandler webSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws");
    }
}
