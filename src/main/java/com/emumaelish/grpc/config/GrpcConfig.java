package com.emumaelish.grpc.config;

import com.emumaelish.config.GrpcProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * grpc配置需要配置节点信息，参考grpc-rpc-client-spring-boot-starter
 */
@Configuration
@ConditionalOnProperty(name = "grpc.enable", havingValue = "true")
public class GrpcConfig {

    @Bean
    public GrpcProperties grpcProperties(){
        return new GrpcProperties();
    }

    @Bean
    public GrpcChannelFactory grpcChannelFactory(GrpcProperties grpcProperties){
        return new GrpcChannelFactory(grpcProperties);
    }

}
