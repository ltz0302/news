package com.ltz.news.pojo.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员登录的BO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminLoginBO {

    private String username;
    private String password;
    private String img64;

}