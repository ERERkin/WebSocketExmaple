package kg.erkin.WebSocketExmaple.util;

import java.util.*;
import kg.erkin.WebSocketExmaple.model.*;
import lombok.*;
import org.springframework.stereotype.*;

@Component
@Getter
public class WizardPool {
    private List<Wizard> wizards = new ArrayList<>();
}
