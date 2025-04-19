package com.ml.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ml.blog.anno.CacheDelete;
import com.ml.blog.config.BlogSearchRepository;
import com.ml.blog.entity.BlogEntity;
import com.ml.blog.interceptor.LoginUserInterceptor;
import com.ml.blog.service.BlogService;
import com.ml.blog.constant.SystemConstants;
import com.ml.common.utils.PageUtils;
import com.ml.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/blog/blog")
public class BlogController {

    @Autowired
    private BlogService blogService;

    @Autowired
    private BlogSearchRepository blogSearchRepository;

    @PutMapping("/like/{id}")
    public R like(@PathVariable("id") Long id) {
        // 修改点赞数量
        return blogService.likeBlog(id);
    }

    @GetMapping("/hot")
    public R queryHotBlog(@RequestParam(value = "current", defaultValue = "0") Integer current) {
        //查询热门文章
         return blogService.queryHotBlog(current);
    }

    @GetMapping("/of/me")
    public R queryMyBlog(@RequestParam(value = "current", defaultValue = "0") Integer current) {
        // 获取当前登录用户 ID
        Long userId = LoginUserInterceptor.loginUser.get().getId();
        // 创建分页对象
        Page<BlogEntity> page = new Page<>(current, SystemConstants.MAX_PAGE_SIZE);
        // 构造查询条件
        LambdaQueryWrapper<BlogEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BlogEntity::getCreateUser, userId);
        // 分页查询
        Page<BlogEntity> resultPage = (Page<BlogEntity>) blogService.page(page, queryWrapper);
        // 获取当前页数据
        List<BlogEntity> records = resultPage.getRecords();
        // 返回结果
        return R.ok(records);
    }
    // 检查特定博客
    @GetMapping("/check/{id}")
    public R queryBlogById(@PathVariable("id") Long id) {
        return blogService.queryBlogById(id);
    }
    /**
     * 获取前五个点赞的人用来显示在前端界面
     * @Param nickname
     * @Param avatar
      */

    @GetMapping("/likes/{id}")
    public R queryBlogLikesById(@PathVariable("id") Long id) {
        return blogService.queryBlogLikesById(id);
    }
    // 查看特定用户的
    @GetMapping("/of/user")
    public R queryBlogByUserId(@RequestParam(value = "current",defaultValue = "0")Integer current,
                                    @RequestParam("id")Long id){
        // 创建分页对象
        Page<BlogEntity> page = new Page<>(current, SystemConstants.MAX_PAGE_SIZE);
        // 构造查询条件
        LambdaQueryWrapper<BlogEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BlogEntity::getCreateUser, id);
        // 分页查询
        Page<BlogEntity> resultPage = (Page<BlogEntity>) blogService.page(page, queryWrapper);
        // 获取当前页数据
        List<BlogEntity> records = resultPage.getRecords();
        // 返回结果
        return R.ok(records);
    }

    @GetMapping("/search")
    public R search(
            @RequestParam(value = "current", defaultValue = "0") Integer current,
            @RequestParam(value = "keywords") String[] keywords
    ) {
        PageUtils page = blogService.searchBlog(current, keywords);
        return R.ok(page);
    }


    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        //查询列表数据
        PageUtils page = blogService.queryPage(params);
        return R.ok(page);
    }

    @GetMapping("/info")
    public R info(@RequestParam("id") Long id){
        //查询信息
        BlogEntity Blog = blogService.getById(id);
        return R.ok(Blog);
    }

    @PostMapping("/save")
    @Transactional
    @CacheDelete(keys = {
            "'blog:hot:*'",
            "'blog:search:*'"
    })
    public R save(@RequestBody @Valid BlogEntity blog) {
        boolean result = blogService.save(blog);
        if (!result) {
            return R.error();
        }
        blogSearchRepository.save(blog);
        return R.ok();
    }



    @PutMapping("/update")
    public R update(@RequestBody @Valid BlogEntity blog) {
        // 更新数据库中的信息
        if(blogService.updateBlog(blog)) {
            return R.ok();
        }else {
            return R.error();
        }
    }


    @DeleteMapping("/delete")
    @Transactional
    public R delete(@RequestParam("id") Long id) {
        // 先删除数据库中的数据
        if(blogService.deleteBlog(id)){
            return R.ok();
        }else{
            return R.error();
        }

    }



}
