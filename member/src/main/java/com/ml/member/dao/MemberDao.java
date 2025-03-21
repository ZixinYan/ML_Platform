package com.ml.member.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ml.member.entity.MemberEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
