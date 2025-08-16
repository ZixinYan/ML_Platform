package com.ml.signIn.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ml.signIn.vo.MemberDaySignInfoRes;
import com.ml.signIn.entity.MemberDaySignEntity;
import org.springframework.stereotype.Service;

@Service
public interface MemberDaySignService extends IService<MemberDaySignEntity> {
    MemberDaySignInfoRes daySignInfo(Long memberId);
    Boolean daySignIn(Long memberId);
}
