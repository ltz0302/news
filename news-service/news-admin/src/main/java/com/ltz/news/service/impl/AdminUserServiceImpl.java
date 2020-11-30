package com.ltz.news.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ltz.news.constant.Constant;
import com.ltz.news.constant.FaceVerifyType;
import com.ltz.news.exception.GraceException;
import com.ltz.news.mapper.AdminUserMapper;
import com.ltz.news.pojo.AdminUser;
import com.ltz.news.pojo.bo.AdminLoginBO;
import com.ltz.news.pojo.bo.NewAdminBO;
import com.ltz.news.result.GraceJSONResult;
import com.ltz.news.result.ResponseStatusEnum;
import com.ltz.news.service.IAdminUserService;
import com.ltz.news.utils.ControllerUtils;
import com.ltz.news.utils.FaceVerifyUtils;
import com.ltz.news.utils.PagedGridResult;
import com.ltz.news.utils.RedisOperator;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.mapper.entity.Example;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.UUID;





@Service
public class AdminUserServiceImpl implements IAdminUserService {


    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private RedisOperator redis;

    @Autowired
    private Sid sid;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FaceVerifyUtils faceVerifyUtils;

    @Override
    public GraceJSONResult adminLogin(AdminLoginBO adminLoginBO, HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isBlank(adminLoginBO.getUsername())) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_USERNAME_NULL_ERROR);
        }
        //人脸登录可以不用密码
//        if (StringUtils.isBlank(adminLoginBO.getPassword())) {
//            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_PASSWORD_NULL_ERROR);
//        }

        // 1. 查询admin用户的信息
        AdminUser admin = queryAdminByUsername(adminLoginBO.getUsername());
        // 2. 判断admin不为空，如果为空则登录失败
        if (admin == null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_NOT_EXIT_ERROR);
        }

        // 3. 判断密码是否匹配
        boolean isPwdMatch = BCrypt.checkpw(adminLoginBO.getPassword(), admin.getPassword());
        if (isPwdMatch) {
            doLoginSettings(admin, request, response);
            return GraceJSONResult.ok();
        } else {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_NOT_EXIT_ERROR);
        }
    }


    /**
     * 获得管理员的用户信息
     *
     * @param username
     */
    private AdminUser queryAdminByUsername(String username) {
        Example adminExample = new Example(AdminUser.class);
        Example.Criteria criteria = adminExample.createCriteria();
        criteria.andEqualTo("username", username);

        AdminUser admin = adminUserMapper.selectOneByExample(adminExample);
        return admin;
    }


    /**
     * 用于admin用户登录过后的基本信息设置
     * @param admin
     * @param request
     * @param response
     */
    private void doLoginSettings(AdminUser admin,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        // 保存token放入到redis中
        String token = UUID.randomUUID().toString();
        redis.set(Constant.REDIS_ADMIN_TOKEN + ":" + admin.getId(), token);

        // 保存admin登录基本token信息到cookie中
        ControllerUtils.setCookie(request, response, "atoken", token, Constant.COOKIE_MONTH);
        ControllerUtils.setCookie(request, response, "aid", admin.getId(), Constant.COOKIE_MONTH);
        ControllerUtils.setCookie(request, response, "aname", admin.getAdminName(), Constant.COOKIE_MONTH);
    }


    @Override
    public GraceJSONResult adminIsExist(String username) {
        checkAdminExist(username);
        return GraceJSONResult.ok();
    }

    private void checkAdminExist(String username) {
        AdminUser admin = queryAdminByUsername(username);

        if (admin != null) {
            GraceException.display(ResponseStatusEnum.ADMIN_USERNAME_EXIST_ERROR);
        }
    }


    @Override
    public GraceJSONResult addNewAdmin(NewAdminBO newAdminBO, HttpServletRequest request, HttpServletResponse response) {
        // 0.
        if (StringUtils.isBlank(newAdminBO.getUsername())) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_USERNAME_NULL_ERROR);
        }
