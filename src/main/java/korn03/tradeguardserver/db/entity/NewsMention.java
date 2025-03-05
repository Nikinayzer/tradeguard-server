package korn03.tradeguardserver.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "news_mentions", uniqueConstraints = @UniqueConstraint(columnNames = {"coin_symbol", "mention_date"}))
public class NewsMention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "coin_symbol", nullable = false)
    private String coinSymbol;

    @Setter
    @Column(name = "mention_date", nullable = false)
    private LocalDate mentionDate;

    @Setter
    @Column(nullable = false)
    private int mentionCount;

    public NewsMention(String coinSymbol, LocalDate mentionDate, int mentionCount) {
        this.coinSymbol = coinSymbol;
        this.mentionDate = mentionDate;
        this.mentionCount = mentionCount;
    }
}