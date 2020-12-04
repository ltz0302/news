package com.ltz.news.mapper;

import com.ltz.news.my.mapper.MyMapper;
import com.ltz.news.pojo.Comments;
import com.ltz.news.pojo.vo.CommentsVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CommentsMapperCustom extends MyMapper<Comments> {

    /**
     * 查询文章评论
     */
    List<CommentsVO> queryArticleCommentList(@Param("paramMap") Map<String, Object> map);

}