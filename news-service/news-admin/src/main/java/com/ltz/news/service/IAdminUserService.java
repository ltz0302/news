package com.ltz.news.service;

import com.ltz.news.pojo.AdminUser;
import com.ltz.news.pojo.bo.AdminLoginBO;
import com.ltz.news.pojo.bo.NewAdminBO;
import com.ltz.news.result.GraceJSONResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IAdminUserService {




    GraceJSONResult adminLogin(AdminLoginBO adminLoginBO, HttpServletRequest request, HttpServletResponse response);

    GraceJSONResult adminIsExist(String username);

    GraceJSONResult addNewAdmin(NewAdminBO newAdminBO, HttpServletRequest request, HttpServletResponse response);

    GraceJSONResult getAdminList(Integer page, Integer pageSize);

    GraceJSONResult adminLogout(String adminId,HttpServletRequest request,HttpServletResponse response);
//
//    /**
//     * 新增管理员
//     */
//    public void createAdminUser(NewAdminBO newAdminBO);
//
//    /**
//     * 分页查询admin列表
//     */
//    public PagedGridResult queryAdminList(Integer page, Integer pageSize);

}
