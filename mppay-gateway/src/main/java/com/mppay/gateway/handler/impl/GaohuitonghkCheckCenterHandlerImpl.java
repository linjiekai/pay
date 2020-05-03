package com.mppay.gateway.handler.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mppay.core.constant.*;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.gaohuitong.GaohuitongConstants;
import com.mppay.core.sign.gaohuitong.GaohuitongMessgeUtil;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.gateway.dto.platform.gaohuitong.GHTCheckDTO;
import com.mppay.gateway.handler.CheckCenterHandler;
import com.mppay.service.entity.*;
import com.mppay.service.service.ITradeCheckGaohuitongService;
import com.mppay.service.service.ITradeOrderGaohuitongService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.mppay.core.utils.StringUtil.getRandom;

/**
 * 高汇通香港 交易对账
 */
@Service("gaohuitonghkCheckCenterHandler")
@Slf4j
public class GaohuitonghkCheckCenterHandlerImpl extends BaseCheckCenterHandlerImpl implements CheckCenterHandler {

    @Autowired
    private ITradeCheckGaohuitongService tradeCheckGaohuitongService;

    @Autowired
    private ITradeOrderGaohuitongService tradeOrderGaohuitongService;

    @Value("${ght.xfhl.addressOverseaXfyinli}")
    private String addressOverseaXfyinli;
    @Value("${ght.xfhl.version}")
    private String version;
    @Value("${gaohuitong.overseas}")
    private String overseasUrl;
    @Value("${gaohuitonghk.bill.path}")
    private String filePath;

