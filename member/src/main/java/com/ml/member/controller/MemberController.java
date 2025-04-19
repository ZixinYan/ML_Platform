package com.ml.member.controller;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.ml.common.exception.BizCodeEnum;
import com.ml.common.utils.PageUtils;
import com.ml.common.utils.R;
import com.ml.member.entity.MemberEntity;
import com.ml.member.entity.MemberLevelEntity;
import com.ml.member.exception.PhoneException;
import com.ml.member.exception.UsernameException;
import com.ml.member.service.MemberService;
import com.ml.member.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping("/member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;


    @PostMapping(value = "/register")
    public R register(@RequestBody MemberUserRegisterVo vo) {
        try {
            memberService.register(vo);
        } catch (PhoneException e) {
            return R.error(BizCodeEnum.PHONE_EXISTS_EXCEPTION.getCode(),BizCodeEnum.PHONE_EXISTS_EXCEPTION.getMsg());
        } catch (UsernameException e) {
            return R.error(BizCodeEnum.USER_EXISTS_EXCEPTION.getCode(),BizCodeEnum.USER_EXISTS_EXCEPTION.getMsg());
        }

        return R.ok();
    }


    @PostMapping(value = "/login")
    public R login(@RequestBody MemberUserLoginVo vo) {
        log.info(vo.getUserName());
        log.info(vo.getPassword());
        MemberEntity memberEntity = memberService.login(vo);
        if (memberEntity != null) {
            log.info(memberEntity.toString());
            return R.ok().setData(memberEntity);
        } else {
            return R.error(BizCodeEnum.LOGIN_ACCOUNT_PASSWORD_INVALID.getCode(),BizCodeEnum.LOGIN_ACCOUNT_PASSWORD_INVALID.getMsg());
        }
    }


    @PostMapping(value = "/weibo/login")
    public R oauthLogin(@RequestBody WeiboUser weiboUser) throws Exception {

        MemberEntity memberEntity = memberService.login(weiboUser);

        if (memberEntity != null) {
            return R.ok().setData(memberEntity);
        } else {
            return R.error(BizCodeEnum.LOGIN_ACCOUNT_PASSWORD_INVALID.getCode(),BizCodeEnum.LOGIN_ACCOUNT_PASSWORD_INVALID.getMsg());
        }
    }
    @PostMapping(value = "/weixin/login")
    public R weixinLogin(@RequestBody WxUser wxUser) throws Exception {

        MemberEntity memberEntity = memberService.login(wxUser);
        if (memberEntity != null) {
            return R.ok().setData(memberEntity);
        } else {
            return R.error(BizCodeEnum.LOGIN_ACCOUNT_PASSWORD_INVALID.getCode(),BizCodeEnum.LOGIN_ACCOUNT_PASSWORD_INVALID.getMsg());
        }
    }
    @PostMapping(value = "/github/login")
    public R githubLogin(@RequestBody GithubUser githubUser) throws Exception {
        MemberEntity memberEntity = memberService.login(githubUser);
        if (memberEntity != null) {
            log.info(memberEntity.toString());
            return R.ok().setData(memberEntity);
        }else{
            return R.error(BizCodeEnum.LOGIN_ACCOUNT_PASSWORD_INVALID.getCode(), BizCodeEnum.LOGIN_ACCOUNT_PASSWORD_INVALID.getMsg());
        }
    }
    /**
     * 列表
     */
    @GetMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam List<Long> memberIds){
        String join = StrUtil.join(",", memberIds);
        List<MemberShowVo> memberVo = memberService.lambdaQuery()
                .in(MemberEntity::getId, memberIds)
                .last("order by field(id,"+ join +")")
                .list()
                .stream().map(member ->
                        BeanUtil.copyProperties(member, MemberShowVo.class)
                ).collect(Collectors.toList());
        return R.ok(memberVo);
    }
    /**
     * 信息
     */
    @GetMapping("/info")
    public R info(@RequestParam("id")  Long id){
		MemberEntity member = memberService.getById(id);
        return R.ok(member);
    }
    /**
     * 保存
     */
    @PostMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);
        return R.ok();
    }
    /**
     * 修改
     */
    @PostMapping("/update")
    public R update(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        // 查询用户当前信息
        MemberEntity member = memberService.getById(id);
        if (member == null) {
            return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.UNKNOWN_EXCEPTION.getMsg());
        }
        // 创建更新条件
        UpdateWrapper<MemberEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        // 是否有成长值更新
        Integer newGrowthPoint = null;
        if (params.containsKey("growth")) {
            newGrowthPoint = Integer.valueOf(params.get("growth").toString());
            if (!newGrowthPoint.equals(member.getGrowth())) {
                updateWrapper.set("growth", newGrowthPoint);
            }
        }
        // 遍历所有参数，更新非空字段
        params.forEach((key, value) -> {
            if (value != null && !"growth".equals(key)) { // 避免重复更新
                updateWrapper.set(key, value);
            }
        });
        // 检查是否需要升级等级
        if (newGrowthPoint != null) {
            MemberLevelEntity newLevel = memberService.getNewLevel(newGrowthPoint);
            if (newLevel != null && !newLevel.getId().equals(member.getLevelId())) {
                updateWrapper.set("level_id", newLevel.getId());
                log.info("用户 " + id + " 升级为：" + newLevel.getName());
            }else{
                log.info("不需要升级");
            }
        }

        // 更新更新时间
        updateWrapper.set("update_time", new Date());

        // 执行数据库更新
        boolean updated = memberService.update(updateWrapper);
        return updated ? R.ok() : R.error();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

}
