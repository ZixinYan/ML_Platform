package com.ml.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ml.member.vo.MemberDaySignInfoRes;
import com.ml.member.entity.MemberDaySignEntity;
import org.springframework.stereotype.Service;

@Service
public interface MemberDaySignService extends IService<MemberDaySignEntity> {
    MemberDaySignInfoRes daySignInfo(Long memberId);
    Boolean daySignIn(Long memberId);
}
