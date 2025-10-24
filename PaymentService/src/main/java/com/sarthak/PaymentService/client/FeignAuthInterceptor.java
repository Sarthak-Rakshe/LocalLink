package com.sarthak.PaymentService.client;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class FeignAuthInterceptor {

    @Bean
    public RequestInterceptor requestInterceptor(){
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes)
                    RequestContextHolder.getRequestAttributes();
            if(attributes != null){
                String token = attributes.getRequest().getHeader("Authorization");
                if(token != null){
                    requestTemplate.header("Authorization", token);
                }
            }
        };
    }
}
