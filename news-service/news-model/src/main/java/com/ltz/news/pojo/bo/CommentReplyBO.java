package com.ltz.news.pojo.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * 文章留言的BO
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentReplyBO {

    @NotBlank(message = "留言信息不完整")
    private String articleId;

    @NotBlank(message = "留言信息不完整")
    private String fatherId;

    @NotBlank(message = "当前用户信息不正确，请尝试重新登录")
    private String commentUserId;

    @NotBlank(message = "留言内容不能为空")
    @Length(max = 50, message = "文章内容长度不能超过50")
    private String content;

    @Override
    public String toString() {
        return "CommentReplyBO{" +
                "articleId='" + articleId + '\'' +
                ", fatherId='" + fatherId + '\'' +
                ", commentUserId='" + commentUserId + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}