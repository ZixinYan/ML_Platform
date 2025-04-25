package com.ml.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ml.common.utils.PageUtils;
import com.ml.member.dto.IntegrationChangeHistoryDto;
import com.ml.member.entity.IntegrationChangeHistoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 积分变化历史记录
 */
public interface IntegrationChangeHistoryService extends IService<IntegrationChangeHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<IntegrationChangeHistoryDto> listByMemberId(Long memberId);
}

