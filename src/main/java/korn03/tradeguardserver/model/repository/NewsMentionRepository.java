package korn03.tradeguardserver.model.repository;

import korn03.tradeguardserver.model.entity.NewsMention;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface NewsMentionRepository extends JpaRepository<NewsMention, Long> {
    Optional<NewsMention> findByCoinSymbolAndMentionDate(String coinSymbol, LocalDate mentionDate);
    List<NewsMention> findByCoinSymbolOrderByMentionDateDesc(String coinSymbol);
}