package korn03.tradeguardserver.service.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service to manage SSE connections for real-time updates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SseEmitterService {

    private static final long HEARTBEAT_INTERVAL_MS = 30000;
    
    private final ObjectMapper objectMapper;
    
    // Store emitters by user ID
    private final Map<Long, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    /**
     * Create and register a new SSE emitter for a user
     * 
     * @param userId User ID to associate with the emitter
     * @return The created SSE emitter
     */
    public SseEmitter createEmitter(Long userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); //This way emitter lives as long as possible
        userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        log.debug("Added emitter for user {}", userId);

        emitter.onCompletion(() -> {
            log.trace("SSE connection completed for user {}", userId);
            removeEmitter(userId, emitter);
        });
        emitter.onTimeout(() -> {
            log.trace("SSE connection timed out for user {}", userId);
            removeEmitter(userId, emitter);
        });
        emitter.onError(e -> {
            if (e instanceof IOException && e.getMessage().contains("Connection reset by peer")) {
                log.trace("SSE connection closed by client for user {}", userId);
            } else {
                log.debug("SSE connection error for user {}: {}", userId, e.getMessage());
            }
            removeEmitter(userId, emitter);
        });
        
        try {
            log.trace("Sending initial ping event to user {}", userId);
            emitter.send(SseEmitter.event()
                    .name("ping")
                    .data("connected", MediaType.TEXT_PLAIN));
            log.trace("Initial ping event sent successfully to user {}", userId);
        } catch (IOException e) {
            log.debug("Error sending initial ping to user {}: {}", userId, e.getMessage());
            emitter.completeWithError(e);
        }
        
        log.info("Created SSE emitter for user: {}. Total active connections: {}", 
                userId, getActiveConnectionsCount());
        return emitter;
    }
    
    /**
     * Remove an emitter from a user's list
     */
    private void removeEmitter(Long userId, SseEmitter emitter) {
        userEmitters.computeIfPresent(userId, (id, emitters) -> {
            emitters.remove(emitter);
            log.debug("Removed emitter. User {} now has {} emitters", 
                    userId, emitters.size());
            return emitters.isEmpty() ? null : emitters;
        });
        log.info("Removed SSE emitter for user: {}. Total active connections: {}", 
                userId, getActiveConnectionsCount());
    }
    
    /**
     * Send an update to all connected emitters for a specific user
     * 
     * @param userId The user ID to send updates to
     * @param eventType The type of event (used as the SSE event name)
     * @param data The data to send
     */
    public void sendUpdate(Long userId, String eventType, Object data) {
        if (!userEmitters.containsKey(userId)) {
            log.debug("No emitters found for user: {}", userId);
            return;
        }
        if(!Objects.equals(eventType, "heartbeat")) {
            log.info("Sending {} event to {} emitters for user {}", eventType, userEmitters.get(userId).size(), userId);
        }
        List<SseEmitter> deadEmitters = new ArrayList<>();
        
        try {
            String jsonData = objectMapper.writeValueAsString(data);
            log.debug("Serialized data for event {}: {} chars", eventType, jsonData.length());
            
            for (SseEmitter emitter : userEmitters.get(userId)) {
                try {
                    log.debug("Sending {} event to emitter for user {}", eventType, userId);
                    emitter.send(SseEmitter.event()
                            .name(eventType)
                            .data(jsonData, MediaType.TEXT_PLAIN));
                    log.debug("Successfully sent {} event to user {}", eventType, userId);
                } catch (IOException e) {
                    if (e.getMessage().contains("Connection reset by peer")) {
                        log.trace("Client disconnected while sending {} event to user {}", eventType, userId);
                    } else {
                        log.debug("Error sending {} event to user {}: {}", eventType, userId, e.getMessage());
                    }
                    deadEmitters.add(emitter);
                } catch (Exception e) {
                    log.debug("Unexpected error sending {} event to user {}: {}", eventType, userId, e.getMessage());
                    deadEmitters.add(emitter);
                }
            }
        } catch (Exception e) {
            log.error("Error serializing data for SSE: {}", e.getMessage());
        }

        if (!deadEmitters.isEmpty()) {
            log.debug("Cleaning up {} dead emitters for user {}", deadEmitters.size(), userId);
            deadEmitters.forEach(emitter -> removeEmitter(userId, emitter));
        }
    }
    
    /**
     * Send heartbeat events to all connected clients
     */
    @Scheduled(fixedRate = HEARTBEAT_INTERVAL_MS)
    public void sendHeartbeats() {
        if (userEmitters.isEmpty()) {
            return;
        }
        
        int userCount = userEmitters.size();
        int connectionCount = getActiveConnectionsCount();
        log.trace("Sending heartbeats to {} users with {} total connections", userCount, connectionCount);
        
        Map<String, Object> heartbeatData =  new HashMap<>();
        heartbeatData.put("timestamp", Instant.now().toString());
        heartbeatData.put("type", "heartbeat");
        
        userEmitters.forEach((userId, emitters) -> {
            sendUpdate(userId, "heartbeat", heartbeatData);
        });
    }
    
    /**
     * Get the count of active SSE connections
     */
    public int getActiveConnectionsCount() {
        return userEmitters.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    /**
     * Get all active user IDs that have SSE connections
     * 
     * @return Set of user IDs with active connections
     */
    public Set<Long> getActiveUserIds() {
        return new HashSet<>(userEmitters.keySet());
    }
} 