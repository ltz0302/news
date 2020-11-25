package com.ltz.news.service;

import com.ltz.news.pojo.bo.RegistLoginBO;
import com.ltz.news.result.GraceJSONResult;
import org.springframework.validation.BindingResult;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IPassportService {

    GraceJSONResult getSMSCode(String mobile, HttpServletRequest request);

    GraceJSONResult doLogin(RegistLoginBO registLoginBO, BindingResult result, HttpServletRequest request, HttpServletResponse response);

    GraceJSONResult logout(String userId, HttpServletRequest request, HttpServletResponse response);
}
