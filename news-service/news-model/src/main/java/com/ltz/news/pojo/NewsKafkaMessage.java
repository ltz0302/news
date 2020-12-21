package com.ltz.news.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsKafkaMessage {
    /* 0 下载
       1 删除
     */
    private Integer action;


    private String message;

}
