package com.ltz.news.service;

import com.ltz.news.utils.PagedGridResult;

import java.util.Date;

public interface IAppUserMngService {
    /**
     * 查询管理员列表
     */
    PagedGridResult queryAllUserList(String nickname,
                                     Integer status,
                                     Date startDate,
                                     Date endDate,
                                     Integer page,
                                     Integer pageSize);

    /**
     * 冻结用户账号，或者解除冻结状态
     */
    void freezeUserOrNot(String userId, Integer doStatus);
}
