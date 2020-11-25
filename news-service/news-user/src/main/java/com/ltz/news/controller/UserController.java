package com.ltz.news.controller;

import com.ltz.news.pojo.bo.UpdateUserInfoBO;
import com.ltz.news.controller.user.UserControllerApi;
import com.ltz.news.result.GraceJSONResult;
import com.ltz.news.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class UserController implements UserControllerApi {
    @Autowired
    private IUserService userService;

    @Override
    public GraceJSONResult getUserInfo(String userId) {
        return userService.getUserInfo(userId);
    }

    @Override
    public GraceJSONResult getAccountInfo(String userId) {
        return userService.getAccountInfo(userId);
    }

    @Override
    public GraceJSONResult updateUserInfo(
            @Valid UpdateUserInfoBO updateUserInfoBO,
            BindingResult result) {
        return userService.updateUserInfo(updateUserInfoBO,result);
    }
}

