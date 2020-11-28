package com.ltz.news.service.impl;

import com.ltz.news.exception.GraceException;
import com.ltz.news.pojo.bo.NewAdminBO;
import com.ltz.news.resource.FileResource;
import com.ltz.news.result.GraceJSONResult;
import com.ltz.news.result.ResponseStatusEnum;
import com.ltz.news.service.IUploaderService;
import com.ltz.news.utils.FileUtils;
import com.ltz.news.utils.extend.TengxunyunResource;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectResult;

import com.qcloud.cos.region.Region;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.n3r.idworker.Sid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Service
public class UploaderServiceImpl implements IUploaderService {

    @Autowired
    private FileResource fileResource;

    @Autowired
    private TengxunyunResource tengxunyunResource;

    @Autowired
    private Sid sid;

    @Autowired
    private GridFSBucket gridFSBucket;

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


    @Override
    public GraceJSONResult uploadToGridFS(NewAdminBO newAdminBO) throws Exception {
        // 获得图片的base64字符串
        String file64 = newAdminBO.getImg64();

        // 将base64字符串转换为byte数组
        byte[] bytes = new BASE64Decoder().decodeBuffer(file64.trim());

        // 转换为输入流
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

        // 上传到gridfs中
        ObjectId fileId = gridFSBucket.uploadFromStream(newAdminBO.getUsername() + ".png", inputStream);

        // 获得文件在gridfs中的主键id
        String fileIdStr = fileId.toString();
        return GraceJSONResult.ok(fileIdStr);
    }


    @Override
    public void readInGridFS(String faceId, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 0. 判断参数
        if (StringUtils.isBlank(faceId) || faceId.equalsIgnoreCase("null")) {
            GraceException.display(ResponseStatusEnum.FILE_NOT_EXIST_ERROR);
        }

        // 1. 从gridfs中读取
        File adminFace = readGridFSByFaceId(faceId);

        // 2. 把人脸图片输出到浏览器
        FileUtils.downloadFileByStream(response, adminFace);
    }

    private File readGridFSByFaceId(String faceId) throws Exception {

        GridFSFindIterable gridFSFiles
                = gridFSBucket.find(Filters.eq("_id", new ObjectId(faceId)));

        GridFSFile gridFS = gridFSFiles.first();

        if (gridFS == null) {
            GraceException.display(ResponseStatusEnum.FILE_NOT_EXIST_ERROR);
        }

        String fileName = gridFS.getFilename();

        // 获取文件流，保存文件到本地或者服务器的临时目录
        // TODO
        File fileTemp = new File("C:/Users/HP/Desktop/java/news/temp_face");
        if (!fileTemp.exists()) {
            fileTemp.mkdirs();
        }

        File myFile = new File("C:/Users/HP/Desktop/java/news/temp_face/" + fileName);

        // 创建文件输出流
        OutputStream os = new FileOutputStream(myFile);
        // 下载到服务器或者本地
        gridFSBucket.downloadToStream(new ObjectId(faceId), os);

        return myFile;
    }
}
