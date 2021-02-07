package com.ltz.news.service;

import com.ltz.news.pojo.bo.NewAdminBO;
import com.ltz.news.result.GraceJSONResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IUploaderService {

    GraceJSONResult uploadFace(String userId,
                               MultipartFile file) throws Exception;

    GraceJSONResult uploadSomeFiles(String userId, MultipartFile[] files) throws Exception;

    GraceJSONResult uploadToGridFS(NewAdminBO newAdminBO) throws Exception;

    void readInGridFS(String faceId, HttpServletRequest request, HttpServletResponse response) throws Exception;

    GraceJSONResult readFace64InGridFS(String faceId, HttpServletRequest request, HttpServletResponse response) throws Exception ;

}
