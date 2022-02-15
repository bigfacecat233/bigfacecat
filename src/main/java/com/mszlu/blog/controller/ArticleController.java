package com.mszlu.blog.controller;


import com.mszlu.blog.common.aop.LogAnnotation;
import com.mszlu.blog.service.ArticleService;
import com.mszlu.blog.vo.Result;
import com.mszlu.blog.vo.params.ArticleParam;
import com.mszlu.blog.vo.params.PageParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

//json数据进行交互
@RestController
@RequestMapping("articles")
public class ArticleController {
    /**
     * 首页 文章列表
     */
    @Autowired
    private ArticleService articleService;
    //Result是统一结果返回
    /**
     * 首页 文章列表
     * @param pageParams
     * @return
     */
    @PostMapping
    @LogAnnotation(module="文章",operator="获取文章列表")
    public Result articles(@RequestBody PageParams pageParams) {
        //ArticleVo 页面接收的数据


        return  articleService.listArticlesPage(pageParams);
    }
    /**
     * 首页 最热文章
     */
    @PostMapping("hot")
    public Result hotArticle(){
        int limit = 5;
        return articleService.hotArticle(limit);

    }
    /**
     * 首页 最新文章
     * @return
     */
    @PostMapping("new")
    public Result newArticles(){
        int limit = 5;
        return articleService.newArticles(limit);
    }
    /*
    文章归档
     */
    @PostMapping("listArchives")
    public Result listArchives(){
        return articleService.listArchives();
    }

    @PostMapping("view/{id}")
    public Result findArticleById(@PathVariable("id") Long articleId){
        return articleService.findArticleById(articleId);
    }
    @PostMapping("publish")
    public Result publish(@RequestBody ArticleParam articleParam){
        return articleService.publish(articleParam);
    }
}