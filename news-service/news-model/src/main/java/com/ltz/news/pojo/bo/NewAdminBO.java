package com.ltz.news.pojo.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 添加管理人员的BO
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewAdminBO {

    private String username;
    private String adminName;
    private String password;
    private String confirmPassword;
    private String img64;
    private String faceId;


    @Override
    public String toString() {
        return "NewAdminBO{" +
                "username='" + username + '\'' +
                ", adminName='" + adminName + '\'' +
                ", password='" + password + '\'' +
                ", confirmPassword='" + confirmPassword + '\'' +
                ", img64='" + img64 + '\'' +
                ", faceId='" + faceId + '\'' +
                '}';
    }
}
