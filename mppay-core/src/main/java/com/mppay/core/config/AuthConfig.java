package com.mppay.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 验证
 */
@Component
@ConfigurationProperties(prefix = "auth")
public class AuthConfig {

    private String appKey;// 云信key
    private String appSecret;// 云信screct
    private String createUserUrl;// 创建用户
    private String refreshTokenUrl;// 更新token
    private String updateInfoUrl;// 更新用户信息
    private String teamCreateUrl;// 创建群聊
    private String teamUpdateUrl;// 编辑群聊
    private String teamQueryDetailUrl;// 获取群组详细信息
    private String teamRemoveUrl;// 创建群聊
    private String teamChangeOwnerUrl;// 移交群主
    
    private String teamAddUrl;// 拉人入群
    private String teamKickUrl;// 踢人出群
    private String teamChatHistoryUrl;// 群聊天记录
    private String userChatHistoryUrl;// 单聊记录
    private String teamLeaveUrl;// 退群
    private String msgSendUrl;// 退群

    private Long codeTime;// 验证码有效期,单位：秒
    private Long loginTokenTime;// 登录token有效时间，单位：天
    private Map<String, Object> sign;// 接口验签

    private String accessKeyName;// 阿里短信签名
    private String accessKeyId;// 阿里短信AccessKeyId
    private String accesskeySecret;// 阿里短信AccessKeySecret
    private String smsTemplateBind;// 阿里短信-绑定模板
    private String smsTemplateLogin;// 阿里短信-登录模板
    private String smsTemplateRegister;// 阿里短信-注册模板
    private String smsTemplateResetPwd;// 阿里短信-重置(忘记)密码模板
    
    private String wxAppId;// 微信AppID
    private String wxSecret;// 微信应用密钥AppSecret
    private String wxAccessTokenUrl;// 微信AccessToken接口URL
    private String wxUserinfoUrl;// 微信获取用户个人信息接口URL
    

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getCreateUserUrl() {
        return createUserUrl;
    }

    public void setCreateUserUrl(String createUserUrl) {
        this.createUserUrl = createUserUrl;
    }

    public String getRefreshTokenUrl() {
        return refreshTokenUrl;
    }

    public void setRefreshTokenUrl(String refreshTokenUrl) {
        this.refreshTokenUrl = refreshTokenUrl;
    }

    public String getUpdateInfoUrl() {
        return updateInfoUrl;
    }

    public void setUpdateInfoUrl(String updateInfoUrl) {
        this.updateInfoUrl = updateInfoUrl;
    }

    public Long getCodeTime() {
        return codeTime;
    }

    public void setCodeTime(Long codeTime) {
        this.codeTime = codeTime;
    }

    public Long getLoginTokenTime() {
        return loginTokenTime;
    }

    public void setLoginTokenTime(Long loginTokenTime) {
        this.loginTokenTime = loginTokenTime;
    }

    public Map<String, Object> getSign() {
        return sign;
    }

    public void setSign(Map<String, Object> sign) {
        this.sign = sign;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccesskeySecret() {
        return accesskeySecret;
    }

    public void setAccesskeySecret(String accesskeySecret) {
        this.accesskeySecret = accesskeySecret;
    }

    public String getAccessKeyName() {
        return accessKeyName;
    }

    public void setAccessKeyName(String accessKeyName) {
        this.accessKeyName = accessKeyName;
    }

    public String getSmsTemplateBind() {
        return smsTemplateBind;
    }

    public void setSmsTemplateBind(String smsTemplateBind) {
        this.smsTemplateBind = smsTemplateBind;
    }

    public String getSmsTemplateLogin() {
        return smsTemplateLogin;
    }

    public void setSmsTemplateLogin(String smsTemplateLogin) {
        this.smsTemplateLogin = smsTemplateLogin;
    }

    public String getSmsTemplateRegister() {
        return smsTemplateRegister;
    }

    public void setSmsTemplateRegister(String smsTemplateRegister) {
        this.smsTemplateRegister = smsTemplateRegister;
    }

    public String getSmsTemplateResetPwd() {
        return smsTemplateResetPwd;
    }

    public void setSmsTemplateResetPwd(String smsTemplateResetPwd) {
        this.smsTemplateResetPwd = smsTemplateResetPwd;
    }

    public String getTeamCreateUrl() {
        return teamCreateUrl;
    }

    public void setTeamCreateUrl(String teamCreateUrl) {
        this.teamCreateUrl = teamCreateUrl;
    }
    
	public String getTeamUpdateUrl() {
		return teamUpdateUrl;
	}

	public void setTeamUpdateUrl(String teamUpdateUrl) {
		this.teamUpdateUrl = teamUpdateUrl;
	}

	public String getTeamQueryDetailUrl() {
		return teamQueryDetailUrl;
	}

	public void setTeamQueryDetailUrl(String teamQueryDetailUrl) {
		this.teamQueryDetailUrl = teamQueryDetailUrl;
	}

	public String getTeamRemoveUrl() {
        return teamRemoveUrl;
    }

    public void setTeamRemoveUrl(String teamRemoveUrl) {
        this.teamRemoveUrl = teamRemoveUrl;
    }

    public String getTeamChangeOwnerUrl() {
		return teamChangeOwnerUrl;
	}

	public void setTeamChangeOwnerUrl(String teamChangeOwnerUrl) {
		this.teamChangeOwnerUrl = teamChangeOwnerUrl;
	}

	public String getTeamAddUrl() {
        return teamAddUrl;
    }

    public void setTeamAddUrl(String teamAddUrl) {
        this.teamAddUrl = teamAddUrl;
    }

    public String getTeamKickUrl() {
        return teamKickUrl;
    }

    public void setTeamKickUrl(String teamKickUrl) {
        this.teamKickUrl = teamKickUrl;
    }

	public String getWxAppId() {
		return wxAppId;
	}

	public void setWxAppId(String wxAppId) {
		this.wxAppId = wxAppId;
	}

	public String getWxSecret() {
		return wxSecret;
	}

	public void setWxSecret(String wxSecret) {
		this.wxSecret = wxSecret;
	}

	public String getWxAccessTokenUrl() {
		return wxAccessTokenUrl;
	}

	public void setWxAccessTokenUrl(String wxAccessTokenUrl) {
		this.wxAccessTokenUrl = wxAccessTokenUrl;
	}

	public String getWxUserinfoUrl() {
		return wxUserinfoUrl;
	}

	public void setWxUserinfoUrl(String wxUserinfoUrl) {
		this.wxUserinfoUrl = wxUserinfoUrl;
	}

    public String getTeamChatHistoryUrl() {
        return teamChatHistoryUrl;
    }

    public void setTeamChatHistoryUrl(String teamChatHistoryUrl) {
        this.teamChatHistoryUrl = teamChatHistoryUrl;
    }

    public String getUserChatHistoryUrl() {
        return userChatHistoryUrl;
    }

    public void setUserChatHistoryUrl(String userChatHistoryUrl) {
        this.userChatHistoryUrl = userChatHistoryUrl;
    }

    public String getTeamLeaveUrl() {
        return teamLeaveUrl;
    }

    public void setTeamLeaveUrl(String teamLeaveUrl) {
        this.teamLeaveUrl = teamLeaveUrl;
    }

    public String getMsgSendUrl() {
        return msgSendUrl;
    }

    public void setMsgSendUrl(String msgSendUrl) {
        this.msgSendUrl = msgSendUrl;
    }
}
