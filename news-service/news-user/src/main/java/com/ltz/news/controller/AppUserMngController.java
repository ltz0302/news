package com.ltz.news.controller;

import com.ltz.news.constant.Constant;
import com.ltz.news.constant.UserStatus;
import com.ltz.news.controller.user.AppUserMngControllerApi;
import com.ltz.news.result.GraceJSONResult;
import com.ltz.news.result.ResponseStatusEnum;
import com.ltz.news.service.IAppUserMngService;
import com.ltz.news.service.IUserService;
import com.ltz.news.utils.PagedGridResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class AppUserMngController implements AppUserMngControllerApi {

    @Autowired
    private IAppUserMngService appUserMngService;

    @Autowired
    private IUserService userService;

    @Override
    public GraceJSONResult queryAll(String nickname, Integer status, Date startDate, Date endDate, Integer page, Integer pageSize) {

        if (page == null) {
            page = Constant.COMMON_START_PAGE;
        }

        if (pageSize == null) {
            pageSize = Constant.COMMON_PAGE_SIZE;
        }

        PagedGridResult result = appUserMngService.queryAllUserList(nickname,
                status,
                startDate,
                endDate,
                page,
                pageSize);

        return GraceJSONResult.ok(result);
    }

    @Override
    public GraceJSONResult userDetail(String userId) {
        return GraceJSONResult.ok(userService.getUser(userId));
    }

    @Override
    public GraceJSONResult freezeUserOrNot(String userId, Integer doStatus) {
        if (!UserStatus.isUserStatusValid(doStatus)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_STATUS_ERROR);
        }
        appUserMngService.freezeUserOrNot(userId, doStatus);



        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult queryAll(String userIds) {
        return null;
    }
}
