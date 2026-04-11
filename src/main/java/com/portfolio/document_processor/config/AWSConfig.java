package com.portfolio.document_processor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

// Marks this class as a source of bean definitions.( used to define and configure beans using Java code)
@Configuration
public class AWSConfig {

    // Injects the 'aws.region' property value from the application configuration (e.g., application.yml or application.properties)
    @Value("${aws.region}")
    private String region;

    // Indicates that this method produces a bean/object to be managed by the Spring IoC container
    @Bean
    public S3Client s3Client(){  //S3Client class acts as your application's primary gateway to communicate programmatically with Amazon Simple Storage Service (S3).
        // The SDK automatically finds your AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY
        // from the IntelliJ environment variables you  set!
        return S3Client.builder()
                .region(Region.of(region))
                .build();
    }
}
