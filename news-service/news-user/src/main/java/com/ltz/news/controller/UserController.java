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
import org.springframework.validation.BindingResult;
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
            @Valid UpdateUserInfoBO updateUserInfoBO,
            BindingResult result) {
        return userService.updateUserInfo(updateUserInfoBO,result);
    }

    @Override
    public GraceJSONResult queryByIds(String userIds) {
        // 1. 手动触发异常
//        int a = 1 / 0;

        // 2. 模拟超时异常
//        try {
//            Thread.sleep(6000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


        if (StringUtils.isBlank(userIds)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

        List<AppUserVO> publisherList = new ArrayList<>();
        List<String> userIdList = JsonUtils.jsonToList(userIds, String.class);

//        // FIXME: 仅用于dev测试，硬编码动态判断来抛出异常
//        if (userIdList.size() > 1) {
//            System.out.println("出现异常~~");
//            throw new RuntimeException("出现异常~~");
//        }

        // FIXME: 仅用于dev，硬编码动态判断抛出异常
//        if (!userIdList.get(0).equalsIgnoreCase("200628AFYM7AGWPH")) {
//            System.out.println("出异常啦~");
//            throw new RuntimeException("出异常啦~");
//        }

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

