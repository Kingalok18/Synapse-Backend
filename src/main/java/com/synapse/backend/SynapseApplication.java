package com.synapse.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SynapseApplication {

    public static void main(String[] String) {
        SpringApplication.run(SynapseApplication.class, String);
    }
}
