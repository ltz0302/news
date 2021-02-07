package com.ltz.news.service;

import com.ltz.news.pojo.Category;
import com.ltz.news.pojo.bo.SaveCategoryBO;
import com.ltz.news.result.GraceJSONResult;

import java.util.List;

public interface ICategoryService {

    GraceJSONResult saveOrUpdateCategory(SaveCategoryBO saveCategoryBO);

    /**
     * 获得文章分类列表
     */
    List<Category> queryCategoryList();

    GraceJSONResult getCats();
}
