package io.kpen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.zalando.logbook.Logbook;

import java.io.IOException;

import static org.zalando.logbook.Conditions.*;

@SpringBootApplication
public class App {
    public static void main(String[] args) throws IOException {
        System.setProperty("logging.level.org.springframework.web", "DEBUG");
        System.setProperty("spring.http.log-request-details", "true");
        System.setProperty("logging.level.org.zalando.logbook", "TRACE");

        SpringApplication.run(App.class, args);
    }
}
