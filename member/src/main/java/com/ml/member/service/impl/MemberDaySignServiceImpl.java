package com.ml.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ml.common.utils.DateUtils;
import com.ml.member.dao.DaySignGrowthAwardDao;
import com.ml.member.dao.GrowthChangeHistoryDao;
import com.ml.member.dao.MemberDao;
import com.ml.member.dao.MemberDaySignDao;
import com.ml.member.vo.MemberDaySignInfoRes;
import com.ml.member.vo.MemberDaySignRes;
import com.ml.member.entity.DaySignGrowthAwardEntity;
import com.ml.member.entity.MemberDaySignEntity;
import com.ml.member.entity.MemberEntity;
import com.ml.member.service.MemberDaySignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service("memberDaySignService")
public class MemberDaySignServiceImpl extends ServiceImpl<MemberDaySignDao, MemberDaySignEntity> implements MemberDaySignService {

    private static final String DAY_SIGN_LIST_KEY = "member:daySign:";
    private static final String LAST_SIGN_KEY = "member:lastSign:";
    private static final String CONTINUE_DAYS_KEY = "sign:continue:";
    private static final String START_SIGN_KEY = "sign:start:";


    @Autowired
    private GrowthChangeHistoryDao growthChangeHistoryDao;//暂时没写相关逻辑，下面有todo说明
    @Autowired
    private MemberDao memberDao ;
    @Autowired
    private DaySignGrowthAwardDao daySignGrowthAwardDao;
    @Autowired
    private StringRedisTemplate redisTemplate;


    private String getMonthKey(String date) {
        return DAY_SIGN_LIST_KEY + date.substring(0, 6);// member:daySign:{yyyyMM}
    }

    /**
     * 用户签到(基于redis)
     * @param memberId
     * @return true 签到成功，false 已签到过
     */
    @Override
    public Boolean daySignIn(Long memberId) {
        String todayDate= DateUtils.getTodayStr();
        int dayOfMonth = DateUtils.getDayOfMonth(todayDate);
        String daySignKey = getMonthKey(todayDate) + memberId;// member:daySign:{yyyyMM}:{memberId}

        //判断今日是否签到过，如果已经签到过，则不允许重复签到
        if (Boolean.TRUE.equals(redisTemplate.opsForValue().getBit(String.valueOf(memberId), dayOfMonth - 1))) {
            return false;
        }

        String yesterdayDate = DateUtils.getTodayStr();

        // Lua脚本搞里头，保证原子性操作（原设计是SessionCallback），此处换成RedisTemplate的execute方法，传入脚本和参数，咕咕嘎嘎
        String luaScript = "local todayBit = ARGV[1] " +
                "local memberId = ARGV[2] " +
                "local todayDate = ARGV[3] " +
                "local yesterdayDate = ARGV[4] " +
                "local lastSignKey = KEYS[1] " +
                "local continueKey = KEYS[2] " +
                "local startSignKey = KEYS[3] " +
                "local daySignKey = KEYS[4] " +
                "" +
                "redis.call('setbit', daySignKey, todayBit, 1) " +
                "" +
                "local lastSignDate = redis.call('get', lastSignKey) " +
                "local continueDays = 1 " +
                "" +
                "if lastSignDate == yesterdayDate then " +
                "continueDays = redis.call('incr', continueKey) " +
                "else " +
                "redis.call('set', continueKey, 1) " +
                "redis.call('set', startSignKey, todayDate) " +
                "end " +
                "" +
                "redis.call('set', lastSignKey, todayDate) " +
                "redis.call('expire', lastSignKey, 60*60*24*40) " + // 40天过期
                "redis.call('expire', continueKey, 60*60*24*40) " + // 40天过期
                "redis.call('expire', startSignKey, 60*60*24*40) " + //40天过期
                "return continueDays";
        Long continueDays = redisTemplate.execute(new DefaultRedisScript<>(luaScript, Long.class),
                Arrays.asList(
                        LAST_SIGN_KEY + memberId,// member:lastSign:{memberId}
                        CONTINUE_DAYS_KEY + memberId,// sign:continue:{memberId}
                        START_SIGN_KEY + memberId,// sign:start:{memberId}
                        daySignKey// member:daySign:{yyyyMM}:{memberId}
                ),
                Arrays.asList(
                        String.valueOf(dayOfMonth - 1),
                        String.valueOf(memberId),
                        todayDate,
                        yesterdayDate
                ));
        //增加成长值
        DaySignGrowthAwardEntity daySignGrowthAward = daySignGrowthAwardDao.selectOne(new QueryWrapper<DaySignGrowthAwardEntity>().eq("continue_day", continueDays));
        if(daySignGrowthAward!=null){
            MemberEntity member = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("member_id", memberId));
            member.setGrowth(member.getGrowth()+daySignGrowthAward.getGrowthAwardAmount());
            memberDao.updateById(member);
        }
        return true;

