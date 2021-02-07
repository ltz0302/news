package com.ltz.news.repository;

import com.ltz.news.pojo.mo.FriendLinkMO;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FriendLinkRepository extends MongoRepository<FriendLinkMO, String> {

    List<FriendLinkMO> getAllByIsDelete(Integer isDelete);

}