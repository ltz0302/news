package com.ltz.news.controller;


import com.ltz.news.constant.Constant;
import com.ltz.news.controller.article.CommentControllerApi;
import com.ltz.news.controller.user.UserControllerApi;
import com.ltz.news.pojo.bo.CommentReplyBO;
import com.ltz.news.pojo.vo.AppUserVO;
import com.ltz.news.result.GraceJSONResult;
import com.ltz.news.service.ICommentPortalService;
import com.ltz.news.utils.JsonUtils;
import com.ltz.news.utils.PagedGridResult;
import com.ltz.news.utils.RedisOperator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;


import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
public class CommentController implements CommentControllerApi {


    @Autowired
    private ICommentPortalService commentPortalService;

    @Autowired
    private RedisOperator redis;

    @Autowired
    private UserControllerApi userControllerApi;

    @Override
    public GraceJSONResult createArticle(@Valid CommentReplyBO commentReplyBO) {

        // 1. 根据留言用户的id查询他的昵称，用于存入到数据表进行字段的冗余处理，从而避免多表关联查询的性能影响
        String userId = commentReplyBO.getCommentUserId();

        // 2. 发起远程调用用户服务，获得用户侧昵称
        Set<String> idSet = new HashSet<>();
        idSet.add(userId);
        String nickname = getBasicUserList(idSet).get(0).getNickname();
        String face = getBasicUserList(idSet).get(0).getFace();

        // 3. 保存用户评论的信息到数据库
        commentPortalService.createComment(commentReplyBO.getArticleId(),
                commentReplyBO.getFatherId(),
                commentReplyBO.getContent(),
                userId,
                nickname,
                face);

        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult commentCounts(String articleId) {
        Integer counts =
                getCountsFromRedis(Constant.REDIS_ARTICLE_COMMENT_COUNTS + ":" + articleId);

        return GraceJSONResult.ok(counts);
    }

    @Override
    public GraceJSONResult list(String articleId, Integer page, Integer pageSize) {
        if (page == null) {
            page = Constant.COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = Constant.COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = commentPortalService.queryArticleComments(articleId, page, pageSize);
        return GraceJSONResult.ok(gridResult);
    }

    @Override
    public GraceJSONResult mng(String writerId, Integer page, Integer pageSize) {
        if (page == null) {
            page = Constant.COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = Constant.COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = commentPortalService.queryWriterCommentsMng(writerId, page, pageSize);
        return GraceJSONResult.ok(gridResult);
    }

    @Override
    public GraceJSONResult delete(String writerId, String commentId) {
        commentPortalService.deleteComment(writerId, commentId);
        return GraceJSONResult.ok();
    }


    private List<AppUserVO> getBasicUserList(Set idSet) {

        GraceJSONResult bodyResult = userControllerApi.queryByIds(JsonUtils.objectToJson(idSet));
        List<AppUserVO> userVOList = null;
        if (bodyResult.getStatus() == 200) {
            String userJson = JsonUtils.objectToJson(bodyResult.getData());
            userVOList = JsonUtils.jsonToList(userJson, AppUserVO.class);
        }
        return userVOList;
    }

    private Integer getCountsFromRedis(String key) {
        String countsStr = redis.get(key);
        if (StringUtils.isBlank(countsStr)) {
            countsStr = "0";
        }
        return Integer.valueOf(countsStr);
    }
}
