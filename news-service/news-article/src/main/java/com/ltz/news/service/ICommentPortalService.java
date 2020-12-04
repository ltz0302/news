package com.ltz.news.service;

import com.ltz.news.utils.PagedGridResult;

public interface ICommentPortalService {

    /**
     * 发表评论
     */
    void createComment(String articleId,
                              String fatherCommentId,
                              String content,
                              String userId,
                              String nickname,
                              String face);

    /**
     * 查询文章评论列表
     */
    PagedGridResult queryArticleComments(String articleId,
                                         Integer page,
                                         Integer pageSize);

    /**
     * 查询我的评论管理列表
     */
    PagedGridResult queryWriterCommentsMng(String writerId, Integer page, Integer pageSize);

    /**
     * 删除评论
     */
    void deleteComment(String writerId, String commentId);
}
