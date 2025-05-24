package korn03.tradeguardserver.config;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import korn03.tradeguardserver.kafka.events.equity.EquityKafkaDTO;
import korn03.tradeguardserver.kafka.events.jobUpdates.JobSubmissionKafkaDTO;
import korn03.tradeguardserver.kafka.events.position.PositionKafkaDTO;
import korn03.tradeguardserver.kafka.events.jobUpdates.JobEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for Kafka settings.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topic.job-updates}")
    private String jobUpdatesTopic;

    @Value("${kafka.topic.job-submissions}")
    private String jobSubmissionsTopic;
    
    @Value("${kafka.topic.position-updates}")
    private String positionUpdatesTopic;
    
    @Value("${kafka.topic.clean-position-updates}")
    private String cleanPositionUpdatesTopic;
    
    @Value("${kafka.topic.equity}")
    private String equityUpdatesTopic;

    /**
     * Creates Kafka Admin client for managing topics.
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    /**
     * Custom ObjectMapper for Kafka serialization/deserialization.
     * @return ObjectMapper
     */
    @Bean
    public ObjectMapper kafkaObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ISO 8601
        return mapper;
    }

    /**
     * Defines the jobs topic with appropriate configurations.
     */
    @Bean
    public NewTopic jobEventsTopic() {
        return new NewTopic(jobUpdatesTopic, 1, (short) 1); // Single partition, single replica for development
    }

    @Bean
    public NewTopic jobSubmissionsTopic() {
        return new NewTopic(jobSubmissionsTopic, 1, (short) 1); // Single partition, single replica for development
    }
    
    @Bean
    public NewTopic positionUpdatesTopic() {
        return new NewTopic(positionUpdatesTopic, 1, (short) 1);
    }
    
    @Bean
    public NewTopic cleanPositionUpdatesTopic() {
        return new NewTopic(cleanPositionUpdatesTopic, 1, (short) 1);
    }
    
    @Bean
    public NewTopic equityUpdatesTopic() {
        return new NewTopic(equityUpdatesTopic, 1, (short) 1);
    }

    /**
     * Generic Kafka Template - Can be used for different message types
     */
    @Bean
    public <T> KafkaTemplate<String, T> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Specific Kafka Template for JobSubmissionMessage
     */
    @Bean
    public KafkaTemplate<String, JobSubmissionKafkaDTO> jobSubmissionKafkaTemplate(ObjectMapper kafkaObjectMapper) {
        JsonSerializer<JobSubmissionKafkaDTO> serializer = new JsonSerializer<>(kafkaObjectMapper);

        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(
                defaultKafkaProducerProps(),
                new StringSerializer(),
                serializer
        ));
    }

    /**
     * Kafka Listener Factory for JobEventMessage - Uses generic consumerFactory.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, JobEventMessage> jobEventListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, JobEventMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(JobEventMessage.class));
        factory.setCommonErrorHandler(kafkaErrorHandler());
        return factory;
    }
    
    /**
     * Kafka Listener Factory for Position - Uses generic consumerFactory.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PositionKafkaDTO> positionListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PositionKafkaDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(PositionKafkaDTO.class));
        factory.setCommonErrorHandler(kafkaErrorHandler());
        return factory;
    }
    
    /**
     * Kafka Listener Factory for Equity - Uses generic consumerFactory.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EquityKafkaDTO> equityListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, EquityKafkaDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(EquityKafkaDTO.class));
        factory.setCommonErrorHandler(kafkaErrorHandler());
        return factory;
    }

    /**
     * Configures the error handler for Kafka consumers.
     */
    @Bean
    public CommonErrorHandler kafkaErrorHandler() {
        return new DefaultErrorHandler(new FixedBackOff(1000L, 3));
    }

    /**
     * Generic Producer Factory. Uses default Kafka Producer properties, see {@code defaultKafkaProducerProps()}.
     */
    public <T> ProducerFactory<String, T> producerFactory() {
        Map<String, Object> props = defaultKafkaProducerProps();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * Default Kafka Producer properties
     * @return Map of properties
     */
    private Map<String, Object> defaultKafkaProducerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "none");
        return props;
    }

    /**
     * Generic Kafka Consumer Factory
     */
    public <T> ConsumerFactory<String, T> consumerFactory(Class<T> targetType) {
        JsonDeserializer<T> jsonDeserializer = new JsonDeserializer<>(targetType);
        jsonDeserializer.setUseTypeHeaders(false);
        jsonDeserializer.addTrustedPackages("*");
        ErrorHandlingDeserializer<T> errorHandlingDeserializer = new ErrorHandlingDeserializer<>(jsonDeserializer);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "tradeguard-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1024");
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "500");
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, "1048576");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), jsonDeserializer, errorHandlingDeserializer.isForKey());
    }
}
