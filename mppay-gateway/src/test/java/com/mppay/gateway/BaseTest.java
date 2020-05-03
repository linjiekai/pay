package com.mppay.gateway;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.Sign;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.service.entity.Merc;
import com.mppay.service.service.IMercService;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

@Data
public class BaseTest {

    @Autowired
    public WebApplicationContext wac;

    public MockMvc mockMvc;

    @Autowired
    private IMercService mercService;

    private static final String PRE = "req.";
    private static final String OPTIONAL = "^\\[\\w+(-\\w+)*\\]$";
    private static final String MIDBRACKETS = "\\[|\\]";

    @Before
    public void setUp() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

    }

    public String sign(Map<String, Object> map) throws Exception {

        String itfParam = wac.getEnvironment().getProperty(PRE + map.get("methodType"));
        String[] keys = itfParam.split(",");

        // 是否属于非必传参数,配置文件中使用()来区分非必传参数
        boolean isOption = false;
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        int index = 0;
        Object value = null;
        for (String key : keys) {
            value = map.get(key);
            isOption = key.matches(OPTIONAL);
            if (isOption) {
                key = key.replaceAll(MIDBRACKETS, "");
                if (null != map.get(key)) {
                    value = map.get(key).toString();
                    parameterMap.put(key, value);
                }
                keys[index] = key;
            } else if (null != value && !StringUtils.isEmpty(value.toString())) {
                parameterMap.put(key, value.toString());
            } else {
                throw new BusiException("11004", ApplicationYmlUtil.get("11004").replace("$", key));
            }
            index++;
        }

        Merc merc = mercService.getOne(new QueryWrapper<Merc>().eq("merc_id", map.get("mercId")));
        String plain = Sign.getPlain(parameterMap, keys);
        plain += "&key=" + merc.getPrivateKey();
        System.out.println("plain:" + plain);
        String sign = Sign.sign(plain);
        return sign;
    }
}
