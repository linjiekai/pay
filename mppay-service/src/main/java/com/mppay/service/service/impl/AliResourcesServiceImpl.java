package com.mppay.service.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.mppay.core.utils.*;
import com.mppay.service.service.IAliResourcesService;
import com.mppay.service.vo.ali.AliIdImagesVO;
import jodd.util.CharUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AliResourcesServiceImpl implements IAliResourcesService {

    @Value("${aliyun.storage.accessKeyId}")
    private String accessKeyId;
    @Value("${aliyun.storage.endpoint}")
    private String endpoint;
    @Value("${aliyun.storage.accessKeySecret}")
    private String accessKeySecret;
    @Value("${aliyun.storage.stsDomain}")
    private String stsDomain;
    @Value("${aliyun.storage.roleArn}")
    private String roleArn;
    @Value("${aliyun.storage.roleSessionName}")
    private String roleSessionName;
    @Value("${aliyun.storage.bucketName}")
    private String bucketName;
    @Value("${aliyun.storage.privateBucketName}")
    private String privateBucketName;
    @Value("${aliyun.storage.privateEndpoint}")
    private String privateEndpoint;
    @Value("${aliyun.storage.expires}")
    private Long expires;
    @Value("${aliyun.idimages.appCode}")
    private String appCode;
    @Value("${aliyun.idimages.appSecret}")
    private String appSecret;
    @Value("${aliyun.idimages.appKey}")
    private String appKey;
    @Value("${aliyun.idimages.url}")
    private String idimagesUrl;
    @Value("${alipay.real.name.appcode}")
    private String realNameAppcode;
    @Value("${alipay.real.name.url}")
    private String realNameUrl;

    @Override
    public Map<String,String> getStsToken(String url) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            DefaultProfile.addEndpoint("", "", "Sts", stsDomain);
            // 构造default profile（参数留空，无需添加region ID）
            IClientProfile profile = DefaultProfile.getProfile("", accessKeyId, accessKeySecret);
            // 用profile构造client
            DefaultAcsClient client = new DefaultAcsClient(profile);
            final AssumeRoleRequest request = new AssumeRoleRequest();
            request.setMethod(MethodType.GET);
            request.setRoleArn(roleArn);
            request.setRoleSessionName(roleSessionName);
            request.setDurationSeconds(expires); // 设置凭证有效时间
            final AssumeRoleResponse response = client.getAcsResponse(request);
            log.info("AssumeRoleResponse:{}", JSON.toJSONString(response));
            String securityToken = response.getCredentials().getSecurityToken();
            String accessKeyId1 = response.getCredentials().getAccessKeyId();
            String accessKeySecret1 = response.getCredentials().getAccessKeySecret();
            Map<String,String> map= new HashMap<>();
            map.put("token",securityToken);
            map.put("url",url);
            map.put("privateAccessKeyId",accessKeyId1);
            map.put("privateAccessKeySecret",accessKeySecret1);
            log.info("获取STS资源临时token,耗时:{}ms", System.currentTimeMillis() - startTime);
            return map;
        } catch (ClientException e) {
            log.error("获取STS资源临时token失败：{}, 耗时:{}ms", e, System.currentTimeMillis() - startTime);
            throw e;
        }
    }

    @Override
    public OSSObject getOssResouces(Map<String,String> map) throws Exception {
        long startTime = System.currentTimeMillis();
        OSSClient ossClient = null;
        OSSObject object = null;
        String url = map.get("url");
        try {
            String token = map.get("token");
            if (StringUtils.isNotBlank(token)) {
                String privateAccessKeyId = map.get("privateAccessKeyId");
                String privateAccessKeySecret = map.get("privateAccessKeySecret");
                ossClient = new OSSClient(privateEndpoint, privateAccessKeyId, privateAccessKeySecret, token);
                object = ossClient.getObject(privateBucketName, new URL(url).getPath().replaceFirst("/",""));
            } else {
                ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
                object = ossClient.getObject(bucketName, new URL(url).getPath().replaceFirst("/",""));
            }
            log.info("获取oss图片,耗时:{}ms", System.currentTimeMillis() - startTime);
            return object;
        } catch (Exception e) {
            log.error("获取oss图片失败 url：{}，error：{}, 耗时:{}ms", url, e, System.currentTimeMillis() - startTime);
            throw e;
        }

    }

    @Override
    public AliIdImagesVO idimages(String url, boolean isFront) throws Exception {
        long startTime = System.currentTimeMillis();
        AliIdImagesVO vo = null;
        try {
            Map<String, Object> headers = new HashMap<String, Object>();
            //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
            headers.put("Authorization", "APPCODE " + appCode);
            //根据API的要求，定义相对应的Content-Type
            headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            Map<String, Object> bodys = new HashMap<String, Object>();
            bodys.put("image", url);
            if (isFront) {
                bodys.put("idCardSide", "front");//默认正面，背面请传back
            } else {
                bodys.put("idCardSide", "back");//默认正面，背面请传
            }
            String s = HttpClientUtil.sendPost(idimagesUrl, bodys, headers);
            log.info("ali 身份证识别 结果：{},耗时:{}ms ", s, System.currentTimeMillis() - startTime);
            if (StringUtils.isNotBlank(s)) {
                vo = JSON.parseObject(s, AliIdImagesVO.class);
            }
        } catch (Exception e) {
            log.error("ali 身份证识别失败 ，url：{}，error：{}, 耗时:{}ms", url, e, System.currentTimeMillis() - startTime);
        }
        return vo;
    }

    @Override
    public Map<String, Object> realName(String name, String idCard) throws Exception {
        // 校验身份证号
        Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("Authorization", "APPCODE " + realNameAppcode);
        name = URLEncoder.encode(name, "utf-8");
        String paramStr = "idCard=" + idCard + "&name=" + name;
        log.info("用户实名认证请求url:{},header:{}",realNameUrl+ paramStr, JSONUtil.toJsonStr(headerMap));
        String result = HttpUtil.sendGet(realNameUrl, paramStr, headerMap);
        log.info("用户实名认证响应:{}" ,result);
        Map<String, Object> userRealMap = (Map<String, Object>) JSONObject.parseObject(result, Map.class);
        return userRealMap;
    }
}