        /**  以下注释部分为原签到与数据库交互逻辑
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
         */
    }

    /**
     * 查询用户连续签到信息（签到日历）(基于redis)
     * @param memberId
     * @return
     */
    @Override
    public MemberDaySignInfoRes daySignInfo(Long memberId) {

        MemberDaySignInfoRes memberDaySignInfoRes = new MemberDaySignInfoRes();

        // 1. 获取连续签到天数
        String continueDaysStr = redisTemplate.opsForValue().get(CONTINUE_DAYS_KEY + memberId);
        int continueDays = continueDaysStr != null ? Integer.parseInt(continueDaysStr) : 0;

        // 2. 生成当月签到日历
        String todayDate = DateUtils.getTodayStr();
        String firstDayOfMonth = DateUtils.getFirstDayOfMonthStr();
        List<String> dateList = DateUtils.getDateStringsBetween(firstDayOfMonth, todayDate);

        List<MemberDaySignRes> calendarList = new ArrayList<>();
        String monthKey = getMonthKey(todayDate);
        String daySignKey = getMonthKey(todayDate) + memberId;// member:daySign:{yyyyMM}:{memberId}

        // 使用管道批量获取签到状态
        List<Object> results = redisTemplate.executePipelined((RedisCallback<Boolean>) connection -> {
            for (int i = 0; i < dateList.size(); i++) {
                connection.getBit(daySignKey.getBytes(), i);
            }
            return null;
        });
        List<Boolean> signStatusList = results.stream().map(Object::toString).map(Boolean::valueOf).collect(Collectors.toList());

        // 构建日历数据
        for (int i = 0; i < dateList.size(); i++) {
            MemberDaySignRes memberDaySignRes = new MemberDaySignRes();
            memberDaySignRes.setSignDate(dateList.get(i));
            memberDaySignRes.setSignStatus(Boolean.TRUE.equals(signStatusList.get(i)) ? 1 : 0);
            calendarList.add(memberDaySignRes);
        }

        //封装返回结果
        memberDaySignInfoRes.setContinueDay(continueDays);
        memberDaySignInfoRes.setCalendarList(calendarList);

        return memberDaySignInfoRes;

        /**以下注释部分为原查询签到日历与数据库交互逻辑


         //获取最后一次签到记录，用以计算连续天数
        QueryWrapper queryWrapper = new QueryWrapper<MemberDaySignEntity>().eq("member_id", memberId);
        queryWrapper.orderByDesc("sign_date");
        queryWrapper.last("limit 1");
        MemberDaySignEntity memberDayLastSign = memberDaySignDao.selectOne(queryWrapper);

         //获取本月签到记录，并以日期为key存入map中

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

         //封装返回结果

        memberDaySignInfoRes.setContinueDay(memberDayLastSign.getContinueDay());
        memberDaySignInfoRes.setCalendarList(daySignList);
        memberDaySignInfoRes.setGrowthRewardTotal(null);
        //TODO:setGrowthRewardTotal
        return memberDaySignInfoRes;

         */

    }


    /**
     * 计算总成长值奖励(该方法暂不采用)
     * @param memberId
     * @param calendar
     * @return
     */
    private Integer calculateTotalGrowth(Long memberId, List<MemberDaySignRes> calendar) {
        int total = 0;
        int currentStreak = 0;

        for (MemberDaySignRes day : calendar) {
            if (day.getSignStatus() == 1) {
                currentStreak++;
                DaySignGrowthAwardEntity award = daySignGrowthAwardDao.selectOne(
                        new QueryWrapper<DaySignGrowthAwardEntity>().eq("continue_day", currentStreak));
                if (award != null) {
                    total += award.getGrowthAwardAmount();
                }
            } else {
                currentStreak = 0;
            }
        }
        return total;
    }
}
