package korn03.tradeguardserver.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LiqJobSubmissionMessage extends JobSubmissionMessage {
    private List<String> excludeCoins;
    private Double proportionPct;
} 