package com.ltz.news.mapper;

import com.ltz.news.pojo.vo.PublisherVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AppUserMapperCustom {

    List<PublisherVO> getUserList(Map<String, Object> map);

}