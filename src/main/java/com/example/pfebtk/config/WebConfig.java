package com.example.pfebtk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.image.dir}")
    private String imageDir;

    @Value("${file.convention.dir}")
    private String conventionDir;

    @Value("${file.convention.signed.dir}")
    private String conventionSignedDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // ── chemin absolu ─────────────────────────────────────────
        String base = System.getProperty("user.dir") + "/";

        // ── images ────────────────────────────────────────────────
        registry.addResourceHandler("/uploads/images/**")
                .addResourceLocations("file:" + base + imageDir + "/");

        // ── conventions ───────────────────────────────────────────
        registry.addResourceHandler("/uploads/conventions/**")
                .addResourceLocations("file:" + base + conventionDir + "/");

        // ── conventions signées ───────────────────────────────────
        registry.addResourceHandler("/uploads/conventions-signed/**")
                .addResourceLocations("file:" + base + conventionSignedDir + "/");
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
