package com.ltz.news.service;

import com.ltz.news.constant.Sex;
import com.ltz.news.pojo.vo.RegionRatioVO;
import com.ltz.news.utils.PagedGridResult;

import java.util.List;

public interface IMyFanService {
    /**
     * 查询当前用户是否关注作家
     */
    boolean isMeFollowThisWriter(String writerId, String fanId);


    /**
     * 关注成为粉丝
     */
    void follow(String writerId, String fanId);

    /**
     * 粉丝取消关注
     */
    void unfollow(String writerId, String fanId);

    /**
     * 查询我的粉丝数
     */
    PagedGridResult queryMyFansList(String writerId,
                                    Integer page,
                                    Integer pageSize);

    /**
     * 查询粉丝数
     */
    Integer queryFansCounts(String writerId, Sex sex);

    /**
     * 查询粉丝数
     */
    List<RegionRatioVO> queryRegionRatioCounts(String writerId);

}
