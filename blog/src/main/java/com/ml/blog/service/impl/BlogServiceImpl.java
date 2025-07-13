package com.ml.blog.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.util.ObjectBuilder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ml.blog.anno.CacheDelete;
import com.ml.blog.anno.CacheAdd;
import com.ml.blog.config.BlogSearchRepository;
import com.ml.blog.dao.BlogDao;
import com.ml.blog.entity.BlogEntity;
import com.ml.blog.fegin.MemberFeignService;
import com.ml.blog.interceptor.LoginUserInterceptor;
import com.ml.blog.service.BlogService;
import com.ml.blog.constant.RedisConstants;
import com.ml.blog.constant.SystemConstants;
import com.ml.blog.vo.BlogSearchVo;
import com.ml.blog.vo.MemberShowVo;
import com.ml.common.exception.BizCodeEnum;
import com.ml.common.utils.PageUtils;
import com.ml.common.utils.Query;
import com.ml.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static co.elastic.clients.elasticsearch._types.SortOrder.Desc;

@Slf4j
@Service("blogService")
public class BlogServiceImpl extends ServiceImpl<BlogDao, BlogEntity> implements BlogService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MemberFeignService memberClient;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private BlogSearchRepository blogSearchRepository;

    @Override
    @CacheAdd(key = "'blog:detail:' + #id", ttl = 300)
    public R queryBlogById(Long id) {
        //查询blog
        BlogEntity blog = this.getOne(new QueryWrapper<BlogEntity>()
                .eq("id", id)
                .eq("status", 0)  // 只查询未删除的博客
                .eq("state", "已发布")); // 只查询已发布的博客
        if (blog == null) {
            return R.error(BizCodeEnum.BLOG_NOT_FOUND.getCode(), BizCodeEnum.BLOG_NOT_FOUND.getMsg());
        }
        //查询用户
        blog.setUserName(String.valueOf(LoginUserInterceptor.loginUser.get().getNickname()));
        blog.setUserAvatar(LoginUserInterceptor.loginUser.get().getAvatar());
        isBlogLiked(blog);
        return R.ok(blog);
    }

    @Override
    public R queryBlogLikesById(Long id) {
        String key = RedisConstants.BLOG_LIKED_KEY + id;

        // 查询 top5 点赞用户 ID
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if (top5 == null || top5.isEmpty()) {
            return R.ok(Collections.emptyList());
        }

        // 转为 Long 类型 ID
        List<Long> memberIds = top5.stream().map(Long::valueOf).collect(Collectors.toList());

        // 远程调用 member 服务获取用户信息
        R r = memberClient.getMemberList(memberIds);

        if (r.getCode() != 0) {
            return R.error();
        }

        // 使用 ObjectMapper 转换返回的数据
        ObjectMapper mapper = new ObjectMapper();
        List<MemberShowVo> memberShowVos = mapper.convertValue(r.getData(), new TypeReference<List<MemberShowVo>>() {});

        // 排序保持原顺序
        Map<Long, MemberShowVo> map = memberShowVos.stream()
                .collect(Collectors.toMap(MemberShowVo::getId, Function.identity()));
        List<MemberShowVo> sortedUsers = memberIds.stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return R.ok(sortedUsers);
    }
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<BlogEntity> page = this.page(
                new Query<BlogEntity>().getPage(params),
                new QueryWrapper<BlogEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    @Transactional
    @CacheDelete(keys = {
            "'blog:detail:' + #blog.getId()",
            "'blog:hot:*'",
            "'blog:search:*'"
    })
    public boolean updateBlog(BlogEntity blog) {
        // 获取当前登录用户
        Long userId = LoginUserInterceptor.loginUser.get().getId();

        // 文章 ID 必须存在
        Long id = blog.getId();
        if (id == null) return false;

        // 查询当前 version 和现有的 BlogEntity
        BlogEntity existingBlog = this.getById(id);
        if (existingBlog == null) return false; // 如果没有找到该文章，返回 false

        // 创建新的 BlogEntity 来进行更新，只更新传入的字段
        BlogEntity blogToUpdate = new BlogEntity();
        blogToUpdate.setId(id);
        blogToUpdate.setVersion(existingBlog.getVersion()); // 保留原来的 version 字段

        // 更新传入的字段
        if (blog.getTitle() != null) blogToUpdate.setTitle(blog.getTitle());
        if (blog.getContent() != null) blogToUpdate.setContent(blog.getContent());
        if (blog.getState() != null) blogToUpdate.setState(blog.getState());
        if (blog.getCategoryId() != null) blogToUpdate.setCategoryId(blog.getCategoryId());
        blog.setUpdateTime(new Date());
        // 执行更新
        boolean updated = this.updateById(blogToUpdate);
        if (!updated) {
            log.warn("文章更新失败，id={}, version={}", id, existingBlog.getVersion());
            return false;
        }
        BlogSearchVo blogSearchVo = new BlogSearchVo();
        blogSearchVo.setId(blogToUpdate.getId());
        blogSearchVo.setTitle(blogToUpdate.getTitle());
        blogSearchVo.setContent(blogToUpdate.getContent());
        // 更新 Elasticsearch 中的信息
        blogSearchRepository.save(blogSearchVo);
        return true;
    }



    @Override
    public R likeBlog(Long id) {
        //获取当前登录用户
        Long userId = LoginUserInterceptor.loginUser.get().getId();
        String key = RedisConstants.BLOG_LIKED_KEY + id;
        Double score = stringRedisTemplate.opsForZSet().score(key, String.valueOf(userId));
        if(score == null){
            //增加点赞数量
            BlogEntity blog = this.getById(id);
            blog.setLiked(blog.getLiked() + 1);
            this.updateById(blog);
            //点赞
            stringRedisTemplate.opsForZSet().add(key, String.valueOf(userId), System.currentTimeMillis());
        }else{
            //取消点赞
            BlogEntity blog = this.getById(id);
            blog.setLiked(blog.getLiked() - 1);
            this.updateById(blog);
            //取消点赞
            stringRedisTemplate.opsForZSet().remove(key, String.valueOf(userId));
        }
        return R.ok();
    }

    @Override
    @CacheAdd(key = "'blog:hot:' + #current", ttl = 5)
    public R queryHotBlog(Integer current) {
        Page<BlogEntity> page = (Page<BlogEntity>) query()
                .eq("status", 0)  // 过滤已删除博客
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<BlogEntity> records = page.getRecords();
        // 查询用户
        records.forEach(blog -> {
            blog.setUserName(String.valueOf(LoginUserInterceptor.loginUser.get().getNickname()));
            blog.setUserAvatar(LoginUserInterceptor.loginUser.get().getAvatar());
            isBlogLiked(blog);
        });
        return R.ok(records);
    }

    @Override
    @Transactional
    @CacheDelete(keys = {
            "'blog:detail:' + #id",
            "'blog:hot:*'",
            "'blog:search:*'"
    })
    public boolean deleteBlog(Long id) {
        // 删除 Elasticsearch 中的博客信息
        blogSearchRepository.deleteById(id);

        // 执行 MyBatis-Plus 逻辑删除
        // 删除 Redis 缓存（通过 id）
        return this.removeById(id);
    }


    @Override
    @CacheAdd(key = "'blog:search:' + #current + ':' + #keywords", ttl = 5)
    public PageUtils searchBlog(Integer current, String[] keywords) {

        if (keywords == null || keywords.length == 0) {
            throw new IllegalArgumentException("Search keywords must not be empty");
        }
        if (current == null || current < 1) {
            current = 1;
        }

        // 1. Create Bool query
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        for (String keyword : keywords) {
            boolQuery.should(b -> b.match(m -> m.field("title").query(keyword)));
            boolQuery.should(b -> b.match(m -> m.field("content").query(keyword)));
        }

        // 2. Pagination parameters
        int pageSize = SystemConstants.MAX_PAGE_SIZE;
        int from = (current - 1) * pageSize;

        // 3. Build query request with sorting by score
        SearchRequest request = new SearchRequest.Builder()
                .index("blog")
                .query(q -> q.bool(boolQuery.build()))
                .from(from)
                .size(pageSize)
                .sort(s -> s.field(f -> f.field("_score").order(SortOrder.Desc))) // Sorting by score in descending order
                .build();

        // 4. Execute query
        SearchResponse<BlogSearchVo> response;
        try {
            response = elasticsearchClient.search(request, BlogSearchVo.class);
        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch search failed", e);
        }

        // 5. Process the query results
        List<BlogSearchVo> blogList = response.hits().hits().stream()
                .map(hit -> hit.source())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        long total = response.hits().total().value();

        // 6. Return paginated result
        return new PageUtils(blogList, (int) total, pageSize, current);
    }

    private void isBlogLiked(BlogEntity blog) {
        Long userId = LoginUserInterceptor.loginUser.get().getId();
        //判断当前用户时候点赞
        String key = RedisConstants.BLOG_LIKED_KEY + blog.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        blog.setIsLike(score!=null);
    }
}


