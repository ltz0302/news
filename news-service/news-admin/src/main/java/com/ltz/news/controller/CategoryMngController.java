package com.ltz.news.controller;

import com.ltz.news.controller.admin.CategoryMngControllerApi;
import com.ltz.news.pojo.Category;
import com.ltz.news.pojo.bo.SaveCategoryBO;
import com.ltz.news.result.GraceJSONResult;
import com.ltz.news.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
public class CategoryMngController implements CategoryMngControllerApi {

    @Autowired
    private ICategoryService categoryService;

    @Override
    public GraceJSONResult saveOrUpdateCategory(@Valid SaveCategoryBO saveCategoryBO) {
        return categoryService.saveOrUpdateCategory(saveCategoryBO);
    }

    @Override
    public GraceJSONResult getCatList() {

        List<Category> categoryList = categoryService.queryCategoryList();
        return GraceJSONResult.ok(categoryList);
    }

    @Override
    public GraceJSONResult getCats() {

        return categoryService.getCats();
    }
}
