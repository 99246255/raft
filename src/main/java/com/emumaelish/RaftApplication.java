package com.emumaelish;

import com.emumaelish.config.ApplicationReadyListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RaftApplication {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(RaftApplication.class);
        springApplication.addListeners(new ApplicationReadyListener());
        springApplication.run(args);
    }
}
