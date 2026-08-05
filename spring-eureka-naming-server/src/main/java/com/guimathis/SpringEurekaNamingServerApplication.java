package com.guimathis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class SpringEurekaNamingServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringEurekaNamingServerApplication.class, args);
    }

}
