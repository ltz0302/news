package com.ltz.news.controller;


import com.ltz.news.controller.admin.AdminMngControllerApi;
import com.ltz.news.pojo.bo.AdminLoginBO;
import com.ltz.news.pojo.bo.NewAdminBO;
import com.ltz.news.result.GraceJSONResult;
import com.ltz.news.service.IAdminUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class AdminMngController implements AdminMngControllerApi {

    final static Logger logger = LoggerFactory.getLogger(AdminMngController.class);

    @Autowired
    private IAdminUserService adminUserService;

    @Override
    public GraceJSONResult adminLogin(AdminLoginBO adminLoginBO, HttpServletRequest request, HttpServletResponse response) {
        return adminUserService.adminLogin(adminLoginBO,request,response);
    }


    @Override
    public GraceJSONResult adminIsExist(String username) {
        return adminUserService.adminIsExist(username);
    }


    @Override
    public GraceJSONResult addNewAdmin(NewAdminBO newAdminBO, HttpServletRequest request, HttpServletResponse response) {
        return adminUserService.addNewAdmin(newAdminBO,request,response);
    }

    @Override
    public GraceJSONResult getAdminList(Integer page, Integer pageSize) {
        return adminUserService.getAdminList(page,pageSize);
    }

    @Override
    public GraceJSONResult adminLogout(String adminId, HttpServletRequest request, HttpServletResponse response) {
        return adminUserService.adminLogout(adminId,request,response);
    }

    @Override
    public GraceJSONResult adminFaceLogin(AdminLoginBO adminLoginBO, HttpServletRequest request, HttpServletResponse response) {
        return adminUserService.adminFaceLogin(adminLoginBO,request,response);
    }
}
