package com.mszlu.blog.service;

import com.mszlu.blog.vo.Result;
import com.mszlu.blog.vo.params.ArticleParam;
import com.mszlu.blog.vo.params.PageParams;

public interface ArticleService {
/*
分页查询文章列表
 */
    Result listArticlesPage(PageParams pageParams);
/*
最热文章
 */
    Result hotArticle(int limit);
    /*
    最新文章
     */
    Result newArticles(int limit);
/*
文章归档
 */
    Result listArchives();
    /*
    查询文章详情
     */
    Result findArticleById(Long articleId);
        /*
        文章发布服务
         */
    Result publish(ArticleParam articleParam);
}
