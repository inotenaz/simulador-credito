package com.hackathon2025.simulador_credito.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.hackathon2025.simulador_credito.interceptor.TelemetriaInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private TelemetriaInterceptor telemetriaInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(telemetriaInterceptor);
    }
}
