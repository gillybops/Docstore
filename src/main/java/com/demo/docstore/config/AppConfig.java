package com.demo.docstore.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * AppConfig — centralizes application-wide configuration beans.
 *
 * @Configuration → tells Spring this class contains @Bean method definitions.
 * Spring calls these methods at startup and registers the return values as
 * managed beans in the application context.
 *
 * Two concerns handled here:
 *   1. CORS (Cross-Origin Resource Sharing) — allows the React frontend
 *      running on a different domain/port to call this API.
 *   2. OpenAPI / Swagger metadata — populates the title, version, and
 *      server URL shown at the top of the Swagger UI page.
 */
@Configuration
public class AppConfig {

    /**
     * CORS Configuration.
     *
     * Why CORS matters: browsers block JavaScript from calling APIs on a
     * different origin (domain + port) by default. This bean tells Spring MVC
     * to add the Access-Control-Allow-* response headers so the browser
     * permits the React app to make API calls.
     *
     * In production, replace "*" with your actual frontend domain, e.g.:
     *   registry.addMapping("/api/**").allowedOrigins("https://docs.gilliannewton.com")
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        // Allow all origins in development; restrict in production
                        .allowedOriginPatterns("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        // Allow cookies/auth headers if you add security later
                        .allowCredentials(false)
                        // Browser caches preflight response for 1 hour
                        .maxAge(3600);
            }
        };
    }

    /**
     * OpenAPI / Swagger UI customization.
     *
     * This populates the info panel at the top of http://localhost:5000/swagger-ui.html
     * with a title, version, description, and the server URL.
     *
     * @Value injects the server.port property from application.properties.
     * This keeps the Swagger server URL accurate regardless of which port
     * the app is running on.
     */
    @Bean
    public OpenAPI customOpenAPI(@Value("${server.port:5000}") String port) {
        return new OpenAPI()
                .info(new Info()
                        .title("DocStore API")
                        .version("1.0.0")
                        .description("""
                                Document Ingest REST API
                                
                                Built with: Java 17 · Spring Boot 3 · Spring Data MongoDB
                                Deployed to: AWS Elastic Beanstalk + AWS DocumentDB
                                
                                This API demonstrates:
                                - MongoDB document persistence via Spring Data repositories
                                - Derived query methods (no SQL / no boilerplate)
                                - Bean Validation on request bodies
                                - Auto-generated Swagger docs via SpringDoc OpenAPI
                                """)
                        .contact(new Contact()
                                .name("DocStore Demo")
                                .url("https://gilliannewton.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + port)
                                .description("Local development"),
                        new Server()
                                .url("http://your-env.elasticbeanstalk.com")
                                .description("AWS Elastic Beanstalk (production)")
                ));
    }
}
