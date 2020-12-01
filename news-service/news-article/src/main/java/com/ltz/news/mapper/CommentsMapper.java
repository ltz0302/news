package com.ltz.news.mapper;

import com.ltz.news.my.mapper.MyMapper;
import com.ltz.news.pojo.Comments;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentsMapper extends MyMapper<Comments> {
}