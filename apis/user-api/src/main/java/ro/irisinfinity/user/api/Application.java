package ro.irisinfinity.user.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(
        scanBasePackages = {"ro.irisinfinity.user.api", "ro.irisinfinity.common"}
)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
