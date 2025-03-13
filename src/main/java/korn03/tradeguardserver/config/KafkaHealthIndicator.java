package korn03.tradeguardserver.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Health indicator for Kafka integration.
 * This class checks Kafka connectivity and reports health status to Spring Boot Actuator.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaHealthIndicator implements HealthIndicator {

    private final KafkaAdmin kafkaAdmin;
    
    @Override
    public Health health() {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            DescribeClusterOptions options = new DescribeClusterOptions().timeoutMs(5000);
            DescribeClusterResult cluster = adminClient.describeCluster(options);

            String clusterId = cluster.clusterId().get(5, TimeUnit.SECONDS);
            int nodeCount = cluster.nodes().get(5, TimeUnit.SECONDS).size();
            
            if (nodeCount > 0) {
                return Health.up()
                        .withDetail("clusterId", clusterId)
                        .withDetail("nodeCount", nodeCount)
                        .build();
            } else {
                return Health.down()
                        .withDetail("error", "No Kafka nodes available")
                        .build();
            }
        } catch (Exception e) {
            log.warn("Kafka health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
} 