package com.mszlu.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mszlu.blog.dao.dos.Archives;
import com.mszlu.blog.dao.mapper.ArticleBodyMapper;
import com.mszlu.blog.dao.mapper.ArticleMapper;
import com.mszlu.blog.dao.mapper.ArticleTagMapper;
import com.mszlu.blog.dao.pojo.Article;
import com.mszlu.blog.dao.pojo.ArticleBody;
import com.mszlu.blog.dao.pojo.ArticleTag;
import com.mszlu.blog.dao.pojo.SysUser;
import com.mszlu.blog.service.*;
import com.mszlu.blog.utils.UserThreadLocal;
import com.mszlu.blog.vo.ArticleBodyVo;
import com.mszlu.blog.vo.ArticleVo;
import com.mszlu.blog.vo.Result;
import com.mszlu.blog.vo.TagVo;
import com.mszlu.blog.vo.params.ArticleParam;
import com.mszlu.blog.vo.params.PageParams;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import org.apache.commons.collections.functors.FalsePredicate;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ArticleServiceImpl implements ArticleService {
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private TagService tagsService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private ArticleTagMapper articleTagMapper;
    @Override
    public Result listArticlesPage(PageParams pageParams) {
        /*
        分页查询article数据库表
         */
        Page<Article> page=new Page<>(pageParams.getPage(),pageParams.getPageSize());
        //查询条件
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        if (pageParams.getCategoryId() != null) {
            // and category_id=#{categoryId}
            queryWrapper.eq(Article::getCategoryId, pageParams.getCategoryId());

        }
        List<Long> articleIdList = new ArrayList<>();
        if (pageParams.getTagId() != null) {
            //加入标签条件查询
            // article表中并没有tag字段一篇文章有多个标签
            // article_tag article_id 1 : n tag_id
            LambdaQueryWrapper<ArticleTag> articleTagLambdaQueryWrapper = new LambdaQueryWrapper<>();
            articleTagLambdaQueryWrapper.eq(ArticleTag::getTagId, pageParams.getTagId());
            List<ArticleTag> articleTags = articleTagMapper.selectList(articleTagLambdaQueryWrapper);
            for (ArticleTag articleTag : articleTags) {
                articleIdList.add(articleTag.getArticleId());
            }
                if (articleIdList.size() > 0) {
                    // and id in(1,2,3)
                    queryWrapper.in(Article::getId, articleIdList);
                }

        }
                    //是否置顶进行排序
            queryWrapper.orderByDesc(Article::getWeight);
            queryWrapper.orderByDesc(Article::getCreateDate);

            Page<Article> articlePage = articleMapper.selectPage(page, queryWrapper);
            List<Article> records = articlePage.getRecords();
            //records是数据库数据，能直接返回吗，不能
            List<ArticleVo> articleVoList = copyList(records, true, true);
            return Result.success(articleVoList);


    }

    @Override
    public Result hotArticle(int limit) {
        //对浏览量进行倒序排
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        //select id,title from article order by view_counts desc limit 5
        wrapper.orderByDesc(Article::getViewCounts);
        wrapper.select(Article::getId,Article::getTitle);
        wrapper.last("limit "+limit);
        List<Article> articles = articleMapper.selectList(wrapper);
        return Result.success(copyList(articles,false,false));
    }

    @Override
    public Result newArticles(int limit) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Article::getCreateDate);
        wrapper.select(Article::getId,Article::getTitle);
        wrapper.last("limit "+limit);
        //select id, title from ms_article order by create_date limit 5
        List<Article> articles = articleMapper.selectList(wrapper);
        return Result.success(copyList(articles,false,false));
    }

    @Override
    public Result listArchives() {
        List<Archives> archivesList = articleMapper.listArchives();
        return Result.success(archivesList);
    }
    @Autowired
    private ThreadService threadService;

    @Override
    public Result findArticleById(Long articleId) {
//        1. 根据id查询文章信息
//        2. 根据bodyId和categoryId去做关联查询
        Article article = articleMapper.selectById(articleId);
        ArticleVo articleVo = copy(article, true, true, true, true);
        //        查看完文章，新增阅读数，有没有问题？
//        查看完文章之后，本应直接返回数据，这时候做一个更新操作，更新是加写锁的，会阻塞其他的读操作，性能就会比较低
//        更新增加了此次接口的耗时，如果一旦更新出问题，不能影响查看文章的操作。
//        使用线程池，可以把更新操作扔到线程池中去执行，和主线程就不相关
        threadService.updateArticleViewCount(articleMapper,article);
        return Result.success(articleVo);
    }

    @Override
    @Transactional
    public Result publish(ArticleParam articleParam) {
        SysUser sysUser = UserThreadLocal.get();

        Article article = new Article();
        article.setAuthorId(sysUser.getId());
        article.setCategoryId(articleParam.getCategory().getId());
        article.setCreateDate(System.currentTimeMillis());
        article.setCommentCounts(0);
        article.setSummary(articleParam.getSummary());
        article.setTitle(articleParam.getTitle());
        article.setViewCounts(0);
        article.setWeight(Article.Article_Common);
        article.setBodyId(-1L);
        this.articleMapper.insert(article);

        //tags
        List<TagVo> tags = articleParam.getTags();
        if (tags != null) {
            for (TagVo tag : tags) {
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(article.getId());
                articleTag.setTagId(tag.getId());
                this.articleTagMapper.insert(articleTag);
            }
        }
        ArticleBody articleBody = new ArticleBody();
        articleBody.setContent(articleParam.getBody().getContent());
        articleBody.setContentHtml(articleParam.getBody().getContentHtml());
        articleBody.setArticleId(article.getId());
        articleBodyMapper.insert(articleBody);

        article.setBodyId(articleBody.getId());
        articleMapper.updateById(article);
        ArticleVo articleVo = new ArticleVo();
        articleVo.setId(article.getId());
        return Result.success(articleVo);
    }
    private List<ArticleVo> copyList(List<Article> records,boolean isAuthor,boolean isTag) {
        List<ArticleVo> articleVoList=new ArrayList<>();
        for (Article record : records) {
              articleVoList.add(copy(record,isTag,isAuthor,false, false));
        }
        return articleVoList;
    }
    private List<ArticleVo> copyList(List<Article> records,boolean isAuthor,boolean isTag,boolean isBody,boolean isCategory) {
        List<ArticleVo> articleVoList=new ArrayList<>();
        for (Article record : records) {
            articleVoList.add(copy(record,isTag,isAuthor,isBody,isCategory));
        }
        return articleVoList;
    }
