package com.mppay.gateway.handler.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mppay.core.exception.BusiException;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.gateway.handler.CheckCenterHandler;
import com.mppay.service.entity.CheckControl;
import com.mppay.service.service.ICheckControlService;
import com.mppay.service.service.ICheckErrorService;
import com.mppay.service.service.IRouteConfService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public abstract class BaseCheckCenterHandlerImpl implements CheckCenterHandler {

    @Autowired
    protected ICheckControlService checkControlService;

    @Autowired
    protected IRouteConfService routeConfService;

    @Autowired
    protected ICheckErrorService checkErrorService;

    @Override
    public void check(Long batchId) throws Exception {
        // 查询对账批次表信息
        CheckControl checkControl = checkControlService.getById(batchId);

        log.info("总控表对账开始，batchId：{}", batchId);

        if (null == checkControl) {
            throw new BusiException("22201", ApplicationYmlUtil.get("22201"));
        }

        String checkStatus = checkControl.getCheckStatus();

        switch (checkStatus) {
            // 对账状态为0:获取对账文件
            case "0":
                getFile(batchId);
                // 对账状态为1:文件已获取,进行文件入库
            case "1":
                importData(batchId);
                // 对账状态为2:文件已入库,进行对账
            case "2":
                // 检查前一天的对账是否完成
                lastStatus(batchId);
                // 本批次对账
                checkData(batchId);
            case "4":
                break;
            default:
                break;
        }
        log.info("总控表对账结束，batchId：{}", batchId);
    }


    abstract public void getFile(Long batchId) throws Exception;

    abstract public void importData(Long batchId) throws Exception;

    abstract public void lastStatus(Long batchId) throws Exception;

    abstract public void checkData(Long batchId) throws Exception;

}
