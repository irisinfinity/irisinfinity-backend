package ro.irisinfinity.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(
    scanBasePackages = {"ro.irisinfinity.auth", "ro.irisinfinity.platform.common"}
)
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

}
