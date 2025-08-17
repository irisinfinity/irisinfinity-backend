package ro.irisinfinity.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication(
    scanBasePackages = {"ro.irisinfinity.auth", "ro.irisinfinity.platform.common"}
)
@EnableFeignClients(basePackages = "ro.irisinfinity.auth.client")
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

}
