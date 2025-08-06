package com.ml.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ml.common.utils.DateUtils;
import com.ml.member.dao.DaySignGrowthAwardDao;
import com.ml.member.dao.GrowthChangeHistoryDao;
import com.ml.member.dao.MemberDao;
import com.ml.member.dao.MemberDaySignDao;
import com.ml.member.dto.MemberDaySignInfoRes;
import com.ml.member.dto.MemberDaySignRes;
import com.ml.member.entity.DaySignGrowthAwardEntity;
import com.ml.member.entity.MemberDaySignEntity;
import com.ml.member.entity.MemberEntity;
import com.ml.member.service.MemberDaySignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service("memberDaySignService")
public class MemberDaySignServiceImpl extends ServiceImpl<MemberDaySignDao, MemberDaySignEntity> implements MemberDaySignService {
    @Autowired
    private MemberDaySignDao memberDaySignDao;
    @Autowired
    private GrowthChangeHistoryDao growthChangeHistoryDao;//暂时没写相关逻辑，下面有todo说明
    @Autowired
    private MemberDao memberDao ;
    @Autowired
    private DaySignGrowthAwardDao daySignGrowthAwardDao;



    /**
     * 用户签到
     * @param memberId
     * @return true 签到成功，false 已签到过
     */
    @Override
    public Boolean daySignIn(Long memberId) {
        String todayDate= DateUtils.getTodayStr();
        QueryWrapper queryWrapper = new QueryWrapper<MemberDaySignEntity>().eq("member_id", memberId);
        //查询最后一次签到记录
        queryWrapper.orderByDesc("sign_date");
        queryWrapper.last("limit 1");
        MemberDaySignEntity memberDaySign = memberDaySignDao.selectOne(queryWrapper);
        //判断今天是否签到过
        if(!memberDaySign.getSignDate().equals(todayDate)){
            MemberDaySignEntity memberDaySignUpdate = new MemberDaySignEntity();
            //判断是否连续签到
            if(memberDaySign.getContinueDay()>0){
                memberDaySignUpdate.setContinueDay(memberDaySign.getContinueDay()+1);
                memberDaySignUpdate.setSignStatus(1);//今日签到
                memberDaySignUpdate.setStartSignDate(memberDaySign.getStartSignDate());
            }else{
                memberDaySignUpdate.setContinueDay(1);
                memberDaySignUpdate.setStartSignDate(todayDate);
                memberDaySignUpdate.setSignStatus(1);
            }
            memberDaySignUpdate.setMemberId(memberId);
            memberDaySignUpdate.setSignDate(todayDate);
            memberDaySignDao.insert(memberDaySignUpdate);
            //增加成长值
            MemberEntity member = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("member_id", memberId));
            QueryWrapper<DaySignGrowthAwardEntity> awardqueryWrapper = new QueryWrapper<>();
            awardqueryWrapper.eq("continue_day",memberDaySignUpdate.getContinueDay());
            DaySignGrowthAwardEntity daySignGrowthAward = daySignGrowthAwardDao.selectOne(awardqueryWrapper);
            member.setGrowth(member.getGrowth()+daySignGrowthAward.getGrowthAwardAmount());
            memberDao.updateById(member);

            //TODO：这里没写成长值变化历史的记录，因为我没细看growthChangeHistory这个实体类，但是已经注入进来了，以后有时间写

        }else{
            return false;//已经签到过
        }
        return true;//签到成功
    }

    /**
     * 查询用户连续签到信息（签到日历）
     * @param memberId
     * @return
     */
    @Override
    public MemberDaySignInfoRes daySignInfo(Long memberId) {
        /**
         * 获取最后一次签到记录，用以计算连续天数
         */
        QueryWrapper queryWrapper = new QueryWrapper<MemberDaySignEntity>().eq("member_id", memberId);
        queryWrapper.orderByDesc("sign_date");
        queryWrapper.last("limit 1");
        MemberDaySignEntity memberDayLastSign = memberDaySignDao.selectOne(queryWrapper);

        /**
         * 获取本月签到记录，并以日期为key存入map中
         */
        String todayDate = DateUtils.getTodayStr();
        String firstDayOfMonth = DateUtils.getFirstDayOfMonthStr();
        List<String> DatesInMonth = DateUtils.getDateStringsBetween(firstDayOfMonth,todayDate);
        List<MemberDaySignRes> daySignList = new ArrayList();
        QueryWrapper daysign = new QueryWrapper<MemberDaySignEntity>().eq("member_id", memberId)
                .ge("sign_date",firstDayOfMonth).le("sign_date",todayDate).orderByAsc("sign_date");
        List<MemberDaySignEntity> signRecords = memberDaySignDao.selectList(daysign);
        Map<String, MemberDaySignEntity> signRecordMap = signRecords.stream()
                .collect(Collectors.toMap(MemberDaySignEntity::getSignDate, Function.identity()));
        for(String date:DatesInMonth){
            MemberDaySignRes day = new MemberDaySignRes();
            day.setSignDate(date);
            if(signRecordMap.containsKey(date)){
                day.setSignStatus(signRecordMap.get(date).getSignStatus());
            }
            daySignList.add(day);
        }

        /**
         * 封装返回结果
         */
        MemberDaySignInfoRes memberDaySignInfoRes = new MemberDaySignInfoRes();
        memberDaySignInfoRes.setContinueDay(memberDayLastSign.getContinueDay());
        memberDaySignInfoRes.setCalendarList(daySignList);
        memberDaySignInfoRes.setGrowthRewardTotal(null);
        //TODO:setGrowthRewardTotal
        return memberDaySignInfoRes;
    }


}
