package com.ltz.news.service;

import com.alibaba.fastjson.JSON;
import com.ltz.news.constant.Constant;
import com.ltz.news.pojo.NewsKafkaMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class KafkaConsumer {
    @Autowired
    private ArticleHTMLComponent articleHTMLComponent;

    @KafkaListener(topics = {Constant.TOPIC}, groupId = "coupon-1")
    public void consumeKafkaMessage(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if(kafkaMessage.isPresent()){
            Object message = kafkaMessage.get();
            NewsKafkaMessage info = JSON.parseObject(message.toString(),NewsKafkaMessage.class);
            int action = info.getAction();
            if(action == 0){
                String articleId = info.getMessage().split(",")[0];
                String articleMongoId = info.getMessage().split(",")[1];
                try {
                    articleHTMLComponent.download(articleId, articleMongoId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if(action == 1){

                String articleId = info.getMessage();
                try {
                    articleHTMLComponent.delete(articleId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }


}
