package ro.irisinfinity.platform.common.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    @NestedConfigurationProperty
    private Jwt jwt = new Jwt();

    private List<PermitRule> permit = new ArrayList<>();

    @Data
    @NoArgsConstructor
    public static class Jwt {

        private String secret;
        private String issuer = "auth-service";
        private String audience = "irisinfinity";
        private String rolesClaim = "roles";
        private String authorityPrefix = "ROLE_";
    }

    @Data
    @NoArgsConstructor
    public static class PermitRule {

        private String method;
        private List<String> patterns = new ArrayList<>();
    }
}