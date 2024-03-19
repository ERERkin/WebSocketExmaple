package kg.erkin.WebSocketExmaple.model;

import lombok.*;

@Data
@Builder
public class Wizard {
    private String name;
    private String password;
    private int health;

    @Override
    public String toString() {
        return "Wizard{" +
                "name='" + name + '\'' +
                ", health=" + health +
                '}';
    }
}
