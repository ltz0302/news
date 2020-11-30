package com.ltz.news.pojo.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveCategoryBO {
    private Integer id;
    @NotBlank(message = "分类名不能为空")
    private String name;
    private String oldName;
    @NotBlank(message = "分类颜色不能为空")
    private String tagColor;

}