@Autowired
private CategoryService categoryService;


private ArticleVo copy(Article article,boolean isAuthor,boolean isTag,boolean isBody,boolean isCategory) {
    ArticleVo articleVo = new ArticleVo();
    BeanUtils.copyProperties(article, articleVo);
    articleVo.setCreateDate(new DateTime(article.getCreateDate()).toString("yyyy-MM-dd HH:mm"));
    //并不是所有标签都需要作者，标签信息
    if (isAuthor) {
        SysUser sysUser = sysUserService.findUserById(article.getAuthorId());
        articleVo.setAuthor(sysUser.getNickname());
    }

    if (isTag){
        Long articleId=article.getId();
        List<TagVo> tags = tagsService.findTagsByArticleId(articleId);
        articleVo.setTags(tags);
    }
    if (isBody){
        Long bodyId = article.getBodyId();
        articleVo.setBody(findArticleBodyById(bodyId));
    }
    if (isCategory){
        Long categoryId = article.getCategoryId();
        articleVo.setCategory(categoryService.findCategoryById(categoryId));
    }
    return articleVo;
}
    @Autowired
    private ArticleBodyMapper articleBodyMapper;
    private ArticleBodyVo findArticleBodyById(Long bodyId) {
        ArticleBody articleBody = articleBodyMapper.selectById(bodyId ) ;
        ArticleBodyVo articleBodyVo = new ArticleBodyVo();
        articleBodyVo.setContent(articleBody.getContent() ) ;
        return articleBodyVo;

    }
}
