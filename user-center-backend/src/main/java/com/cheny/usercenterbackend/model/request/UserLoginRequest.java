package com.cheny.usercenterbackend.model.request;

import java.io.Serializable;

/**
 * 用户注册请求
 *
 * @author chen_y
 */
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID=231645231984568234L;

    private String userAccount;

    private String userPassword;


    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

}
