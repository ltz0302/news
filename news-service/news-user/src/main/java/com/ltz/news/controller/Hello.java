package com.ltz.news.controller;



import com.ltz.news.controller.files.HelloApi;
import com.ltz.news.result.GraceJSONResult;
import com.ltz.news.utils.RedisOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;




@RestController
public class Hello implements HelloApi {

    final static Logger logger = LoggerFactory.getLogger(Hello.class);
    @Autowired
    private RedisOperator redisOperator;

    @GetMapping("/hello")
    public Object hello(){

        logger.debug("debug: hello~");
        logger.info("info: hello~");
        logger.warn("warn: hello~");
        logger.error("error: hello~");
        return GraceJSONResult.ok();
    }

    @GetMapping("/redis")
    public Object redis(){

        redisOperator.set("age","18");
        return GraceJSONResult.ok(redisOperator.get("age"));
    }

}
