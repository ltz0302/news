package com.ltz.news.exception;

import com.ltz.news.result.ResponseStatusEnum;
import lombok.Data;

@Data
public class MyCustomException extends RuntimeException{


    private ResponseStatusEnum responseStatusEnum;

    public MyCustomException(ResponseStatusEnum responseStatusEnum) {
        super("异常状态码为：" + responseStatusEnum.status()
                + "；具体异常信息为：" + responseStatusEnum.msg());
        this.responseStatusEnum = responseStatusEnum;
    }


}
