package com.ltz.news.controller;

import com.ltz.news.controller.files.FileUploaderControllerApi;
import com.ltz.news.result.GraceJSONResult;
import com.ltz.news.service.IUploaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
}
