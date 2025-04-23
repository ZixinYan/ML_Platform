package com.ml.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.ml.common.utils.HttpUtils;
import com.ml.common.utils.PageUtils;
import com.ml.common.utils.Query;
import com.ml.member.dao.MemberDao;
import com.ml.member.dao.MemberLevelDao;
import com.ml.member.dao.MemberLoginLogDao;
import com.ml.member.entity.MemberEntity;
import com.ml.member.entity.MemberLevelEntity;
import com.ml.member.entity.MemberLoginLogEntity;
import com.ml.member.exception.PhoneException;
import com.ml.member.exception.UsernameException;
import com.ml.member.service.MemberService;
import com.ml.member.utils.HttpClientUtils;
import com.ml.member.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Resource
    private MemberLevelDao memberLevelDao;

    @Resource
    private MemberLoginLogDao memberLoginLogDao;

    @Autowired
    private LevelCacheServiceImpl levelCacheService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberUserRegisterVo vo) {

        MemberEntity memberEntity = new MemberEntity();

        //设置默认等级
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(levelEntity.getId());

        //设置其它的默认信息
        //检查用户名和手机号是否唯一。感知异常，异常机制
        checkPhoneUnique(vo.getPhone());
        checkUserNameUnique(vo.getUserName());

        memberEntity.setNickname(vo.getUserName());
        memberEntity.setUsername(vo.getUserName());
        //密码进行MD5加密
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);
        memberEntity.setMobile(vo.getPhone());
        memberEntity.setGender(0);
        memberEntity.setCreateTime(new Date());

        //保存数据
        this.baseMapper.insert(memberEntity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneException {

        int phoneCount = Math.toIntExact(this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone)));

        if (phoneCount > 0) {
            throw new PhoneException();
        }

    }

    @Override
    public void checkUserNameUnique(String userName) throws UsernameException {

        int usernameCount = Math.toIntExact(this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName)));

        if (usernameCount > 0) {
            throw new UsernameException();
        }
    }

    @Override
    public MemberEntity login(MemberUserLoginVo vo) {
        String loginAccount = vo.getUserName();
        String password = vo.getPassword();
        Integer type = vo.getType();
        //1、去数据库查询 SELECT * FROM member_info WHERE username = ? OR mobile = ?
        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>()
                .eq("username", loginAccount).or().eq("mobile", loginAccount));

        if (memberEntity == null) {
            //登录失败
            return null;
        } else {
            //获取到数据库里的password
            String password1 = memberEntity.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            //进行密码匹配
            boolean matches = passwordEncoder.matches(password, password1);
            if (matches) {
                //登录成功,记录登录历史
                MemberLoginLogEntity loginLogEntity = new MemberLoginLogEntity();
                loginLogEntity.setMemberId(memberEntity.getId());
                loginLogEntity.setIp(vo.getIP());
                loginLogEntity.setCreateTime(new Date());
                loginLogEntity.setType(type);
                memberLoginLogDao.insert(loginLogEntity);

                return memberEntity;
            }
        }

        return null;
    }

    @Override
    public MemberEntity login(WeiboUser weiboUser) throws Exception {

        //具有登录和注册逻辑
        String uid = weiboUser.getUid();

        //1、判断当前社交用户是否已经登录过系统
        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));

        if (memberEntity != null) {
            //这个用户已经注册过
            //更新用户的访问令牌的时间和access_token
            MemberEntity update = new MemberEntity();
            update.setId(memberEntity.getId());
            update.setUpdateTime(new Date());
            update.setAccessToken(weiboUser.getAccess_token());
            update.setExpiresIn(weiboUser.getExpires_in());
            this.baseMapper.updateById(update);
            memberEntity.setAccessToken(weiboUser.getAccess_token());
            memberEntity.setExpiresIn(weiboUser.getExpires_in());
            return memberEntity;
        } else {
            //2、没有查到当前社交用户对应的记录我们就需要注册一个
            MemberEntity register = new MemberEntity();
            //3、查询当前社交用户的社交账号信息（昵称、性别等）
            Map<String,String> query = new HashMap<>();
            query.put("access_token", weiboUser.getAccess_token());
            query.put("uid", weiboUser.getUid());
            HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<String, String>(), query);

            if (response.getStatusLine().getStatusCode() == 200) {
                //查询成功
                String json = EntityUtils.toString(response.getEntity());
                JSONObject jsonObject = JSON.parseObject(json);
                String name = jsonObject.getString("name");
                String gender = jsonObject.getString("gender");
                String profileImageUrl = jsonObject.getString("profile_image_url");
                MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
                register.setLevelId(levelEntity.getId());
                register.setNickname(name);
                register.setGender("m".equals(gender)?1:0);
                register.setAvatar(profileImageUrl);
                register.setCreateTime(new Date());
                register.setSocialUid(weiboUser.getUid());
                register.setAccessToken(weiboUser.getAccess_token());
                register.setExpiresIn(weiboUser.getExpires_in());

                //把用户信息插入到数据库中
                this.baseMapper.insert(register);

            }
            return register;
        }

    }

    @Override
    public MemberEntity login(WxUser wxUser) throws Exception {
        //从accessTokenInfo中获取出来两个值 access_token 和 oppenid
        //把accessTokenInfo字符串转换成map集合，根据map里面中的key取出相对应的value
        Gson gson = new Gson();
        HashMap accessMap = gson.fromJson(wxUser.getAccessToken(), HashMap.class);
        String accessToken = (String) accessMap.get("access_token");
        String openid = (String) accessMap.get("openid");

        //3、拿到access_token 和 oppenid，再去请求微信提供固定的API，获取到扫码人的信息

        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", openid));

        if (memberEntity == null) {
            System.out.println("新用户注册");
            //访问微信的资源服务器，获取用户信息
            String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                    "?access_token=%s" +
                    "&openid=%s";
            String userInfoUrl = String.format(baseUserInfoUrl, accessToken, openid);
            //发送请求
            String resultUserInfo = null;
            try {
                resultUserInfo = HttpClientUtils.get(userInfoUrl);
                System.out.println("resultUserInfo==========" + resultUserInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //解析json
            HashMap userInfoMap = gson.fromJson(resultUserInfo, HashMap.class);
            String nickName = (String) userInfoMap.get("nickname");      //昵称
            Double sex = (Double) userInfoMap.get("sex");        //性别
            String headimgurl = (String) userInfoMap.get("headimgurl");      //微信头像

            //把扫码人的信息添加到数据库中
            memberEntity = new MemberEntity();
            memberEntity.setNickname(nickName);
            memberEntity.setGender(Integer.valueOf(Double.valueOf(sex).intValue()));
            memberEntity.setAvatar(headimgurl);
            memberEntity.setCreateTime(new Date());
            memberEntity.setSocialUid(openid);
            // register.setExpiresIn(socialUser.getExpires_in());
            this.baseMapper.insert(memberEntity);
        }
        return memberEntity;
    }
    @Override
    public MemberEntity login(GithubUser githubUser) throws Exception {
        String accessToken = githubUser.getAccess_token();
        String apiUrl = "https://api.github.com/user";
        JsonNode userInfo;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(apiUrl);
            request.addHeader("Authorization", "Bearer " + accessToken);
            request.addHeader("Accept", "application/json");

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    return null;
                }
                String responseBody = EntityUtils.toString(response.getEntity());
                ObjectMapper objectMapper = new ObjectMapper();
                userInfo =  objectMapper.readTree(responseBody);
            }
        }catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
        if (userInfo == null) {
            throw new RuntimeException("Failed to fetch user info from GitHub");
        }

        // 解析 GitHub 用户信息
        String githubId = userInfo.get("id").asText();
        String username = userInfo.get("name").asText();
        String nickName = userInfo.get("login").asText();
        String avatarUrl = userInfo.get("avatar_url").asText();
        String email = userInfo.has("email") ? userInfo.get("email").asText() : null;

        // 在数据库中查找用户
        MemberEntity member = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", githubId));
        if (member == null) {
            // 新建用户
            member = new MemberEntity();
            MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
            member.setLevelId(levelEntity.getId());
            member.setSocialUid(githubId);
            member.setUsername(username);
            member.setAvatar(avatarUrl);
            member.setEmail(email);
            member.setStatus(1);
            member.setNickname(nickName);
            member.setAccessToken(accessToken);
            member.setCreateTime(new Date());
            this.baseMapper.insert(member);
        } else {
            // 更新用户信息
            member.setUsername(username);
            member.setAvatar(avatarUrl);
            member.setAccessToken(accessToken);
            member.setUpdateTime(new Date());
            member.setEmail(email);
            this.baseMapper.updateById(member);
        }
        log.info(member.toString());
        return member;
    }

    @Override
    public MemberLevelEntity getNewLevel(Integer growthPoint) {
        Map<Integer, MemberLevelEntity> levelCache = levelCacheService.getLevelCache();
        MemberLevelEntity nowLevel = null;

        for (Map.Entry<Integer, MemberLevelEntity> entry : levelCache.entrySet()) {
            if (growthPoint >= entry.getKey()) {
                if (nowLevel == null || entry.getKey() > nowLevel.getGrowthPoint()) {
                    nowLevel = entry.getValue();
                }
            }
        }
        return nowLevel;
    }
}