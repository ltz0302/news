package com.ltz.news.service;

import com.ltz.news.pojo.Category;
import com.ltz.news.pojo.bo.SaveCategoryBO;
import com.ltz.news.result.GraceJSONResult;

import java.util.List;

public interface ICategoryService {

    GraceJSONResult saveOrUpdateCategory(SaveCategoryBO saveCategoryBO);

    /**
     * 新增文章分类
     */
    void createCategory(Category category);

    /**
     * 修改文章分类列表
     */
    void modifyCategory(Category category);


    /**
     * 获得文章分类列表
     */
    List<Category> queryCategoryList();

    GraceJSONResult getCats();
}
