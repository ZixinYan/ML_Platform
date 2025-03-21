package com.ml.member.vo;

import lombok.Data;
/**
 * @author yaoxinjia
 * @email 894548575@qq.com
 */
@Data
public class SocialUser {

    private String access_token;

    private String remind_in;

    private long expires_in;

    private String uid;

    private String isRealName;

}
