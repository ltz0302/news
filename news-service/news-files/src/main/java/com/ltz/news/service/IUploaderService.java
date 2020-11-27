package com.ltz.news.service;

import com.ltz.news.result.GraceJSONResult;
import org.springframework.web.multipart.MultipartFile;

public interface IUploaderService {
    /**
     * 使用COS上传文件
     */
    public String uploadCOS(MultipartFile file,
                            String userId,
                            String fileExtName) throws Exception;

    public GraceJSONResult uploadFace(String userId,
                                      MultipartFile file) throws Exception;
}
