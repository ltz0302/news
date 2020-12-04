package com.ltz.news.service.impl;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ltz.news.constant.Constant;
import com.ltz.news.mapper.CommentsMapper;
import com.ltz.news.mapper.CommentsMapperCustom;
import com.ltz.news.pojo.Comments;
import com.ltz.news.pojo.vo.ArticleDetailVO;
import com.ltz.news.pojo.vo.CommentsVO;
import com.ltz.news.service.IArticlePortalService;
import com.ltz.news.service.ICommentPortalService;
import com.ltz.news.utils.PagedGridResult;
import com.ltz.news.utils.RedisOperator;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentPortalService implements ICommentPortalService {


    @Autowired
    private Sid sid;

    @Autowired
    private IArticlePortalService articlePortalService;

    @Autowired
    private CommentsMapper commentsMapper;

    @Autowired
    private CommentsMapperCustom commentsMapperCustom;

    @Autowired
    private RedisOperator redis;

    /**
     * 发表评论
     *
     * @param articleId
     * @param fatherCommentId
     * @param content
     * @param userId
     * @param nickname
     * @param face
     */
    @Transactional
    @Override
    public void createComment(String articleId, String fatherCommentId, String content, String userId, String nickname, String face) {
        String commentId = sid.nextShort();

        ArticleDetailVO article
                = articlePortalService.queryDetail(articleId);

        Comments comments = new Comments();
        comments.setId(commentId);

        comments.setWriterId(article.getPublishUserId());
        comments.setArticleTitle(article.getTitle());
        comments.setArticleCover(article.getCover());
        comments.setArticleId(articleId);

        comments.setFatherId(fatherCommentId);
        comments.setCommentUserId(userId);
        comments.setCommentUserNickname(nickname);
        comments.setCommentUserFace(face);

        comments.setContent(content);
        comments.setCreateTime(new Date());

        commentsMapper.insert(comments);

        // 评论数累加
        redis.increment(Constant.REDIS_ARTICLE_COMMENT_COUNTS + ":" + articleId, 1);
    }

    /**
     * 查询文章评论列表
     *
     * @param articleId
     * @param page
     * @param pageSize
     */
    @Override
    public PagedGridResult queryArticleComments(String articleId, Integer page, Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        map.put("articleId", articleId);

        PageHelper.startPage(page, pageSize);
        List<CommentsVO> list = commentsMapperCustom.queryArticleCommentList(map);
        return setterPagedGrid(list, page);
    }

    /**
     * 查询我的评论管理列表
     *
     * @param writerId
     * @param page
     * @param pageSize
     */
    @Override
    public PagedGridResult queryWriterCommentsMng(String writerId, Integer page, Integer pageSize) {
        Comments comment = new Comments();
        comment.setWriterId(writerId);

        PageHelper.startPage(page, pageSize);
        List<Comments> list = commentsMapper.select(comment);
        return setterPagedGrid(list, page);
    }

    /**
     * 删除评论
     *
     * @param writerId
     * @param commentId
     */
    @Override
    public void deleteComment(String writerId, String commentId) {
        Comments comment = new Comments();
        comment.setId(commentId);
        comment.setWriterId(writerId);

        commentsMapper.delete(comment);
    }


    private PagedGridResult setterPagedGrid(List<?> list,
                                            Integer page) {
        PageInfo<?> pageList = new PageInfo<>(list);
        PagedGridResult gridResult = new PagedGridResult();
        gridResult.setRows(list);
        gridResult.setPage(page);
        gridResult.setRecords(pageList.getTotal());
        gridResult.setTotal(pageList.getPages());
        return gridResult;
    }
}
