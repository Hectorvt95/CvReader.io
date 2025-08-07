
package com.hmvt.CvReader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.hmvt.CvReader.repository")
@EntityScan(basePackages = "com.hmvt.CvReader.model")

public class SimpleRestServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SimpleRestServiceApplication.class, args);
    }

}


