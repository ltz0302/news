package com.ltz.news.service.impl;

import com.ltz.news.resource.FileResource;
import com.ltz.news.result.GraceJSONResult;
import com.ltz.news.result.ResponseStatusEnum;
import com.ltz.news.service.IUploaderService;
import com.ltz.news.utils.extend.TengxunyunResource;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectResult;

import com.qcloud.cos.region.Region;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.InputStream;

@Service
public class UploaderServiceImpl implements IUploaderService {

    @Autowired
    private FileResource fileResource;

    @Autowired
    private TengxunyunResource tengxunyunResource;

    @Autowired
    private Sid sid;

    final static Logger logger = LoggerFactory.getLogger(UploaderServiceImpl.class);

    /**
     * 使用COS上传文件
     *
     * @param file
     * @param userId
     * @param fileExtName
     */
    @Override
    public String uploadCOS(MultipartFile file, String userId, String fileExtName) throws Exception {
        // 1 初始化用户身份信息（secretId, secretKey）。
        String secretId = tengxunyunResource.getSecretId();
        String secretKey = tengxunyunResource.getSecretKey();
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        // 2 设置 bucket 的区域, COS 地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
        // clientConfig 中包含了设置 region, https(默认 http), 超时, 代理等 set 方法, 使用可参见源码或者常见问题 Java SDK 部分。
        Region region = new Region("ap-guangzhou");
        ClientConfig clientConfig = new ClientConfig(region);
        // 3 生成 cos 客户端。
        COSClient cosClient = new COSClient(cred, clientConfig);


        // Bucket的命名格式为 BucketName-APPID ，此处填写的存储桶名称必须为此格式
        String bucketName = fileResource.getBucketName();
        String fileName = sid.nextShort();
        String key = fileResource.getObjectName()
                + "/" + userId + "/" + fileName + "." + fileExtName;

        // 从输入流上传(需提前告知输入流的长度, 否则可能导致 oom)


        InputStream fileInputStream = file.getInputStream();
        ObjectMetadata objectMetadata = new ObjectMetadata();
        // 设置输入流长度为5000
//        objectMetadata.setContentLength(5000);
        // 设置 Content type, 默认是 application/octet-stream
        // objectMetadata.setContentType("application/pdf");

        PutObjectResult putObjectResult = cosClient.putObject(bucketName, key, fileInputStream, objectMetadata);
        String etag = putObjectResult.getETag();
        // 关闭输入流...
        cosClient.shutdown();
        return key;
    }


    @Override
    public GraceJSONResult uploadFace(String userId, MultipartFile file) throws Exception {
        String path = "";
        if (file != null) {
            // 获得文件上传的名称
            String fileName = file.getOriginalFilename();

            // 判断文件名不能为空
            if (StringUtils.isNotBlank(fileName)) {
                String fileNameArr[] = fileName.split("\\.");
                // 获得后缀
                String suffix = fileNameArr[fileNameArr.length - 1];
                // 判断后缀符合我们的预定义规范
                if (!suffix.equalsIgnoreCase("png") &&
                        !suffix.equalsIgnoreCase("jpg") &&
                        !suffix.equalsIgnoreCase("jpeg")
                ) {
                    return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_FORMATTER_FAILD);
                }

                // 执行上传
                path = uploadCOS(file, userId, suffix);

            } else {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_NULL_ERROR);
            }
        } else {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_NULL_ERROR);
        }

        logger.info("path = " + path);

        String finalPath = "";
        if (StringUtils.isNotBlank(path)) {
            finalPath = fileResource.getCosHost() + path;
        } else {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }
        return GraceJSONResult.ok(finalPath);
    }
}
