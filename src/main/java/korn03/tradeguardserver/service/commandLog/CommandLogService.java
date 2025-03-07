package korn03.tradeguardserver.service.commandLog;

import korn03.tradeguardserver.model.entity.CommandLog;
import korn03.tradeguardserver.model.repository.CommandLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CommandLogService {

    private static final Logger logger = LoggerFactory.getLogger(CommandLogService.class);

    private final CommandLogRepository commandLogRepository;

    public CommandLogService(CommandLogRepository commandLogRepository) {
        this.commandLogRepository = commandLogRepository;
    }

    @Async
    public void logCommand(String userId, String command, String parameters,
                           String status, String response, long executionTimeMs,
                           String ipAddress, String userAgent) {
        try {
            CommandLog log = CommandLog.builder()
                    .userId(userId)
                    .command(command)
                    .parameters(parameters)
                    .status(status)
                    .response(response)
                    .executionTimeMs(executionTimeMs)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .timestamp(LocalDateTime.now())
                    .build();

            commandLogRepository.save(log);
        } catch (Exception e) {
            logger.error("Error saving command log: {}", e.getMessage(), e);
        }
    }
}
