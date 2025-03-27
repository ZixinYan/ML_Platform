package com.ml.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ml.common.utils.PageUtils;
import com.ml.member.entity.MemberEntity;
import com.ml.member.entity.MemberLevelEntity;
import com.ml.member.exception.PhoneException;
import com.ml.member.exception.UsernameException;
import com.ml.member.vo.*;

import java.util.Map;

public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 用户注册
     * @param vo
     */
    void register(MemberUserRegisterVo vo);

    /**
     * 判断邮箱是否重复
     * @param phone
     * @return
     */
    void checkPhoneUnique(String phone) throws PhoneException;

    /**
     * 判断用户名是否重复
     * @param userName
     * @return
     */
    void checkUserNameUnique(String userName) throws UsernameException;

    /**
     * 用户登录
     * @param vo
     * @return
     */
    MemberEntity login(MemberUserLoginVo vo);

    /**
     * 社交用户的登录
     * @param weiboUser
     * @return
     */
    MemberEntity login(WeiboUser weiboUser) throws Exception;

    /**
     * 微信登录
     * @param wxUser
     * @return
     */
    MemberEntity login(WxUser wxUser) throws Exception;

    /**
     * Github登录
     * @param githubUser
     * @return
     */
    MemberEntity login(GithubUser githubUser) throws Exception;

    MemberLevelEntity getNewLevel(Integer newGrowthPoint);
}

