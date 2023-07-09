package com.paddi.service.config;

import com.paddi.service.interceptor.GatewayInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月07日 13:56:13
 */
@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    @Autowired
    private GatewayInterceptor gatewayInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(gatewayInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/v1/system/access")
                .excludePathPatterns("/v1/system/signature")
                .excludePathPatterns("/v1/message/check");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600)
                .allowedHeaders("*");
    }
}
