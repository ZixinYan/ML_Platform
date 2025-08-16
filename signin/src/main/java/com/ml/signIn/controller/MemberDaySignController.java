package com.ml.signIn.controller;


import com.ml.common.utils.R;
import com.ml.signIn.vo.MemberDaySignInfoRes;
import com.ml.signIn.service.MemberDaySignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/member/daySign")
public class MemberDaySignController {
    @Autowired
    private MemberDaySignService memberDaySignService;

    /**
     * 用户签到
     * @param memberId
     * @return
     */
    @GetMapping(value = "/daySignIn")
    public R<Boolean> daySignIn(@RequestParam("memberId") Long memberId) {
        return R.ok(memberDaySignService.daySignIn(memberId));
    }


    /**
     * 查询用户连续签到信息（签到日历）
     * @param memberId
     * @return
     */
    @GetMapping(value = "/user/daySignInfo")
    public R<MemberDaySignInfoRes> daySignInfo(@RequestParam Long memberId) {
        MemberDaySignInfoRes userDaySignInfoRes = memberDaySignService.daySignInfo(memberId);
        return R.ok(userDaySignInfoRes);
    }

}
