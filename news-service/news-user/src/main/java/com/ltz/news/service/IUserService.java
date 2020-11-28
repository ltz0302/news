package com.ltz.news.service;

import com.ltz.news.pojo.bo.UpdateUserInfoBO;
import com.ltz.news.pojo.AppUser;
import com.ltz.news.result.GraceJSONResult;
import org.springframework.validation.BindingResult;

public interface IUserService {


    /**
     * 判断用户是否存在，如果存在返回user信息
     */
    AppUser queryMobileIsExist(String mobile);

    /**
     * 创建用户，新增用户记录到数据库
     */
    AppUser createUser(String mobile);


    GraceJSONResult getUserInfo(String userId);



    GraceJSONResult getAccountInfo(String userId);


//
//    /**
//     * 根据用户id查询用户
//     */
//    public List<PublisherVO> getUserList(List<String> userIdList);


    /**
     * 用户修改信息，完善资料，并且激活
     */
    GraceJSONResult updateUserInfo(UpdateUserInfoBO updateUserInfoBO, BindingResult result);

}
