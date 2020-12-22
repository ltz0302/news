package com.ltz.news.service.impl;

import com.ltz.news.constant.Constant;
import com.ltz.news.pojo.bo.RegistLoginBO;
import com.ltz.news.constant.UserStatus;
import com.ltz.news.pojo.AppUser;
import com.ltz.news.result.GraceJSONResult;
import com.ltz.news.result.ResponseStatusEnum;
import com.ltz.news.service.IPassportService;
import com.ltz.news.service.IUserService;
import com.ltz.news.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



import java.util.UUID;



@Service
public class PassportServiceImpl implements IPassportService {

    @Autowired
    private RedisOperator redis;
//    @Autowired
//    private SMSUtils smsUtils;
    @Autowired
    private IUserService userService;


    @Override
    public GraceJSONResult getSMSCode(String mobile, HttpServletRequest request) {

        // 获得用户ip
        String userIp = IPUtil.getRequestIp(request);

        // 根据用户的ip进行限制，限制用户在60秒内只能获得一次验证码
        redis.setnx60s(Constant.MOBILE_SMSCODE + ":" + userIp, userIp);

        // 生成随机验证码并且发送短信
        String random = (int)((Math.random() * 9 + 1) * 100000) + "";

        //TODO
//        smsUtils.sendSMS(mobile, random);

        // 把验证码存入redis，用于后续进行验证
        redis.set(Constant.MOBILE_SMSCODE + ":" + mobile, random, 30 * 60);

        return GraceJSONResult.ok();
    }


    @Override
    public GraceJSONResult doLogin(RegistLoginBO registLoginBO,HttpServletRequest request, HttpServletResponse response) {

        String mobile = registLoginBO.getMobile();
        String smsCode = registLoginBO.getSmsCode();


        // 1. 校验验证码是否匹配
        String redisSMSCode = redis.get(Constant.MOBILE_SMSCODE + ":" + mobile);
        if (StringUtils.isBlank(redisSMSCode) || !redisSMSCode.equalsIgnoreCase(smsCode)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }

        // 2. 查询数据库，判断该用户注册
        AppUser user = userService.queryMobileIsExist(mobile);
        if (user != null && user.getActiveStatus() == UserStatus.FROZEN.type) {
            // 如果用户不为空，并且状态为冻结，则直接抛出异常，禁止登录
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_FROZEN);
        } else if (user == null) {
            // 如果用户没有注册过，则为null，需要注册信息入库
            user = userService.createUser(mobile);
        }

        // 3. 保存用户分布式会话的相关操作
        int userActiveStatus = user.getActiveStatus();
        if (userActiveStatus != UserStatus.FROZEN.type) {
            // 保存token到redis
            String uToken = UUID.randomUUID().toString();
            redis.set(Constant.REDIS_USER_TOKEN + ":" + user.getId(), uToken);
            redis.set(Constant.REDIS_USER_INFO + ":" + user.getId(), JsonUtils.objectToJson(user));

            // 保存用户id和token到cookie中
            ControllerUtils.setCookie(request, response, "utoken", uToken, Constant.COOKIE_MONTH);
            ControllerUtils.setCookie(request, response, "uid", user.getId(), Constant.COOKIE_MONTH);
        }

//        // 4. 用户登录或注册成功以后，需要删除redis中的短信验证码，验证码只能使用一次，用过后则作废
        redis.del(Constant.MOBILE_SMSCODE + ":" + mobile);

        // 5. 返回用户状态
        return GraceJSONResult.ok(userActiveStatus);
    }


    @Override
    public GraceJSONResult logout(String userId, HttpServletRequest request, HttpServletResponse response) {

        redis.del(Constant.REDIS_USER_TOKEN + ":" + userId);

        ControllerUtils.setCookie(request, response, "utoken", "", Constant.COOKIE_DELETE);
        ControllerUtils.setCookie(request, response, "uid", "", Constant.COOKIE_DELETE);

        return GraceJSONResult.ok();
    }
}
