package com.ltz.news.mapper;

import com.ltz.news.my.mapper.MyMapper;
import com.ltz.news.pojo.AppUser;
import org.springframework.stereotype.Repository;


@Repository
public interface AppUserMapper extends MyMapper<AppUser> {
}