//        if (StringUtils.isBlank(newAdminBO.getPassword())) {
//            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_PASSWORD_NULL_ERROR);
//        }

        // 1. base64不为空，则代表人脸入库，否则需要用户输入密码和确认密码
        if (StringUtils.isBlank(newAdminBO.getImg64())) {
            if (StringUtils.isBlank(newAdminBO.getPassword()) ||
                    StringUtils.isBlank(newAdminBO.getConfirmPassword())
            ) {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_PASSWORD_NULL_ERROR);
            }
        }

        // 2. 密码不为空，则必须判断两次输入一致
        if (StringUtils.isNotBlank(newAdminBO.getPassword())) {
            if (!newAdminBO.getPassword()
                    .equalsIgnoreCase(newAdminBO.getConfirmPassword())) {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_PASSWORD_ERROR);
            }
        }

        // 3. 校验用户名唯一
        checkAdminExist(newAdminBO.getUsername());

        // 4. 调用service存入admin信息
        createAdminUser(newAdminBO);
        return GraceJSONResult.ok();
    }

    @Transactional
    public void createAdminUser(NewAdminBO newAdminBO) {

        String adminId = sid.nextShort();

        AdminUser adminUser = new AdminUser();
        adminUser.setId(adminId);
        adminUser.setUsername(newAdminBO.getUsername());
        adminUser.setAdminName(newAdminBO.getAdminName());

        // 如果密码不为空，则需要加密密码，存入数据库
        if (StringUtils.isNotBlank(newAdminBO.getPassword())) {
            String pwd = BCrypt.hashpw(newAdminBO.getPassword(), BCrypt.gensalt());
            adminUser.setPassword(pwd);
        }

        // 如果人脸上传以后，则有faceId，需要和admin信息关联存储入库
        if (StringUtils.isNotBlank(newAdminBO.getFaceId())) {
            adminUser.setFaceId(newAdminBO.getFaceId());
        }

        adminUser.setCreatedTime(new Date());
        adminUser.setUpdatedTime(new Date());

        int result = adminUserMapper.insert(adminUser);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.ADMIN_CREATE_ERROR);
        }
    }


    @Override
    public GraceJSONResult getAdminList(Integer page, Integer pageSize) {
        if (page == null) {
            page = Constant.COMMON_START_PAGE;
        }

        if (pageSize == null) {
            pageSize = Constant.COMMON_PAGE_SIZE;
        }

        PagedGridResult result = queryAdminList(page, pageSize);
        return GraceJSONResult.ok(result);
    }


    private PagedGridResult queryAdminList(Integer page, Integer pageSize) {
        Example adminExample = new Example(AdminUser.class);
        adminExample.orderBy("createdTime").desc();

        PageHelper.startPage(page, pageSize);
        List<AdminUser> adminUserList =
                adminUserMapper.selectByExample(adminExample);

        return setterPagedGrid(adminUserList, page);
    }

        private PagedGridResult setterPagedGrid(List<?> adminUserList,
                                            Integer page) {
            PageInfo<?> pageList = new PageInfo<>(adminUserList);
            PagedGridResult gridResult = new PagedGridResult();
            gridResult.setRows(adminUserList);
            gridResult.setPage(page);
            gridResult.setRecords(pageList.getTotal());
            gridResult.setTotal(pageList.getPages());
            return gridResult;
        }


    @Override
    public GraceJSONResult adminLogout(String adminId, HttpServletRequest request, HttpServletResponse response) {
        // 从redis中删除admin的会话token
        redis.del(Constant.REDIS_ADMIN_TOKEN + ":" + adminId);

        // 从cookie中清理adming登录的相关信息
        ControllerUtils.deleteCookie(request, response, "atoken");
        ControllerUtils.deleteCookie(request, response, "aid");
        ControllerUtils.deleteCookie(request, response, "aname");

        return GraceJSONResult.ok();
    }


    @Override
    public GraceJSONResult adminFaceLogin(AdminLoginBO adminLoginBO, HttpServletRequest request, HttpServletResponse response) {
        // 0. 判断用户名和人脸信息不能为空
        if (StringUtils.isBlank(adminLoginBO.getUsername())) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_USERNAME_NULL_ERROR);
        }
        String tempFace64 = adminLoginBO.getImg64();
        if (StringUtils.isBlank(tempFace64)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_FACE_NULL_ERROR);
        }

        // 1. 从数据库中查询出faceId
        AdminUser admin = queryAdminByUsername(adminLoginBO.getUsername());
        String adminFaceId = admin.getFaceId();

        if (StringUtils.isBlank(adminFaceId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_FACE_LOGIN_ERROR);
        }

        // 2. 请求文件服务，获得人脸数据的base64数据
        // TODO
        String fileServerUrlExecute
                = "http://files.imoocnews.com:8004/fs/readFace64InGridFS?faceId=" + adminFaceId;
        ResponseEntity<GraceJSONResult> responseEntity
                = restTemplate.getForEntity(fileServerUrlExecute, GraceJSONResult.class);
        GraceJSONResult bodyResult = responseEntity.getBody();
        String base64DB = (String)bodyResult.getData();


        // 3. 调用阿里ai进行人脸对比识别，判断可信度，从而实现人脸登录
        boolean result = faceVerifyUtils.faceVerify(FaceVerifyType.BASE64.type,
                tempFace64,
                base64DB,
                60);

        if (!result) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_FACE_LOGIN_ERROR);
        }

        // 4. admin登录后的数据设置，redis与cookie
        doLoginSettings(admin, request, response);

        return GraceJSONResult.ok();
    }
}
