package com.ltz.news.service;

import com.ltz.news.pojo.Article;
import com.ltz.news.pojo.vo.ArticleDetailVO;
import com.ltz.news.utils.PagedGridResult;

import java.util.List;

public interface IArticlePortalService {
    /**
     * 首页查询文章列表
     */
    PagedGridResult queryIndexArticleList(String keyword,
                                                 Integer category,
                                                 Integer page,
                                                 Integer pageSize);
    /**
     * 首页查询热闻列表
     */
    List<Article> queryHotList();

    /**
     * 查询作家发布的所有文章列表
     */
    PagedGridResult queryArticleListOfWriter(String writerId,
                                                    Integer page,
                                                    Integer pageSize);

    /**
     * 作家页面查询近期佳文
     */
    PagedGridResult queryGoodArticleListOfWriter(String writerId);

    /**
     * 查询文章详情
     */
    ArticleDetailVO queryDetail(String articleId);
}
