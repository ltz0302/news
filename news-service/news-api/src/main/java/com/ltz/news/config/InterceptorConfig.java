package com.ltz.news.config;


import com.ltz.news.interceptors.PassportInterceptor;
import com.ltz.news.interceptors.UserActiveInterceptor;
import com.ltz.news.interceptors.UserTokenInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Bean
    public PassportInterceptor passportInterceptor() { return new PassportInterceptor(); }

    @Bean
    public UserTokenInterceptor userTokenInterceptor() {
        return new UserTokenInterceptor();
    }

    @Bean
    public UserActiveInterceptor userActiveInterceptor() {
        return new UserActiveInterceptor();
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(passportInterceptor())
                .addPathPatterns("/passport/getSMSCode");

        registry.addInterceptor(userTokenInterceptor())
                .addPathPatterns("/user/getAccountInfo")
                .addPathPatterns("/user/updateUserInfo");


//        registry.addInterceptor(userActiveInterceptor())
//                .addPathPatterns("/fs/uploadSomeFiles")
//                .addPathPatterns("/fans/follow")
//                .addPathPatterns("/fans/unfollow");
    }

}
