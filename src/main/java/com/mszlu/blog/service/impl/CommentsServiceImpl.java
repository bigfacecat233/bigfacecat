package com.mszlu.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mszlu.blog.dao.mapper.CommentMapper;
import com.mszlu.blog.dao.pojo.Comment;
import com.mszlu.blog.dao.pojo.SysUser;
import com.mszlu.blog.service.CommentsService;
import com.mszlu.blog.service.SysUserService;
import com.mszlu.blog.utils.UserThreadLocal;
import com.mszlu.blog.vo.CommentVo;
import com.mszlu.blog.vo.Result;
import com.mszlu.blog.vo.UserVo;
import com.mszlu.blog.vo.params.CommentParam;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentsServiceImpl implements CommentsService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private SysUserService sysUserService;
    @Override
    public Result commentsByArticleId(Long id) {
        /*
         * 1．根据文章id查询评论列表从 comment表中查询
         * 2.根掂作看的id查询作看的信悬
         * 3.判断如果level =1要去查询它有没有子评论
         * 4.如果有根据评论id进行查询(parent_id)

         */
        LambdaQueryWrapper<Comment> querywrapper = new LambdaQueryWrapper<>();
        querywrapper.eq(Comment::getArticleId,id);
        querywrapper.eq(Comment::getLevel,1);
        List<Comment> comments = commentMapper.selectList(querywrapper);
        List<CommentVo> commentVoList = copyList(comments);

        return Result.success(commentVoList);
    }

    @Override
    public Result comment(CommentParam commentParam) {
        //登陆的时候，就把用户信息存在了ThreadLocal中
        SysUser sysUser = UserThreadLocal.get();
        Comment comment = new Comment();
        comment.setArticleId(commentParam.getArticleId());
        comment.setAuthorId(sysUser.getId());
        comment.setContent(commentParam.getContent());
        comment.setCreateDate(System.currentTimeMillis());
        Long parent = commentParam.getParent();
        if (parent == null || parent == 0) {
            comment.setLevel(1);
        }else{
            comment.setLevel(2);
        }
        comment.setParentId(parent == null ? 0 : parent);
        Long toUserId = commentParam.getToUserId();
        comment.setToUid(toUserId == null ? 0 : toUserId);
        commentMapper.insert(comment);
        return Result.success(null);
    }

    private List<CommentVo> copyList(List<Comment> comments) {
        ArrayList<CommentVo> commentVoArrayList = new ArrayList<>();
        for(Comment comment : comments){
            commentVoArrayList.add(copy(comment));
        }
        return commentVoArrayList;
    }
    private CommentVo copy(Comment comment) {
        CommentVo commentVo = new CommentVo();
        BeanUtils.copyProperties(comment, commentVo);
        //作者信息
        Long authorId = comment.getAuthorId();
        UserVo userVo = sysUserService.findUserVoById(authorId);
        commentVo.setAuthor(userVo);
        //子评论
        Integer level = comment.getLevel();
        if (level == 1) {
            String id = comment.getId();
            List<CommentVo> commentVoList = findCommentsByParentId(id);
            commentVo.setChildrens(commentVoList);
        }
        //to User 给谁评论
        if (level > 1) {//没有评论
            Long toUid = comment.getToUid();
            UserVo toUserVo = this.sysUserService.findUserVoById(toUid);
            commentVo.setToUser(toUserVo);
        }
        return commentVo;
    }

    private List<CommentVo> findCommentsByParentId(String id) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getParentId,id);
        queryWrapper.eq(Comment::getLevel,2);
        return copyList(commentMapper.selectList(queryWrapper));
    }
}
