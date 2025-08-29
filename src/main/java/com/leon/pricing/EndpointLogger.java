package com.leon.pricing;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Component
public class EndpointLogger
{
    private final RequestMappingHandlerMapping handlerMapping;

    public EndpointLogger(@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping)
    {
        this.handlerMapping = handlerMapping;
    }
}
