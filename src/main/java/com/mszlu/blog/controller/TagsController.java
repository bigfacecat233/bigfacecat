package com.mszlu.blog.controller;

import com.mszlu.blog.service.TagService;
import com.mszlu.blog.vo.Result;
import com.mszlu.blog.vo.TagVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController//代表返回json数据
@RequestMapping("tags")//路径映射
public class TagsController {

    @Autowired
    private TagService tagService;
     // /tags/hot
    @GetMapping("/hot")
    public Result hot() {
        int limit = 6;//查询最热的6个标签

        return  tagService.hots(limit);
    }
    @GetMapping
    public Result findAll(){
        return tagService.findAll();
    }
    @GetMapping("detail")
    public Result findAllDetail(){
        return tagService.findAllDetail();
    }

    @GetMapping("detail/{id}")
    public Result findDetailById(@PathVariable("id") Long id){
        return tagService.findDetailById(id);
    }

}