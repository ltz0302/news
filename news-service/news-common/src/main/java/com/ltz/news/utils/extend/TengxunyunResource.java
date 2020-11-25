package com.ltz.news.utils.extend;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:tengxunyun.properties")
@ConfigurationProperties(prefix = "tengxunyun")
@Data
public class TengxunyunResource {
    private String secretId;
    private String secretKey;

}
