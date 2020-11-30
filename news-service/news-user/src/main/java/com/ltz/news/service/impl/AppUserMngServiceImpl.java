package com.ltz.news.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ltz.news.constant.Constant;
import com.ltz.news.constant.UserStatus;
import com.ltz.news.mapper.AppUserMapper;
import com.ltz.news.pojo.AppUser;
import com.ltz.news.service.IAppUserMngService;
import com.ltz.news.utils.PagedGridResult;
import com.ltz.news.utils.RedisOperator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;


@Service
public class AppUserMngServiceImpl implements IAppUserMngService {

    @Autowired
    private AppUserMapper appUserMapper;

    @Autowired
    private RedisOperator redis;
    /**
     * 查询管理员列表
     *
     * @param nickname
     * @param status
     * @param startDate
     * @param endDate
     * @param page
     * @param pageSize
     */
    @Override
    public PagedGridResult queryAllUserList(String nickname, Integer status, Date startDate, Date endDate, Integer page, Integer pageSize) {
        Example example = new Example(AppUser.class);
        example.orderBy("createdTime").desc();
        Example.Criteria criteria = example.createCriteria();

        if (StringUtils.isNotBlank(nickname)) {
            criteria.andLike("nickname", "%" + nickname + "%");
        }

        if (UserStatus.isUserStatusValid(status)) {
            criteria.andEqualTo("activeStatus", status);
        }

        if (startDate != null) {
            criteria.andGreaterThanOrEqualTo("createdTime", startDate);
        }
        if (endDate != null) {
            criteria.andLessThanOrEqualTo("createdTime", endDate);
        }

        PageHelper.startPage(page, pageSize);
        List<AppUser> list = appUserMapper.selectByExample(example);

        return setterPagedGrid(list, page);
    }

    /**
     * 冻结用户账号，或者解除冻结状态
     *
     * @param userId
     * @param doStatus
     */
    @Transactional
    @Override
    public void freezeUserOrNot(String userId, Integer doStatus) {

        AppUser user = new AppUser();
        user.setId(userId);
        user.setActiveStatus(doStatus);
        appUserMapper.updateByPrimaryKeySelective(user);
        // 刷新用户状态两种方法：
        // 1. 删除用户会话，从而保障用户需要重新登录以后再来刷新他的会话状态
        // 2. 查询最新用户的信息，重新放入redis中，做一次更新
        redis.del(Constant.REDIS_USER_INFO + ":" + userId);
    }


    private PagedGridResult setterPagedGrid(List<?> userList,
                                            Integer page) {
        PageInfo<?> pageList = new PageInfo<>(userList);
        PagedGridResult gridResult = new PagedGridResult();
        gridResult.setRows(userList);
        gridResult.setPage(page);
        gridResult.setRecords(pageList.getTotal());
        gridResult.setTotal(pageList.getPages());
        return gridResult;
    }
}
