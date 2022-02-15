package com.mszlu.blog.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

@Data
public class ArticleVo {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String title;

    private String summary;//简介

    private Integer commentCounts;//文章评论次数

    private Integer viewCounts;//阅读数

    private Integer weight;
    /**
     * 创建时间
     */
    private String createDate;

    private String author;

    private ArticleBodyVo body;

    private CategoryVo category;

    private List<TagVo> tags;




}