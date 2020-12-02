package com.ltz.news.service;

import com.ltz.news.pojo.bo.SaveFriendLinkBO;
import com.ltz.news.pojo.mo.FriendLinkMO;
import com.ltz.news.result.GraceJSONResult;

import java.util.List;


public interface IFriendLinkService {

    GraceJSONResult saveOrUpdateFriendLink(SaveFriendLinkBO saveFriendLinkBO);

    /**
     * 查询友情链接
     */
    List<FriendLinkMO> queryAllFriendLinkList();

    /**
     * 删除友情链接
     */
    void delete(String linkId);

    /**
     * 首页查询友情链接
     */
    public List<FriendLinkMO> queryPortalAllFriendLinkList();
}
