package com.mppay.gateway.handler.impl;

import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;
import com.mppay.gateway.handler.BaseBusiHandler;
import com.mppay.gateway.handler.UnifiedHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 业务链处理服务
 *
 * @author Administrator
 */
@Service("chainHandler")
@Slf4j
public class ChainHandlerImpl implements UnifiedHandler {
    private static final String preBean = "chain.";

    @Override
    public ResponseMsg execute(RequestMsg requestMsg) throws Exception {
        // 根据请求接口类型methodType获取相应的处理链
        String chain = ApplicationYmlUtil.get(preBean + requestMsg.get("methodType"));
        log.info("requestId[{}], methodType[{}], 处理链为[{}]", requestMsg.get("requestId"), requestMsg.get("methodType"), chain);

        String[] beans = chain.split(",");
        BaseBusiHandler handler = (BaseBusiHandler) SpringContextHolder.getBean(beans[0]);
        ResponseMsg responseMsg = new ResponseMsg();
        handler.handle(requestMsg, responseMsg, beans, 0);
        return responseMsg;

    }

}
