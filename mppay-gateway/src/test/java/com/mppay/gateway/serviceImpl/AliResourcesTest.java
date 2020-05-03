package com.mppay.gateway.serviceImpl;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.fastjson.JSON;
import com.mppay.service.service.IAliResourcesService;
import com.mppay.service.vo.ali.AliIdImagesVO;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AliResourcesTest {


    @Autowired
    protected ApplicationContext ctx;
    @Autowired
    protected IAliResourcesService aliResourcesService;

    @Test
    public void idimages() throws Exception {

        AliIdImagesVO idimages = aliResourcesService.idimages("http://47.106.74.98:15102/shop/mobile/storage/interior/ossResouces?iconUrl=http://static-xfyinli.yinli.gdxfhl.com/idCard/xfyinli/z9gagypc61heh6auqe5b.jpeg", true);
        System.out.println(JSON.toJSONString(idimages));
    }

    @Test
    public void realName() throws Exception {
        Map<String, Object> userRealMap = aliResourcesService.realName("黄世安", "441284198902272118");
        System.out.println(JSON.toJSONString(userRealMap));
    }

}
