package com.example.crawling.config;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;



@Configuration
@RequiredArgsConstructor
public class S3Config {

    private final Environment env;

    @Bean
    public S3Client amazoneS3(){
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                env.getProperty("cloud.aws.credentials.access-key"),
                env.getProperty("cloud.aws.credentials.secret-key"));

        return S3Client.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
