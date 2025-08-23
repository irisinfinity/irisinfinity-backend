package ro.irisinfinity.gateway.config;

import org.springframework.cloud.gateway.config.HttpClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.client.HttpClient;


@Configuration
public class NettyMetricsConfig {

    @Bean
    HttpClientCustomizer enableNettyClientMetrics() {
        return (HttpClient http) -> http.metrics(true, uri -> uri);
    }
}