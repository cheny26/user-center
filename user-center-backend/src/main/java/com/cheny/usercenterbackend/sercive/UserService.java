package com.cheny.usercenterbackend.sercive;

import com.cheny.usercenterbackend.model.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author chen
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2023-10-30 22:58:42
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    long userRegister(String userAccount,String userPassword,String checkPassword);

    /**
     *用户登录
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @return 用户脱敏信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest httpServletRequest);

    /**
     * 用户信息脱敏
     * @param user 原始用户
     * @return 脱敏用户信息
     */
    User safeteUser(User user);

    /**
     * 用户注销
     */
    void userLogout(HttpServletRequest httpServletRequest);
}
