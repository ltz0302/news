package com.ltz.news.controller.files;

import com.ltz.news.pojo.bo.NewAdminBO;
import com.ltz.news.result.GraceJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(value = "文件上传的Controller", tags = {"文件上传的Controller"})
@RequestMapping("fs")
public interface FileUploaderControllerApi {

    /**
     * 上传单文件
     *
     * @param userId
     * @param file
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "上传头像", notes = "上传头像", httpMethod = "POST")
    @PostMapping("/uploadFace")
    GraceJSONResult uploadFace(@RequestParam String userId,
                               MultipartFile file) throws Exception;

//    /**
//     * 上传多个文件
//     * @param userId
//     * @param files
//     * @return
//     * @throws Exception
//     */
//    @PostMapping("/uploadSomeFiles")
//    public GraceJSONResult uploadSomeFiles(@RequestParam String userId,
//                                           MultipartFile[] files) throws Exception;
//
//

    /**
     * 文件上传到mongodb的gridfs中
     *
     * @param newAdminBO
     * @return
     * @throws Exception
     */
    @PostMapping("/uploadToGridFS")
    GraceJSONResult uploadToGridFS(@RequestBody NewAdminBO newAdminBO)
            throws Exception;

    /**
     * 从gridfs中读取图片内容
     *
     * @param faceId
     * @return
     * @throws Exception
     */
    @GetMapping("/readInGridFS")
    void readInGridFS(String faceId,
                      HttpServletRequest request,
                      HttpServletResponse response)
            throws Exception;
//
//
//    /**
//     * 从gridfs中读取图片内容，并且返回base64
//     * @param faceId
//     * @param request
//     * @param response
//     * @return
//     * @throws Exception
//     */
//    @GetMapping("/readFace64InGridFS")
//    GraceJSONResult readFace64InGridFS(String faceId,
//                                              HttpServletRequest request,
//                                              HttpServletResponse response)
//                                                throws Exception;
}
