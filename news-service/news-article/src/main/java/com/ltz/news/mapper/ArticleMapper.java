package com.ltz.news.mapper;

import com.ltz.news.my.mapper.MyMapper;
import com.ltz.news.pojo.Article;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleMapper extends MyMapper<Article> {
}