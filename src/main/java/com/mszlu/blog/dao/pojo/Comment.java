package com.mszlu.blog.dao.pojo;

import lombok.Data;

@Data
public class Comment {
    private String id;//评论ID
    private String content;//评论的内容
    private Long articleId;//文章ID
    private Long authorId;//作者ID
    private Long parentId;//盖楼，对评论进行评论

    private Long createDate;//评论的时间

    private Integer level;//评论第几层

    private Long toUid;//给谁评论
}
