package com.mszlu.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mszlu.blog.dao.mapper.ArticleMapper;
import com.mszlu.blog.dao.pojo.Article;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ThreadService {

    //此操作在线程池中执行，不会影响原有的主线程
    @Async("taskExecutor")
    public void updateArticleViewCount(ArticleMapper articleMapper, Article article) {
        Integer viewCounts = article.getViewCounts();
        Article articleUpdate = new Article();
        articleUpdate.setViewCounts(viewCounts + 1);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getId,article.getId());
//        多线程环境下保证线程安全，类似于乐观锁，检查是否相等，相等了才进行+1操作
        wrapper.eq(Article::getViewCounts, viewCounts);
//        update article set view_count=100 where view_count=99 and id=11
        articleMapper.update(articleUpdate,wrapper);

//        try {
//            Thread.sleep(5000);
//            System.out.println("更新完成");
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }
}