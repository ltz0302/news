package com.ltz.news.service.impl;

import com.ltz.news.constant.YesOrNo;
import com.ltz.news.pojo.bo.SaveFriendLinkBO;
import com.ltz.news.pojo.mo.FriendLinkMO;
import com.ltz.news.repository.FriendLinkRepository;
import com.ltz.news.result.GraceJSONResult;
import com.ltz.news.service.IFriendLinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class FriendLinkServiceImpl implements IFriendLinkService {

    final static Logger logger = LoggerFactory.getLogger(FriendLinkServiceImpl.class);

    @Autowired
    private FriendLinkRepository friendLinkRepository;

    @Override
    public GraceJSONResult saveOrUpdateFriendLink(SaveFriendLinkBO saveFriendLinkBO) {
        //        if (result.hasErrors()) {
//            Map<String, String> map = getErrors(result);
//            return GraceJSONResult.errorMap(map);
//        }

//        saveFriendLinkBO -> ***MO

        FriendLinkMO saveFriendLinkMO = new FriendLinkMO();
        BeanUtils.copyProperties(saveFriendLinkBO, saveFriendLinkMO);
        saveFriendLinkMO.setCreateTime(new Date());
        saveFriendLinkMO.setUpdateTime(new Date());

        saveOrUpdateFriendLink(saveFriendLinkMO);

        return GraceJSONResult.ok();
    }

    public void saveOrUpdateFriendLink(FriendLinkMO friendLinkMO) {
        friendLinkRepository.save(friendLinkMO);
    }

    /**
     * 查询友情链接
     */
    @Override
    public List<FriendLinkMO> queryAllFriendLinkList() {
        return friendLinkRepository.findAll();
    }


    /**
     * 删除友情链接
     *
     * @param linkId
     */
    @Override
    public void delete(String linkId) {
        friendLinkRepository.deleteById(linkId);
    }


    /**
     * 首页查询友情链接
     */
    @Override
    public List<FriendLinkMO> queryPortalAllFriendLinkList() {
        return friendLinkRepository.getAllByIsDelete(YesOrNo.NO.type);
    }
}
