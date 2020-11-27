package com.ltz.news.resource;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:file.properties")
@ConfigurationProperties(prefix = "file")
@Data
public class FileResource {

    private String bucketName;
    private String objectName;
    private String cosHost;


}
