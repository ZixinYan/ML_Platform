package com.ml.blog.controller;

import com.ml.blog.entity.CommentEntity;
import com.ml.blog.interceptor.LoginUserInterceptor;
import com.ml.blog.service.CommentService;
import com.ml.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/blog/comments")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @GetMapping("/list")
    public R getComments(@RequestParam Long id) {
        return  commentService.getComments(id);
    }

    @PutMapping("/save")
    public R saveComments(@RequestBody CommentEntity commentEntity) {
        Long userId = LoginUserInterceptor.loginUser.get().getId();
        commentEntity.setUserId(userId);
        if(commentEntity.getAnswerId() == null) {
            commentEntity.setParentId(0L);
        }else {
            commentEntity.setParentId(1L);
        }
        if(commentService.save(commentEntity)){
            return R.ok();
        }else{
            return R.error();
        }
    }

    @DeleteMapping("/delete")
    public R deleteComments(@RequestParam Long id) {
        return commentService.deleteComments(id);
    }

}
