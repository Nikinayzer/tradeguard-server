package korn03.tradeguardserver.service.email;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.mail")
public class EmailConfigProperties {
    private String username;
    private String password;
}
