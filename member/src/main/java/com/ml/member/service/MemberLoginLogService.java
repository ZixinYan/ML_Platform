package com.ml.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ml.common.utils.PageUtils;
import com.ml.common.utils.PageUtils;
import com.ml.member.entity.MemberLoginLogEntity;

import java.util.Map;

/**
 * 会员登录记录
 *
 */
public interface MemberLoginLogService extends IService<MemberLoginLogEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

