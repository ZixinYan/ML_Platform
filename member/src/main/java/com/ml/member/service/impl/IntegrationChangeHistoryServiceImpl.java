package com.ml.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ml.common.utils.PageUtils;
import com.ml.common.utils.Query;
import com.ml.member.dao.IntegrationChangeHistoryDao;
import com.ml.member.dto.IntegrationChangeHistoryDto;
import com.ml.member.entity.IntegrationChangeHistoryEntity;
import com.ml.member.service.IntegrationChangeHistoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("integrationChangeHistoryService")
public class IntegrationChangeHistoryServiceImpl extends ServiceImpl<IntegrationChangeHistoryDao, IntegrationChangeHistoryEntity> implements IntegrationChangeHistoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<IntegrationChangeHistoryEntity> page = this.page(
                new Query<IntegrationChangeHistoryEntity>().getPage(params),
                new QueryWrapper<IntegrationChangeHistoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<IntegrationChangeHistoryDto> listByMemberId(Long memberId) {
        List<IntegrationChangeHistoryEntity> entityList = this.list(new QueryWrapper<IntegrationChangeHistoryEntity>()
                .eq("member_id", memberId)
                .orderByDesc("create_time"));

        // 将 Entity 列表转为 DTO 列表
        return entityList.stream()
                .map(this::entityToDto)  // 映射每个实体到 DTO
                .collect(Collectors.toList());
    }
    private IntegrationChangeHistoryDto entityToDto(IntegrationChangeHistoryEntity entity) {
        IntegrationChangeHistoryDto dto = new IntegrationChangeHistoryDto();
        dto.setId(entity.getId());
        dto.setMemberId(entity.getMemberId());
        dto.setCreateTime(entity.getCreateTime());
        dto.setChangeCount(entity.getChangeCount());
        dto.setNote(entity.getNote());
        dto.setSourceType(entity.getSourceType());
        return dto;
    }


}