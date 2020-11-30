package com.ltz.news.controller;

import com.ltz.news.controller.files.FileUploaderControllerApi;
import com.ltz.news.pojo.bo.NewAdminBO;
import com.ltz.news.result.GraceJSONResult;
import com.ltz.news.service.IUploaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class FileUploaderController implements FileUploaderControllerApi {

    @Autowired
    private IUploaderService uploaderService;
    /**
     * 上传单文件
     *
     * @param userId
     * @param file
     * @return
     * @throws Exception
     */
    @Override
    public GraceJSONResult uploadFace(String userId, MultipartFile file) throws Exception {
        return uploaderService.uploadFace(userId,file);
    }

    /**
     * 文件上传到mongodb的gridfs中
     *
     * @param newAdminBO
     * @return
     * @throws Exception
     */
    @Override
    public GraceJSONResult uploadToGridFS(NewAdminBO newAdminBO) throws Exception {
        return uploaderService.uploadToGridFS(newAdminBO);
    }

    /**
     * 从gridfs中读取图片内容
     *
     * @param faceId
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    public void readInGridFS(String faceId, HttpServletRequest request, HttpServletResponse response) throws Exception {
        uploaderService.readInGridFS(faceId,request,response);
    }

    /**
     * 从gridfs中读取图片内容，并且返回base64
     *
     * @param faceId
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    public GraceJSONResult readFace64InGridFS(String faceId, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return uploaderService.readFace64InGridFS(faceId,request,response);
    }
}
