package com.cheny.usercenterbackend.sercive.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cheny.usercenterbackend.common.ErrorCode;
import com.cheny.usercenterbackend.exception.BusinessException;
import com.cheny.usercenterbackend.model.User;
import com.cheny.usercenterbackend.sercive.UserService;
import com.cheny.usercenterbackend.mapper.UserMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cheny.usercenterbackend.constant.UserConstants.USER_LOGIN_STATE;

/**
* @author chen
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2023-10-30 22:58:42
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //账号，密码非空验证
        if(StringUtils.isAllBlank(userAccount,userPassword,checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        //对账号长度，密码长度，账号特殊字符校验
        checkAccountAndPassword(userAccount,userPassword);
        //校验两次密码是否相同
        if(!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次密码不一致");
        }
        //是否已经存在账号
        QueryWrapper<User> query=new QueryWrapper<>();
        query.eq("userAccount",userAccount);
        User one = this.getOne(query);
        if(one!=null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号已存在");
        }
        //密码加密
        String saltAndHash = PasswordEncipher(userPassword);
        //用户插入数据库
        User user=new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(saltAndHash);
        boolean save = this.save(user);
        if(!save){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"注册失败");
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest httpServletRequest) {
        //账号，密码非空验证
        if(StringUtils.isAllBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        //对账号长度，密码长度，账号特殊字符校验
        checkAccountAndPassword(userAccount,userPassword);
        //密码加密
        String saltAndHash=PasswordEncipher(userPassword);
        //查询账号密码是否正确
        QueryWrapper<User> query=new QueryWrapper<>();
        query.eq("userAccount",userAccount);
        query.eq("userPassword",saltAndHash);
        User user = this.getOne(query);
        ///用户不存在或者密码错误
        if(user==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在或者密码错误");
        }
        //用户信息脱敏
        User safeUser=safeteUser(user);
        //记录用户的登录态
        httpServletRequest.getSession().setAttribute(USER_LOGIN_STATE,safeUser);
        return safeUser;
    }

    @Override
    public void userLogout(HttpServletRequest httpServletRequest) {
        httpServletRequest.getSession().removeAttribute(USER_LOGIN_STATE);
    }

    /**
     * 加密密码
     * @param userPassword 原始密码
     * @return 加密后的密码
     */
    public String PasswordEncipher(String userPassword){
        MessageDigest md;
        try {
            md =MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] salt="chen_y".getBytes();
        //添加盐值
        md.update(salt);
        byte[] md5Bytes = md.digest(userPassword.getBytes());
        // 将盐和哈希值转换为Base64编码字符串
        return Base64.getEncoder().encodeToString(salt) + Base64.getEncoder().encodeToString(md5Bytes);
    }


    public User safeteUser(User user){
        if(user==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"");
        }
        User safeUser=new User();
        safeUser.setId(user.getId());
        safeUser.setUsername(user.getUsername());
        safeUser.setUserAccount(user.getUserAccount());
        safeUser.setAvatarUrl(user.getAvatarUrl());
        safeUser.setUserRole(user.getUserRole());
        safeUser.setGender(user.getGender());
        safeUser.setPhone(user.getPhone());
        safeUser.setEmail(user.getEmail());
        safeUser.setUserStatus(user.getUserStatus());
        safeUser.setCreateTime(user.getCreateTime());
        return safeUser;
    }



    /**
     * 对账号长度，密码长度和账号特殊字符校验
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     */
    public void checkAccountAndPassword(String userAccount,String userPassword){
        //账号长度验证
        if(userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号长度过短");
        }
        //密码长度验证
        if(userPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度过短");
        }
        //账号不包含特殊字符
        String pattern = "[\\\\u00A0\\\\s\\\"`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“'。，、？]";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(userAccount);
        if (m.  find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号包含特殊字符");
        }
    }
}




