package ro.irisinfinity.auth.config;

import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@NoArgsConstructor
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    @NotBlank
    private String secret;

    @NotBlank
    private String refreshSecret;

    @NotBlank
    private String issuer;

    @NotBlank
    private String audience;

    @DurationUnit(ChronoUnit.MINUTES)
    private Duration accessTtlMinutes = Duration.ofMinutes(15);

    @DurationUnit(ChronoUnit.DAYS)
    private Duration refreshTtlDays = Duration.ofDays(7);

}