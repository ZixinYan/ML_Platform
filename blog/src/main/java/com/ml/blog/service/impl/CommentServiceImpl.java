package com.ml.blog.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.additional.query.impl.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ml.blog.dao.CommentDao;
import com.ml.blog.entity.CommentEntity;
import com.ml.blog.fegin.MemberFeignService;
import com.ml.blog.interceptor.LoginUserInterceptor;
import com.ml.blog.service.CommentService;
import com.ml.blog.vo.CommentVo;
import com.ml.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentDao, CommentEntity> implements CommentService {
    @Autowired
    private MemberFeignService memberClient;

    @Override
    public R getComments(Long id) {
        // 1. 根据 BlogId 查询所有 **未被删除的** 评论
        List<CommentEntity> comments = this.list(new LambdaQueryWrapper<CommentEntity>()
                .eq(CommentEntity::getBlogId, id)
                .eq(CommentEntity::getStatus, 0)); // 只查询未删除的评论

        // 2. 找出所有 **一级评论**（parentId = 0）
        List<CommentVo> commentVos = comments.stream()
                .filter(comment -> comment.getParentId() == 0)
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 3. 获取 **子评论**
        for (CommentVo commentVo : commentVos) {
            commentVo.setReplies(getReplies(commentVo.getId(), comments));
        }

        return R.ok(commentVos);
    }


    @Override
    public R deleteComments(Long id) {
        boolean success = this.removeById(id);
        return success ? R.ok() : R.error();
    }



    private List<CommentVo> getReplies(Long parentId, List<CommentEntity> comments) {
        return comments.stream()
                .filter(comment -> comment.getParentId().equals(parentId) && comment.getStatus() == 0) // 过滤已删除
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    private CommentVo convertToVO(CommentEntity entity) {
        CommentVo vo = new CommentVo();
        vo.setId(entity.getId());
        vo.setContent(entity.getContent());
        vo.setUserId(entity.getUserId());
        vo.setBlogId(entity.getBlogId());
        vo.setParentId(entity.getParentId());
        vo.setAnswerId(entity.getAnswerId());
        vo.setCreateTime(entity.getCreateTime());

        // 通过 Feign 获取 当前评论的用户信息
        R userResponse = memberClient.getUserInfo(entity.getUserId());
        if (userResponse.getCode() == 0) {
            JSONObject data = JSON.parseObject(JSON.toJSONString(userResponse.getData()));
            vo.setUsername(data.getString("username"));
            vo.setUserAvatar(data.getString("avatar"));
        }

        // 查询被回复的人（answerId 指向的评论）
        if (entity.getAnswerId() != null && entity.getAnswerId() > 0) {
            CommentEntity answeredComment = this.getById(entity.getAnswerId());
            if (answeredComment != null) {
                R answeredUserResponse = memberClient.getUserInfo(answeredComment.getUserId());
                if (answeredUserResponse.getCode() == 0) {
                    JSONObject answeredData = JSON.parseObject(JSON.toJSONString(answeredUserResponse.getData()));
                    vo.setAnswerName(answeredData.getString("username"));
                }
            }
        }

        return vo;
    }

}