    @Override
    public void getFile(Long batchId) throws Exception {
        log.info("高汇通HK：获取对账文件batchId：{}", batchId);

        // 查询对账批次表信息
        CheckControl checkControl = checkControlService.getById(batchId);
        List<RouteConf> routeConfList = routeConfService.list(new QueryWrapper<RouteConf>().eq("route_code", RouteCode.GAOHUITONGHK.getId()));
        String mercId = "";

        String checkDate = DateTimeUtil.date10();
        String fileName = "";
        for (RouteConf routeConf : routeConfList) {
            if (!mercId.equals(routeConf.getBankMercId())) {
                mercId = routeConf.getBankMercId();
                String fileNameTmp = mercId + "_trade_" + checkControl.getAccountDate() + ".txt";
                String content = sendMsg(routeConf, checkControl);
                writeFile(content, fileNameTmp, checkControl);
                fileName += fileNameTmp + ",";
            }
        }
        log.info("高汇通HK：批次号batchId：{}，生成对账文件fileName：{}", batchId, fileName);
        checkControl.setCheckDate(checkDate);
        checkControl.setCheckStatus("1");
        checkControl.setFileName(fileName);
        checkControl.setStartTime(DateTimeUtil.formatTimestamp2String(new Date(), "yyyyMMddHHmmss"));

        // 更新批次表批次信息
        checkControlService.updateById(checkControl);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importData(Long batchId) throws Exception {
        log.info("高汇通HK：对账文件待入库, batchId：{}", batchId);

        // 查询对账批次表信息
        CheckControl checkControl = checkControlService.getById(batchId);

        String checkDate = DateTimeUtil.date10();

        String[] fileNameArray = checkControl.getFileName().split("\\,");
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        File file = null;
        String line = null;
        String[] data = null;
        TradeCheckGaohuitong gaohuitong = null;
        int rowNum = 0;
        try {
            for (String fileName : fileNameArray) {
                file = new File(filePath + "/" + fileName);
                fis = new FileInputStream(file);
                isr = new InputStreamReader(fis, "utf-8");
                br = new BufferedReader(isr);

                // 清算日期（settle_date）,交易日期（trade_date）,交易时间 （trade_time）,商户号（agencyId）,子商户号（child_merchant_no）,业务类型（bank_code）,系统订单号（pay_no）,商户订单号(order_no),交易金额（amount）,商户手续费（merchant_fee）,子商户手续费（fee）,支付状态（PayStatus）
                while ((line = br.readLine()) != null) {
                    log.info("高汇通HK：batchId：{}, line：{}", batchId, line);
                    // 从第二行开始
                    if (0 == rowNum) {
                        rowNum++;
                        continue;
                    }
                    line = line.replace("`", "");
                    data = line.split(",");
                    // 如果小于11列,不处理
                    if (data.length < 11) {
                        continue;
                    }

                    gaohuitong = new TradeCheckGaohuitong();
                    gaohuitong.setAccountDate(checkControl.getAccountDate());
                    gaohuitong.setRouteCode(checkControl.getRouteCode());
                    gaohuitong.setBatchId(batchId);
                    gaohuitong.setCheckStatus("0");
                    gaohuitong.setCheckDate(DateTimeUtil.date10());
                    gaohuitong.setCheckTime(DateTimeUtil.time8());
                    gaohuitong.setBankMercId(data[3]);
                    gaohuitong.setBankTradeNo(data[6]);
                    gaohuitong.setOutTradeNo(data[7]);
                    gaohuitong.setOpenId(data[4]);
                    String datum = data[5];
                    if ("PUBLICWECHAT".equalsIgnoreCase(datum)) {
                        gaohuitong.setTradeType(TradeType.JSAPI.getId());
                    } else if ("APPWECHAT".equalsIgnoreCase(datum)) {
                        gaohuitong.setTradeType(TradeType.APP.getId());
                    } else {
                        gaohuitong.setTradeType(datum);
                    }
                    gaohuitong.setPrice(new BigDecimal(data[8]));
                    gaohuitong.setBankCode(BankCode.WEIXIN.getId());
                    gaohuitong.setRouteCode(RouteCode.GAOHUITONGHK.getId());

                    tradeCheckGaohuitongService.save(gaohuitong);
                }

                rowNum = 0;
            }
        } catch (Exception e) {
            log.error("高汇通HK：对账文件入库失败batchId：{}", batchId, e);
            throw new BusiException("22302", ApplicationYmlUtil.get("22302") + ",batchId=[" + batchId + "]");
        } finally {
            if (null != br) {
                br.close();
                br = null;
            }
            if (null != isr) {
                isr.close();
                isr = null;
            }
            if (null != fis) {
                fis.close();
                fis = null;
            }
        }

        checkControl.setCheckDate(checkDate);
        checkControl.setCheckStatus("2");
        // 更新总控表批次信息
        checkControlService.updateById(checkControl);
    }

    @Override
    public void lastStatus(Long batchId) throws Exception {
// 查询对账批次表信息
        CheckControl checkControl = checkControlService.getById(batchId);
        String accountDate = checkControl.getAccountDate();
        // 获取前一天的日期
        Date beforeDay = DateTimeUtil.beforeDay(DateTimeUtil.formatStringToDate(accountDate, "yyyy-MM-dd"), -1);
        // 将日期转换成字符串
        String beforeAccountDate = DateTimeUtil.formatTimestamp2String(beforeDay, "yyyy-MM-dd");
        log.info("高汇通HK：beforeAccountDate：{}, 前一天的日期：{}", accountDate, beforeAccountDate);
        CheckControl lastCheckControl = checkControlService.getOne(new QueryWrapper<CheckControl>()
                .eq("account_date", beforeAccountDate)
                .eq("route_code", checkControl.getRouteCode())
                .eq("trade_code", checkControl.getTradeCode())
        );

        // 如果等于null，前一天没有对账数据，直接返回,该地方后续正式运作的时候需要关掉，若找不到前一天数据不允许对账，目前因为第一天的前面没有数据暂时如此操作
        if (null == lastCheckControl) {
            log.info("高汇通HK：没有前一天的对账批次号");
            return;
        }

        // 如果前一天的对账状态不等于4，说明前一天的对账未完成。异常返回
        if (!"4".equals(lastCheckControl.getCheckStatus())) {
            log.error("高汇通HK：前一天的对账未完成,前一天的批次号：{},状态：{}", lastCheckControl.getId(),
                    lastCheckControl.getCheckStatus());
            throw new BusiException("22202", ApplicationYmlUtil.get("22202") + "[" + accountDate + "]");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void checkData(Long batchId) throws Exception {
        // 查询对账批次表信息
        CheckControl checkControl = checkControlService.getById(batchId);

        // 如果等于null，直接返回
        if (null == checkControl) {
            log.info("批次号 {}的批次信息不存在", batchId);
            return;
        }

        BigDecimal tatolSuccessPrice = new BigDecimal(0); // 平账总金额
        Integer tatolSuccessNum = 0; // 平账总笔数
        BigDecimal longPrice = new BigDecimal(0); // 对方有我方无总金额
        Integer longNum = 0; // 对方有我方无总笔数
        BigDecimal shortPrice = new BigDecimal(0); // 我方有对方无总金额
        Integer shortNum = 0; // 我方有对方无总笔数
        BigDecimal errorPrice = new BigDecimal(0); // 金额差错总金额
        Integer errorNum = 0; // 金额差错总笔数
        BigDecimal errorTatolPrice = new BigDecimal(0); // 对账差异总金额
        Integer errorTatolNum = 0; // 对账差异总笔数
        BigDecimal dubiousPrice = new BigDecimal(0); // 存疑总金额
        Integer dubiousNum = 0; // 存疑总笔数
        BigDecimal filePrice = new BigDecimal(0); // 对账文件总金额
        Integer fileNum = 0; // 对账文件总笔数

        // 查询所有未对账的数据
        List<TradeCheckGaohuitong> lists = tradeCheckGaohuitongService.list(new QueryWrapper<TradeCheckGaohuitong>()
                .eq("batch_id", checkControl.getId()).eq("check_status", CheckStatus.WAITCHECK.getId()));

        String checkStatus = null;
        String checkDate = DateTimeUtil.date10();
        String checkTime = DateTimeUtil.time8();
        boolean flag = false;

        TradeOrderGaohuitong tradeOrderGaohuitong = null;
        for (TradeCheckGaohuitong obj : lists) {
            flag = false;
            // 如果外部订单或者金额为null,continue
            if (null == obj.getOutTradeNo() || null == obj.getPrice()) {
                continue;
            }

            // 查询交易流水，对账状态0|5
            tradeOrderGaohuitong = tradeOrderGaohuitongService.getOne(new QueryWrapper<TradeOrderGaohuitong>()
                    .eq("out_trade_no", obj.getOutTradeNo()).in("check_status", CheckStatus.WAITCHECK.getId(), CheckStatus.DOUBT.getId()));

            if (null != tradeOrderGaohuitong) {
                if (tradeOrderGaohuitong.getOrderStatus().equals(OrderStatus.SUCCESS.getId())) {
                    // 比较金额是否相等
                    if (tradeOrderGaohuitong.getPrice().compareTo(obj.getPrice()) == 0) {
                        checkStatus = CheckStatus.SUCCESS.getId();
                        tatolSuccessPrice = tatolSuccessPrice.add(tradeOrderGaohuitong.getPrice());
                        tatolSuccessNum++;
                    } else {
                        //状态：4金额差错
                        checkStatus = CheckStatus.DIFF.getId();
                        errorTatolPrice = errorTatolPrice.add(tradeOrderGaohuitong.getPrice());
                        errorTatolNum++;
                    }
                } else {
                    // 状态：3对方有，我方无
                    checkStatus = CheckStatus.LONG.getId();
                    errorTatolPrice = errorTatolPrice.add(tradeOrderGaohuitong.getPrice());
                    errorTatolNum++;
                }
            } else {
                // 状态：3对方有，我方无
                checkStatus = CheckStatus.LONG.getId();
                errorTatolPrice = errorTatolPrice.add(obj.getPrice());
                errorTatolNum++;
                flag = true;
            }

            obj.setCheckDate(checkDate);
            obj.setCheckTime(checkTime);
            obj.setCheckStatus(checkStatus);
            // 更新对账流水
            tradeCheckGaohuitongService.updateById(obj);

            // 如果对账流水有数据，交易流水表没有数据记录，则不更新交易流水表
            if (!flag) {
                tradeOrderGaohuitong.setCheckDate(checkDate);
                tradeOrderGaohuitong.setCheckStatus(checkStatus);
                // 更新交易对账流水
                tradeOrderGaohuitongService.updateById(tradeOrderGaohuitong);
            }

            filePrice = filePrice.add(obj.getPrice());
        }
        fileNum = lists.size();

        // update 5to2
        tradeOrderGaohuitongService.update(tradeOrderGaohuitong, new UpdateWrapper<TradeOrderGaohuitong>()
                .set("check_date", checkDate)
                .set("check_status", CheckStatus.SHORT.getId())
                .eq("check_status", CheckStatus.DOUBT.getId())
                .eq("trade_date", checkControl.getAccountDate())
        );

        tradeOrderGaohuitong = new TradeOrderGaohuitong();
        tradeOrderGaohuitong.setCheckStatus(CheckStatus.WAITCHECK.getId());
        tradeOrderGaohuitong.setOrderStatus(OrderStatus.SUCCESS.getId());
        tradeOrderGaohuitong.setTradeDate(checkControl.getAccountDate());
        // 统计交易流水状态为0的数据
        Map<String, Object> tradeOrderWeixinMap = tradeOrderGaohuitongService.statPrice(tradeOrderGaohuitong);
        // 统计存疑金额
        dubiousPrice = new BigDecimal(tradeOrderWeixinMap.get("total_price") == null ? "0" : tradeOrderWeixinMap.get("total_price").toString());
        dubiousNum = tradeOrderWeixinMap.get("total_price") == null ? 0 : Integer.parseInt(tradeOrderWeixinMap.get("total_price").toString());

        // 将交易流水表对账状态为0(未对账)的改为5(存疑)  0to5
        tradeOrderGaohuitongService.update(new TradeOrderGaohuitong(), new UpdateWrapper<TradeOrderGaohuitong>()
                .set("check_date", checkDate)
                .set("check_status", CheckStatus.DOUBT.getId())
                .eq("check_status", CheckStatus.WAITCHECK.getId())
                .eq("order_status", OrderStatus.SUCCESS.getId())
                .eq("trade_date", checkControl.getAccountDate())
        );

        // 查询对账状态为3、4的差错数据
        List<TradeCheckGaohuitong> tradeCheckWeixinList = tradeCheckGaohuitongService.list(
                new QueryWrapper<TradeCheckGaohuitong>().eq("check_date", checkDate)
                        .in("check_status", "3", "4")
        );

        if (null != tradeCheckWeixinList) {
            CheckError checkError = null;
            // 登记对方有我方无,金额差异数据
            for (TradeCheckGaohuitong obj : tradeCheckWeixinList) {
                tradeOrderGaohuitong = tradeOrderGaohuitongService.getOne(
                        new QueryWrapper<TradeOrderGaohuitong>().eq("out_trade_no", obj.getOutTradeNo())
                );

                checkError = new CheckError();
                checkError.setBatchId(batchId);
                checkError.setBankTradeNo(obj.getBankTradeNo());
                checkError.setRouteCode(obj.getRouteCode());
                checkError.setOpenId(obj.getOpenId());
                checkError.setOutTradeNo(obj.getOutTradeNo());
                checkError.setErrorStatus(obj.getCheckStatus());
                checkError.setTradePrice(obj.getPrice());
                checkError.setOppositePrice(obj.getPrice());
                if (null != tradeOrderGaohuitong) {
                    checkError.setOurPrice(tradeOrderGaohuitong.getPrice());
                }
                checkError.setErrorDate(DateTimeUtil.date10());
                checkError.setAccountDate(checkControl.getAccountDate());
                checkError.setTradeCode(checkControl.getTradeCode());
                checkErrorService.save(checkError);

                if (CheckStatus.LONG.getId().equals(obj.getCheckStatus())) {
                    longPrice = longPrice.add(obj.getPrice());
                    longNum++;
                    continue;
                }
                errorPrice = errorPrice.add(obj.getPrice()); // 金额差错总金额
                errorNum++; // 金额差错总笔数
            }
        }

        // 查询对账状态为2的短款数据
        List<TradeOrderGaohuitong> tradeOrderWeixinList = tradeOrderGaohuitongService.list(
                new QueryWrapper<TradeOrderGaohuitong>().eq("check_date", checkDate)
                        .eq("check_status", CheckStatus.SHORT.getId())
        );

        if (null != tradeOrderWeixinList) {
            CheckError checkError = null;
            // 登记短款差异数据
            for (TradeOrderGaohuitong obj : tradeOrderWeixinList) {
                checkError = new CheckError();
                checkError.setBatchId(batchId);
                checkError.setBankTradeNo(obj.getBankTradeNo());
                checkError.setRouteCode(obj.getRouteCode());
                checkError.setOpenId(obj.getOpenId());
                checkError.setOutTradeNo(obj.getOutTradeNo());
                checkError.setErrorStatus(obj.getCheckStatus());
                checkError.setTradePrice(obj.getPrice());
                checkError.setOurPrice(obj.getPrice());
                checkError.setErrorDate(DateTimeUtil.date10());
                checkError.setAccountDate(checkControl.getAccountDate());
                checkError.setTradeCode(checkControl.getTradeCode());
                checkErrorService.save(checkError);

                shortPrice = shortPrice.add(obj.getPrice()); // 短款总金额
                shortNum++; // 短款总笔数
            }
        }

        checkControl.setTatolSuccessPrice(tatolSuccessPrice);
        checkControl.setTatolSuccessNum(tatolSuccessNum);
        checkControl.setLongPrice(longPrice);
        checkControl.setLongNum(longNum);
        checkControl.setShortPrice(shortPrice);
        checkControl.setShortNum(shortNum);
        checkControl.setErrorPrice(errorPrice);
        checkControl.setErrorNum(errorNum);
        checkControl.setErrorTatolPrice(errorTatolPrice);
        checkControl.setErrorTatolNum(errorTatolNum);
        checkControl.setDubiousPrice(dubiousPrice);
        checkControl.setDubiousNum(dubiousNum);
        checkControl.setFilePrice(filePrice);
        checkControl.setFileNum(fileNum);

        checkControl.setEndTime(DateTimeUtil.formatTimestamp2String(new Date(), "yyyyMMddHHmmss"));
        checkControl.setCheckDate(checkDate);
        checkControl.setCheckStatus("4");
        // 更新总控表批次信息
        checkControlService.updateById(checkControl);
    }

    protected String sendMsg(RouteConf routeConf, CheckControl checkControl) throws Exception {

        GHTCheckDTO dto = new GHTCheckDTO();
        dto.setVersion(version);
        dto.setAgencyId(routeConf.getBankMercId());
        dto.setTerminal_no(routeConf.getAppId());
        dto.setSettle_date(checkControl.getAccountDate().replaceAll("-", ""));
        dto.setTrade_code(GaohuitongConstants.GHTHK_DOWNLOAD_SETTLE_FILE);
        dto.setFile_type("TRAN");

        String reqMsg = JSON.toJSONString(dto);
        //AES key
        String keyStr = getRandom(16);
        PublicKey publicKey = GaohuitongMessgeUtil.getPublicKey(routeConf.getPublicKeyPath());
        PrivateKey privateKey = GaohuitongMessgeUtil.getPrivateKey(routeConf.getKeyPath());
        //组装请求参数
        Map<String, Object> msgMap = GaohuitongMessgeUtil.requestHandle(routeConf.getBankMercId(), reqMsg, keyStr, publicKey, privateKey, null, null);
        String s1 = addressOverseaXfyinli + overseasUrl;
        long l = System.currentTimeMillis();
        String s = JSON.toJSONString(msgMap);
        log.info(" call_ght： url：{}，request密文:{}", s1, JSON.toJSONString(msgMap));
        log.info("call_ght： tranCode：{}， json：{}", GaohuitongConstants.GHTHK_DOWNLOAD_SETTLE_FILE, reqMsg);
        String response = HttpUtil.post(s1, s);
        if (StringUtils.isEmpty(response)) {
            throw new BusiException(13110);
        }
        String[] split = response.split(",");
        if ("0099".equalsIgnoreCase(split[3].substring(0, 4).trim())) {
            throw new BusiException(22301);
        }
        return response;
    }

    protected void writeFile(String content, String fileName, CheckControl checkControl) throws Exception {


        File file = new File(filePath);
        FileOutputStream fos = null;
        try {
            // 如果目录不存在，则新建目录
            if (!file.exists()) {
                file.mkdir();
            }

            file = new File(filePath + File.separator + fileName);
            log.info("批次号：{}, 文件路径：{}", checkControl.getId(), file.getPath());
            if (file.exists()) {
                log.info("对账文件已经存在，删除已存在的文件：{}", file.getPath());
                file.delete();
            }

            file.createNewFile();
            fos = new FileOutputStream(file);
            fos.write(content.getBytes("UTF-8"));
        } finally {
            try {
                if (null != fos) {
                    fos.flush();
                    fos.close();
                    fos = null;
                }
            } catch (IOException e) {
                log.error("关闭IO失败", e);
            }
        }
    }
}
