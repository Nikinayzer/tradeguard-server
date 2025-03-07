package korn03.tradeguardserver.service.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class CacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public CacheService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public <T> void storeInCache(String key, T data, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(data), ttl.toSeconds(), TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to store in cache: " + key, e);
        }
    }

    public <T> T getFromCache(String key, Class<T> type) {
        try {
            String jsonData = redisTemplate.opsForValue().get(key);
            return objectMapper.readValue(jsonData, type);
        } catch (Exception e) {
            return null;
        }
    }
}
