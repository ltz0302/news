package com.ltz.news;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@MapperScan(basePackages = "com.ltz.news.mapper")
@ComponentScan(basePackages = {"com.ltz.news","org.n3r.idworker"})
public class ArticleHTMLApplication {
    public static void main(String[] args) {
        SpringApplication.run(ArticleHTMLApplication.class, args);
    }
}