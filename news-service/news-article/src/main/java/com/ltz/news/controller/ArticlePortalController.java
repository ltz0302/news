package com.ltz.news.controller;

import com.ltz.news.constant.Constant;
import com.ltz.news.controller.article.ArticlePortalControllerApi;
import com.ltz.news.controller.user.UserControllerApi;
import com.ltz.news.pojo.Article;
import com.ltz.news.pojo.vo.AppUserVO;
import com.ltz.news.pojo.vo.ArticleDetailVO;
import com.ltz.news.pojo.vo.IndexArticleVO;
import com.ltz.news.result.GraceJSONResult;
import com.ltz.news.service.IArticlePortalService;
import com.ltz.news.utils.IPUtil;
import com.ltz.news.utils.JsonUtils;
import com.ltz.news.utils.PagedGridResult;
import com.ltz.news.utils.RedisOperator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@RestController
public class ArticlePortalController implements ArticlePortalControllerApi {

    @Autowired
    private IArticlePortalService articlePortalService;

    @Autowired
    private RedisOperator redis;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public GraceJSONResult list(String keyword, Integer category, Integer page, Integer pageSize) {
        if (page == null) {
            page = Constant.COMMON_START_PAGE;
        }

        if (pageSize == null) {
            pageSize = Constant.COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult
                = articlePortalService.queryIndexArticleList(keyword,
                category,
                page,
                pageSize);

        gridResult = rebuildArticleGrid(gridResult);
        return GraceJSONResult.ok(gridResult);
    }

    private PagedGridResult rebuildArticleGrid(PagedGridResult gridResult) {
        // START

        List<Article> list = (List<Article>)gridResult.getRows();

        // 1. 构建发布者id列表
        Set<String> idSet = new HashSet<>();
        List<String> idList = new ArrayList<>();
        for (Article a : list) {
            // 1.1 构建发布者的set
            idSet.add(a.getPublishUserId());
            // 1.2 构建文章id的list
            idList.add(Constant.REDIS_ARTICLE_READ_COUNTS + ":" + a.getId());
        }
        // 发起redis的mget批量查询api，获得对应的值
        List<String> readCountsRedisList = redis.mget(idList);

        // 2. 发起远程调用（restTemplate），请求用户服务获得用户（idSet 发布者）的列表
        List<AppUserVO> publisherList = getPublisherList(idSet);
//

        // 3. 拼接两个list，重组文章列表
        List<IndexArticleVO> indexArticleList = new ArrayList<>();
        for (int i = 0 ; i < list.size() ; i ++) {
            IndexArticleVO indexArticleVO = new IndexArticleVO();
            Article a = list.get(i);
            BeanUtils.copyProperties(a, indexArticleVO);

            // 3.1 从publisherList中获得发布者的基本信息
            AppUserVO publisher  = getUserIfPublisher(a.getPublishUserId(), publisherList);
            indexArticleVO.setPublisherVO(publisher);

            // 3.2 重新组装设置文章列表中的阅读量
            String redisCountsStr = readCountsRedisList.get(i);
            int readCounts = 0;
            if (StringUtils.isNotBlank(redisCountsStr)) {
                readCounts = Integer.valueOf(redisCountsStr);
            }
            indexArticleVO.setReadCounts(readCounts);

            indexArticleList.add(indexArticleVO);
        }


        gridResult.setRows(indexArticleList);
// END
        return gridResult;
    }



    private AppUserVO getUserIfPublisher(String publisherId,
                                         List<AppUserVO> publisherList) {
        for (AppUserVO user : publisherList) {
            if (user.getId().equalsIgnoreCase(publisherId)) {
                return user;
            }
        }
        return null;
    }

    // 注入服务发现，可以获得已经注册的服务相关信息
    @Autowired
    private DiscoveryClient discoveryClient;

//    @Autowired
//    private UserControllerApi userControllerApi;

    // 发起远程调用，获得用户的基本信息
    private List<AppUserVO> getPublisherList(Set idSet) {

//        String serviceId = "SERVICE-USER";
//        List<ServiceInstance> instanceList = discoveryClient.getInstances(serviceId);
//        ServiceInstance userService = instanceList.get(0);

//        String userServerUrlExecute
//                = "http://" + serviceId + "/user/queryByIds?userIds=" + JsonUtils.objectToJson(idSet);

//        GraceJSONResult bodyResult = userControllerApi.queryByIds(JsonUtils.objectToJson(idSet));

//        String userServerUrlExecute
//                = "http://" + userService.getHost() +
//                ":"
//                + userService.getPort()
//                + "/user/queryByIds?userIds=" + JsonUtils.objectToJson(idSet);

        String userServerUrlExecute
                = "http://user.imoocnews.com:8003/user/queryByIds?userIds=" + JsonUtils.objectToJson(idSet);

        ResponseEntity<GraceJSONResult> responseEntity
                = restTemplate.getForEntity(userServerUrlExecute, GraceJSONResult.class);
        GraceJSONResult bodyResult = responseEntity.getBody();
        List<AppUserVO> publisherList = null;
        if (bodyResult.getStatus() == 200) {
            String userJson = JsonUtils.objectToJson(bodyResult.getData());
            publisherList = JsonUtils.jsonToList(userJson, AppUserVO.class);
        } else {
            publisherList = new ArrayList<>();
        }
        return publisherList;
    }

    @Override
    public GraceJSONResult hotList() {

        return GraceJSONResult.ok(articlePortalService.queryHotList());
    }

    @Override
    public GraceJSONResult queryArticleListOfWriter(String writerId, Integer page, Integer pageSize) {

        if (page == null) {
            page = Constant.COMMON_START_PAGE;
        }

        if (pageSize == null) {
            pageSize = Constant.COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = articlePortalService.queryArticleListOfWriter(writerId, page, pageSize);
        gridResult = rebuildArticleGrid(gridResult);
        return GraceJSONResult.ok(gridResult);
    }

    @Override
    public GraceJSONResult queryGoodArticleListOfWriter(String writerId) {

        PagedGridResult gridResult = articlePortalService.queryGoodArticleListOfWriter(writerId);
        return GraceJSONResult.ok(gridResult);
    }

    @Override
    public GraceJSONResult detail(String articleId) {
        ArticleDetailVO detailVO = articlePortalService.queryDetail(articleId);

        Set<String> idSet = new HashSet();
        idSet.add(detailVO.getPublishUserId());
        List<AppUserVO> publisherList = getPublisherList(idSet);

        if (!publisherList.isEmpty()) {
            detailVO.setPublishUserName(publisherList.get(0).getNickname());
        }

        detailVO.setReadCounts(
                getCountsFromRedis(Constant.REDIS_ARTICLE_READ_COUNTS + ":" + articleId));

        return GraceJSONResult.ok(detailVO);
    }

    private Integer getCountsFromRedis(String key) {
        String countsStr = redis.get(key);
        if (StringUtils.isBlank(countsStr)) {
            countsStr = "0";
        }
        return Integer.valueOf(countsStr);
    }

    @Override
    public Integer readCounts(String articleId) {
        System.out.println(articleId);
        return getCountsFromRedis(Constant.REDIS_ARTICLE_READ_COUNTS + ":" + articleId);
    }

    @Override
    public GraceJSONResult readArticle(String articleId, HttpServletRequest request) {
        String userIp = IPUtil.getRequestIp(request);
        // 设置针对当前用户ip的永久存在的key，存入到redis，表示该ip的用户已经阅读过了，无法累加阅读量
        redis.setnx(Constant.REDIS_ALREADY_READ + ":" +  articleId + ":" + userIp, userIp);

        redis.increment(Constant.REDIS_ARTICLE_READ_COUNTS + ":" + articleId, 1);
        return GraceJSONResult.ok();
    }
}
