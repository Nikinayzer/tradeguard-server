package korn03.tradeguardserver.model.entity.service;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "push_tokens")
public class PushToken {
    @Id //because user can have multiple tokens. Anyway, this code is PoC
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false)
    private Long userId;

    @Setter
    @Column(nullable = false)
    private String token;

}
