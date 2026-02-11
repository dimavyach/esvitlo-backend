package site.esvitlo.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EsvitloBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EsvitloBackendApplication.class, args);
    }

}
