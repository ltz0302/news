package com.ltz.news.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUserVO {

    private String id;
    private String nickname;
    private String face;
    private Integer activeStatus;

    private Integer myFollowCounts;
    private Integer myFansCounts;

}
