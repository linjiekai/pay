package com.mppay.service.service;

import com.aliyun.oss.model.OSSObject;
import com.mppay.service.vo.ali.AliIdImagesVO;

import java.util.Map;

public interface IAliResourcesService {

    /**
     * @Description(描述): 获取临时访问token
     * @auther: Jack Lin
     * @param :[]
     * @return :void
     * @date: 2019/9/25 15:41
     */
     Map<String,String> getStsToken(String url)throws Exception;
     /**
      * @Description(描述): 获取资源
      * @auther: Jack Lin
      * @param :[]
      * @return :void
      * @date: 2019/9/25 15:41
      */
     OSSObject getOssResouces(Map<String,String> map)throws Exception;
    /**
     * @Description(描述):   身份证图片验证
     * @auther: Jack Lin
     * @param :[idCardNo, url]
     * @return :void
     * @date: 2019/9/26 14:07
     */
    AliIdImagesVO idimages(String url, boolean isFront)throws Exception;

    /**
     * @Description(描述): 身份证实名识别
     * @auther: Jack Lin
     * @return :java.util.Map<java.lang.String,java.lang.Object>
     * @date: 2019/10/22 11:30
     */
    Map<String, Object> realName(String name, String idCard)throws Exception;


}
