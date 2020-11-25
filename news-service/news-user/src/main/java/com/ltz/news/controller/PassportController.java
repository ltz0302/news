package com.ltz.news.controller;


import com.ltz.news.pojo.bo.RegistLoginBO;
import com.ltz.news.controller.user.PassportControllerApi;
import com.ltz.news.result.GraceJSONResult;
import com.ltz.news.service.IPassportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
public class PassportController implements PassportControllerApi {

    @Autowired
    private IPassportService passportService;

    @Override
    public GraceJSONResult getSMSCode(@RequestParam String mobile, HttpServletRequest request) {

        return passportService.getSMSCode(mobile,request);
    }

    @Override
    public GraceJSONResult doLogin(@Valid RegistLoginBO registLoginBO, BindingResult result, HttpServletRequest request, HttpServletResponse response) {
        return passportService.doLogin(registLoginBO,result,request,response);
    }

    @Override
    public GraceJSONResult logout(String userId, HttpServletRequest request, HttpServletResponse response) {
        return passportService.logout(userId,request,response);
    }
}
