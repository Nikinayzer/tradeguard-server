package korn03.tradeguardserver.model.entity.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import korn03.tradeguardserver.kafka.events.JobEventType;

import java.io.IOException;

// 1) Create a converter
@Converter(autoApply = false)
public class JobEventTypeJsonConverter 
       implements AttributeConverter<JobEventType, String> {

   private static final ObjectMapper MAPPER = new ObjectMapper();

   @Override
   public String convertToDatabaseColumn(JobEventType attribute) {
       if (attribute == null) {
           return null;
       }
       try {
           return MAPPER.writeValueAsString(attribute);
       } catch (JsonProcessingException e) {
           throw new IllegalStateException("Could not serialize JobEventType", e);
       }
   }

   @Override
   public JobEventType convertToEntityAttribute(String dbData) {
       if (dbData == null) {
           return null;
       }
       try {
           return MAPPER.readValue(dbData, JobEventType.class);
       } catch (IOException e) {
           throw new IllegalStateException("Could not deserialize JobEventType", e);
       }
   }
}