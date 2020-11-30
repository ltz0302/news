package com.ltz.news.pojo.bo;

import com.ltz.news.validate.CheckUrl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveFriendLinkBO {
    private String id;
    @NotBlank(message = "友情链接名不能为空")
    private String linkName;
    @NotBlank(message = "友情链接地址不能为空")
    @CheckUrl
    private String linkUrl;
    @NotNull(message = "请选择保留或删除")
    private Integer isDelete;

}