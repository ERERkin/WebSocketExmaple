package kg.erkin.WebSocketExmaple.model.entity;

import jakarta.persistence.*;
import static jakarta.persistence.GenerationType.IDENTITY;
import lombok.*;

@Data
@Builder
@Entity
@Table(name = "wizard")
@NoArgsConstructor
@AllArgsConstructor
public class WizardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "password")
    private String password;

    @Column(name = "health")
    private Integer health;
}
