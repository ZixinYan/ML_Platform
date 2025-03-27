package com.ml.gamegetflag.feign;

import com.ml.common.utils.R;
import com.ml.gamegetflag.vo.GrowthChangeHistoryVo;
import com.ml.gamegetflag.vo.IntegrationChangeHistoryVo;
import com.ml.gamegetflag.vo.MemberVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("member")
public interface MemberFeignService {

    @PostMapping("member/integrationchangehistory/save")
    R saveIntegration(@RequestBody IntegrationChangeHistoryVo integrationChangeHistory);

    @PostMapping("member/member/update")
    R updateMember(@RequestBody MemberVo memberVo);

    @PostMapping("member/growthchangehistory/save")
    R saveGrowth(@RequestBody GrowthChangeHistoryVo growthChangeHistoryVo);

    @GetMapping("member/member/info")
    R getMemberById(@RequestParam("id") Long id);
}
