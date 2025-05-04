package korn03.tradeguardserver.service.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public CacheService(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public <T> void storeInCache(String key, T data, Duration ttl) {
        try {
            String jsonData = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(key, jsonData, ttl.toSeconds(), TimeUnit.SECONDS);
            log.debug("Stored in cache: {} with TTL: {} seconds", key, ttl.toSeconds());
        } catch (JsonProcessingException e) {
            log.error("Failed to store in cache: {}", key, e);
            throw new RuntimeException("Failed to store in cache: " + key, e);
        }
    }

    public <T> T getFromCache(String key, Class<T> type) {
        try {
            String jsonData = redisTemplate.opsForValue().get(key);
            if (jsonData == null) {
                log.debug("Key not found in cache: {}", key);
                return null;
            }
            if (jsonData.isEmpty()) {
                log.debug("Empty string found in cache for key: {}", key);
                return null;
            }
            
            try {
                T result = objectMapper.readValue(jsonData, type);
                log.debug("Retrieved from cache: {} (type: {})", key, type.getSimpleName());
                return result;
            } catch (Exception e) {
                log.warn("Failed to deserialize data for key {}: {}", key, e.getMessage());
                return null;
            }
        } catch (Exception e) {
            log.error("Error accessing cache for key: {}", key, e);
            return null;
        }
    }
    
    /**
     * Get all keys matching a pattern
     */
    public Set<String> getKeys(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            log.debug("Found {} keys matching pattern: {}", keys.size(), pattern);
            return keys;
        } catch (Exception e) {
            log.error("Error getting keys for pattern: {}", pattern, e);
            return Set.of();
        }
    }
    
    /**
     * Check if a key exists in the cache
     */
    public boolean keyExists(String key) {
        return redisTemplate.hasKey(key);
    }
}
