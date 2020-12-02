package com.ltz.news.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexArticleVO {

    private String id;
    private String title;
    private Integer categoryId;
    private Integer articleType;
    private String articleCover;
    private Integer isAppoint;
    private Integer articleStatus;
    private String publishUserId;
    private Date publishTime;
    private Integer readCounts;
    private Integer commentCounts;
    private String mongoFileId;
    private Integer isDelete;
    private Date createTime;
    private Date updateTime;
    private String content;

    private AppUserVO publisherVO;

}