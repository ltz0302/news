package com.ltz.news.service;

import com.ltz.news.pojo.Category;
import com.ltz.news.pojo.bo.NewArticleBO;
import com.ltz.news.utils.PagedGridResult;

import java.util.Date;

public interface IArticleService {

    /**
     * 发布文章
     */
    void createArticle(NewArticleBO newArticleBO, Category category);

    /**
     * 更新定时发布为即时发布
     */
    void updateAppointToPublish();


    /**
     * 用户中心 - 查询我的文章列表
     */
    PagedGridResult queryMyArticleList(String userId,
                                       String keyword,
                                       Integer status,
                                       Date startDate,
                                       Date endDate,
                                       Integer page,
                                       Integer pageSize);

    /**
     * 更改文章的状态
     */
    void updateArticleStatus(String articleId, Integer pendingStatus);

    /**
     * 关联文章和gridfs的html文件id
     */
    void updateArticleToGridFS(String articleId, String articleMongoId);

    /**
     * 管理员查询文章列表
     */
    PagedGridResult queryAllArticleListAdmin(Integer status, Integer page, Integer pageSize);

    /**
     * 删除文章
     */
    void deleteArticle(String userId, String articleId);

    /**
     * 撤回文章
     */
    void withdrawArticle(String userId, String articleId);

}
