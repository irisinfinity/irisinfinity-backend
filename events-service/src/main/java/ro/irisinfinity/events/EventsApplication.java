package ro.irisinfinity.events;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(
    scanBasePackages = {"ro.irisinfinity.events", "ro.irisinfinity.platform.common"}
)
@EnableFeignClients(basePackages = "ro.irisinfinity.events.client")
public class EventsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventsApplication.class, args);
    }

}
