package com.ltz.news.controller;

import com.ltz.news.controller.admin.FriendLinkControllerApi;
import com.ltz.news.pojo.bo.SaveFriendLinkBO;
import com.ltz.news.pojo.mo.FriendLinkMO;
import com.ltz.news.result.GraceJSONResult;
import com.ltz.news.service.impl.FriendLinkServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
public class FriendLinkController implements FriendLinkControllerApi {

    @Autowired
    private FriendLinkServiceImpl friendLinkService;

    @Override
    public GraceJSONResult saveOrUpdateFriendLink(@Valid SaveFriendLinkBO saveFriendLinkBO) {
        return friendLinkService.saveOrUpdateFriendLink(saveFriendLinkBO);
    }

    @Override
    public GraceJSONResult getFriendLinkList() {
        return GraceJSONResult.ok(friendLinkService.queryAllFriendLinkList());
    }

    @Override
    public GraceJSONResult delete(String linkId) {
        friendLinkService.delete(linkId);
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult queryPortalAllFriendLinkList() {
        List<FriendLinkMO> list = friendLinkService.queryPortalAllFriendLinkList();
        return GraceJSONResult.ok(list);
    }
}
