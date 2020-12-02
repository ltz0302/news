package com.ltz.news.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ltz.news.constant.Constant;
import com.ltz.news.constant.Sex;
import com.ltz.news.mapper.FansMapper;
import com.ltz.news.pojo.AppUser;
import com.ltz.news.pojo.Fans;
import com.ltz.news.pojo.vo.RegionRatioVO;
import com.ltz.news.service.IMyFanService;
import com.ltz.news.service.IUserService;
import com.ltz.news.utils.PagedGridResult;
import com.ltz.news.utils.RedisOperator;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MyFanServiceImpl implements IMyFanService {

    @Autowired
    private FansMapper fansMapper;

    @Autowired
    private IUserService userService;

    @Autowired
    private Sid sid;

    @Autowired
    private RedisOperator redis;

    /**
     * 查询当前用户是否关注作家
     *
     * @param writerId
     * @param fanId
     */
    @Override
    public boolean isMeFollowThisWriter(String writerId, String fanId) {
        Fans fan = new Fans();
        fan.setFanId(fanId);
        fan.setWriterId(writerId);

        int count = fansMapper.selectCount(fan);

        return count > 0 ? true : false;
    }

    /**
     * 关注成为粉丝
     *
     * @param writerId
     * @param fanId
     */
    @Transactional
    @Override
    public void follow(String writerId, String fanId) {
        // 获得粉丝用户的信息
        AppUser fanInfo = userService.getUser(fanId);

        String fanPkId = sid.nextShort();

        Fans fans = new Fans();
        fans.setId(fanPkId);
        fans.setFanId(fanId);
        fans.setWriterId(writerId);

        fans.setFace(fanInfo.getFace());
        fans.setFanNickname(fanInfo.getNickname());
        fans.setSex(fanInfo.getSex());
        fans.setProvince(fanInfo.getProvince());

        fansMapper.insert(fans);

        // redis 作家粉丝数累加
        redis.increment(Constant.REDIS_WRITER_FANS_COUNTS + ":" + writerId, 1);
        // redis 当前用户的（我的）关注数累加
        redis.increment(Constant.REDIS_MY_FOLLOW_COUNTS + ":" + fanId, 1);
    }

    /**
     * 粉丝取消关注
     *
     * @param writerId
     * @param fanId
     */
    @Transactional
    @Override
    public void unfollow(String writerId, String fanId) {
        Fans fans = new Fans();
        fans.setWriterId(writerId);
        fans.setFanId(fanId);

        fansMapper.delete(fans);

        // redis 作家粉丝数累减
        redis.decrement(Constant.REDIS_WRITER_FANS_COUNTS + ":" + writerId, 1);
        // redis 当前用户的（我的）关注数累减
        redis.decrement(Constant.REDIS_MY_FOLLOW_COUNTS + ":" + fanId, 1);
    }

    /**
     * 查询我的粉丝数
     *
     * @param writerId
     * @param page
     * @param pageSize
     */
    @Override
    public PagedGridResult queryMyFansList(String writerId, Integer page, Integer pageSize) {
        Fans fans = new Fans();
        fans.setWriterId(writerId);

        PageHelper.startPage(page, pageSize);
        List<Fans> list = fansMapper.select(fans);
        return setterPagedGrid(list, page);
    }

    /**
     * 查询粉丝数
     *
     * @param writerId
     * @param sex
     */
    @Override
    public Integer queryFansCounts(String writerId, Sex sex) {
        Fans fans = new Fans();
        fans.setWriterId(writerId);
        fans.setSex(sex.type);

        Integer count = fansMapper.selectCount(fans);
        return count;
    }

    public static final String[] regions = {"北京", "天津", "上海", "重庆",
            "河北", "山西", "辽宁", "吉林", "黑龙江", "江苏", "浙江", "安徽", "福建", "江西", "山东",
            "河南", "湖北", "湖南", "广东", "海南", "四川", "贵州", "云南", "陕西", "甘肃", "青海", "台湾",
            "内蒙古", "广西", "西藏", "宁夏", "新疆",
            "香港", "澳门"};

    /**
     * 查询粉丝数
     *
     * @param writerId
     */
    @Override
    public List<RegionRatioVO> queryRegionRatioCounts(String writerId) {
        Fans fans = new Fans();
        fans.setWriterId(writerId);

        List<RegionRatioVO> list = new ArrayList<>();
        for (String r : regions) {
            fans.setProvince(r);
            Integer count = fansMapper.selectCount(fans);

            RegionRatioVO regionRatioVO = new RegionRatioVO();
            regionRatioVO.setName(r);
            regionRatioVO.setValue(count);

            list.add(regionRatioVO);
        }

        return list;
    }


    private PagedGridResult setterPagedGrid(List<?> list,
                                           Integer page) {
        PageInfo<?> pageList = new PageInfo<>(list);
        PagedGridResult gridResult = new PagedGridResult();
        gridResult.setRows(list);
        gridResult.setPage(page);
        gridResult.setRecords(pageList.getTotal());
        gridResult.setTotal(pageList.getPages());
        return gridResult;
    }
}
