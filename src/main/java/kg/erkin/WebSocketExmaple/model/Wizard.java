package kg.erkin.WebSocketExmaple.model;

import lombok.*;

@Data
@Builder
public class Wizard {
    private Long id;
    private String name;
    private String password;
    private Integer health;

    @Override
    public String toString() {
        return "Wizard{" +
                "name='" + name + '\'' +
                ", health=" + health +
                '}';
    }
}
