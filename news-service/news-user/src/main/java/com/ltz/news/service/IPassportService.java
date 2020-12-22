package com.ltz.news.service;

import com.ltz.news.pojo.bo.RegistLoginBO;
import com.ltz.news.result.GraceJSONResult;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IPassportService {

    GraceJSONResult getSMSCode(String mobile, HttpServletRequest request);

    GraceJSONResult doLogin(RegistLoginBO registLoginBO, HttpServletRequest request, HttpServletResponse response);

    GraceJSONResult logout(String userId, HttpServletRequest request, HttpServletResponse response);
}
