package com.thaihoc.hotelbooking.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/branches/**")
                .addResourceLocations("file:C:/hotelbooking/uploads/branches/");

        // Handler cho ảnh phòng
        registry.addResourceHandler("/rooms/**")
                .addResourceLocations("file:C:/hotelbooking/uploads/rooms/");

    }
}

