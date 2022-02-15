package com.mszlu.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mszlu.blog.dao.mapper.SysUserMapper;
import com.mszlu.blog.dao.pojo.SysUser;
import com.mszlu.blog.service.LoginService;
import com.mszlu.blog.service.SysUserService;
import com.mszlu.blog.utils.JWTUtils;
import com.mszlu.blog.vo.ErrorCode;
import com.mszlu.blog.vo.LoginUserVo;
import com.mszlu.blog.vo.Result;
import com.mszlu.blog.vo.UserVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SysUserServiceImpl implements SysUserService {
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Autowired
    private LoginService loginService;

    @Override
    public UserVo findUserVoById(Long id) {
        SysUser sysUser = sysUserMapper.selectById(id);
        if (sysUser ==null) {
            sysUser = new SysUser();
            sysUser.setId(1L);
            sysUser.setAvatar( " /static/img/logo.b3a48c0.png");
            sysUser.setNickname("MEA");

        }
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties (sysUser,userVo) ;
        return userVo;
    }
    @Override
    public SysUser findUserById(Long id) {
        SysUser sysUser = sysUserMapper.selectById(id);
        if (sysUser ==null){
            sysUser=new SysUser();
            sysUser.setNickname("MEA");}
        return sysUser;
    }
    @Override
    public SysUser findUser(String account, String password) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getAccount,account);
        queryWrapper.eq(SysUser::getPassword, password);
        queryWrapper.select(SysUser::getAccount, SysUser::getId,SysUser::getAvatar,SysUser::getNickname);
//        加快查询策略
        queryWrapper.last("limit 1");
        return sysUserMapper.selectOne(queryWrapper);
    }

    @Override
    public Result findUserByToken(String token) {
        /**
       *1. token合法性校验

       * 是否为空，解析是否成功redis是否存在
        *2．如果校验失败返回错误
        *3.如果成功，返回对应的结果 LoginUserVo
         */
        SysUser sysUser = loginService.checkToken(token);
        if ( sysUser == null){
            Result.fail(ErrorCode.TOKEN_ERROR.getCode() ,ErrorCode.TOKEN_ERROR.getMsg());

            }
        LoginUserVo loginUserVo = new LoginUserVo();
        loginUserVo.setId ( sysUser.getId() );
        loginUserVo.setNickname ( sysUser.getNickname() ) ;
        loginUserVo.setAvatar(sysUser.getAvatar());
        loginUserVo.setAccount( sysUser.getAccount() );

        return Result.success(loginUserVo);

    }

    @Override
    public SysUser findUserByAccount(String account) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getAccount,account);
        queryWrapper.last("limit 1");//查出一条数据
        return sysUserMapper.selectOne(queryWrapper);
    }

    @Override
    public void save(SysUser sysUser) {
        //注意 默认生成的id 是分布式id 采用了雪花算法
        this.sysUserMapper.insert(sysUser);
    }
}
