package com.ml.blog.controller;

import com.ml.blog.entity.CategoryEntity;
import com.ml.blog.service.CategoryService;
import com.ml.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/blog/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    public R getCategoryList() {
        List<CategoryEntity> list = categoryService.list();
        return R.ok(list);
    }
    @GetMapping("/info")
    public R getCategoryInfo(Long id) {
        CategoryEntity category = categoryService.getById(id);
        return R.ok(category);
    }

    @PostMapping("/save")
    public R saveCategory(@RequestBody CategoryEntity category) {
        categoryService.save(category);
        return R.ok();
    }

    @PutMapping("/update")
    public R updateCategory(@RequestBody CategoryEntity category) {
        categoryService.updateById(category);
        return R.ok();
    }

    @DeleteMapping("/delete")
    public R deleteCategory(Long id) {
        categoryService.removeById(id);
        return R.ok();
    }
}
