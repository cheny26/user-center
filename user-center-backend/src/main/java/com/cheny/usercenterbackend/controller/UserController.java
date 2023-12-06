package com.cheny.usercenterbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.cheny.usercenterbackend.common.BaseResponse;
import com.cheny.usercenterbackend.common.ErrorCode;
import com.cheny.usercenterbackend.common.ResultUtils;
import com.cheny.usercenterbackend.exception.BusinessException;
import com.cheny.usercenterbackend.model.User;
import com.cheny.usercenterbackend.model.request.UserLoginRequest;
import com.cheny.usercenterbackend.model.request.UserRegisterRequest;
import com.cheny.usercenterbackend.sercive.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

import static com.cheny.usercenterbackend.constant.UserConstants.ADMIN;
import static com.cheny.usercenterbackend.constant.UserConstants.USER_LOGIN_STATE;


/**
 * @author chen_y
 * @date 2023-10-20 17:13
 */

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    /**
     * 用户注册
     * @param userRegisterRequest 请求参数
     * @return 用户ID
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if(userRegisterRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"");
        }
        String userAccount=userRegisterRequest.getUserAccount();
        String userPassword=userRegisterRequest.getUserPassword();
        String checkPassword=userRegisterRequest.getCheckPassword();
        if(StringUtils.isAllBlank(userAccount,userPassword,checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"注册失败");
        }
        long id= userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils. success(id);
    }

    /**
     * 用户登录
     * @param userLoginRequest 请求参数
     * @param httpServletRequest 请求参数
     * @return 用户脱敏信息
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest){
        if(userLoginRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"");
        }
        String userAccount=userLoginRequest.getUserAccount();
        String userPassword=userLoginRequest.getUserPassword();
        if(StringUtils.isAllBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"登录失败");
        }
        User user= userService.userLogin(userAccount, userPassword, httpServletRequest);
        return ResultUtils.success(user);
    }

    @GetMapping("getUsers")
    public BaseResponse<List<User>> getUsers(String username,HttpServletRequest httpServletRequest){
        if(!isAdmin(httpServletRequest)){
            throw new BusinessException(ErrorCode.NO_AUTH,"无权限");
        }
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        if(StringUtils.isNoneBlank(username)){
            queryWrapper.like("username",username);
        }
        List<User> userList= userService.list(queryWrapper);
         List<User> users=userList.stream().map(user -> userService.safeteUser(user)).collect(Collectors.toList());
         return ResultUtils.success(users);
    }

    @PostMapping("/deleteUser")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id,HttpServletRequest httpServletRequest){
        if(id<=0 || !isAdmin(httpServletRequest)){
            throw new BusinessException(ErrorCode.NO_AUTH,"无权限");
        }
        userService.removeById(id);
        return ResultUtils.success(true);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest httpServletRequest){
        Object userObj = httpServletRequest.getSession().getAttribute(USER_LOGIN_STATE);
        User curUser=(User)userObj;
        if(curUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN,"");
        }
        long userId=curUser.getId();
        User user=userService.getById(userId);
        User safeUser=userService.safeteUser(user);
        return ResultUtils.success(safeUser);
    }

    @PostMapping("/logout")
    public BaseResponse<String> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"");
        }
        userService.userLogout(request);
        return ResultUtils.success("退出成功");
    }


    /**
     * 判断是否为管理员
     * @param httpServletRequest
     * @return
     */
    public boolean isAdmin(HttpServletRequest httpServletRequest){
        //管理员查询
        User user = (User)httpServletRequest.getSession().getAttribute(USER_LOGIN_STATE);
        if(user==null || user.getUserRole()!=ADMIN){
            return false;
        }
        return true;
    }
}
