package com.ltz.news.controller;

import com.ltz.news.pojo.AppUser;
import com.ltz.news.pojo.bo.UpdateUserInfoBO;
import com.ltz.news.controller.user.UserControllerApi;
import com.ltz.news.pojo.vo.AppUserVO;
import com.ltz.news.result.GraceJSONResult;
import com.ltz.news.result.ResponseStatusEnum;
import com.ltz.news.service.IUserService;
import com.ltz.news.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

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
            @Valid UpdateUserInfoBO updateUserInfoBO) {
        return userService.updateUserInfo(updateUserInfoBO);
    }

    @Override
    public GraceJSONResult queryByIds(String userIds) {

        if (StringUtils.isBlank(userIds)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

        List<AppUserVO> publisherList = new ArrayList<>();
        List<String> userIdList = JsonUtils.jsonToList(userIds, String.class);


        for (String userId : userIdList) {
            // 获得用户基本信息
            AppUserVO userVO = getBasicUserInfo(userId);
            // 添加到publisherList
            publisherList.add(userVO);
        }

        return GraceJSONResult.ok(publisherList);
    }

    private AppUserVO getBasicUserInfo(String userId) {
        // 1. 根据userId查询用户的信息
        AppUser user = userService.getUser(userId);

        // 2. 返回用户信息
        AppUserVO userVO = new AppUserVO();
        BeanUtils.copyProperties(user, userVO);

        return userVO;
    }
}

