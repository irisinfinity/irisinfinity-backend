package ro.irisinfinity.platform.common.config;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@EnableMethodSecurity
@AutoConfiguration
@AutoConfigureBefore({
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class
})
@RequiredArgsConstructor
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityAutoConfiguration {

    private final SecurityProperties securityProperties;

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthConverter
    ) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(reg -> {
                for (SecurityProperties.PermitRule rule : securityProperties.getPermit()) {
                    String method = rule.getMethod();
                    List<String> patterns = rule.getPatterns();
                    if (patterns == null || patterns.isEmpty()) {
                        continue;
                    }

                    if (method == null || method.isBlank()) {
                        reg.requestMatchers(patterns.toArray(String[]::new)).permitAll();
                    } else {
                        reg.requestMatchers(HttpMethod.valueOf(method.toUpperCase()),
                            patterns.toArray(String[]::new)).permitAll();
                    }
                }
                reg.anyRequest().authenticated();
            })
            .oauth2ResourceServer(
                oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)));
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        String secret = securityProperties.getJwt().getSecret();

        SecretKeySpec key = new SecretKeySpec(secret.getBytes(UTF_8), "HmacSHA256");
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).build();

        var issuerValidator = JwtValidators.createDefaultWithIssuer(
            securityProperties.getJwt().getIssuer());
        OAuth2TokenValidator<Jwt> audienceValidator = jwt ->
            jwt.getAudience() != null && jwt.getAudience()
                .contains(securityProperties.getJwt().getAudience())
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Invalid audience", "aud")
                );

        decoder.setJwtValidator(
            new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator));
        return decoder;
    }

    @Bean
    public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix(securityProperties.getJwt().getAuthorityPrefix());
        authoritiesConverter.setAuthoritiesClaimName(securityProperties.getJwt().getRolesClaim());

        JwtAuthenticationConverter authConverter = new JwtAuthenticationConverter();
        authConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return authConverter;
    }
}
