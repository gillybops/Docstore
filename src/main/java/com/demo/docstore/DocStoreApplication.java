package com.demo.docstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DocStoreApplication — entry point for the Spring Boot application.
 *
 * @SpringBootApplication is a convenience annotation that combines:
 *   - @Configuration      → marks this class as a source of bean definitions
 *   - @EnableAutoConfiguration → tells Spring Boot to auto-configure beans
 *     based on dependencies on the classpath (e.g. MongoDB, Web, Validation)
 *   - @ComponentScan      → scans this package and sub-packages for
 *     @Component, @Service, @Repository, @Controller, etc.
 *
 * When this runs on Elastic Beanstalk, the embedded Tomcat server starts
 * on the port defined in application.properties (default 5000).
 */
@SpringBootApplication
public class DocStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocStoreApplication.class, args);
    }
}
