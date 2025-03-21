package com.ml.member.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ml.member.entity.MemberLevelEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 *
 */
@Mapper
public interface MemberLevelDao extends BaseMapper<MemberLevelEntity> {

    MemberLevelEntity getDefaultLevel();
}
