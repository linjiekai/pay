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
import com.mppay.service.service.ITradeRefundCheckGaohuitongService;
import com.mppay.service.service.ITradeRefundGaohuitongService;
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
 * 高汇通香港 退款对账
 */
@Service("gaohuitonghkRefundCheckCenterHandler")
@Slf4j
public class GaohuitonghkRefundCheckCenterHandlerImpl extends BaseCheckCenterHandlerImpl implements CheckCenterHandler {

    @Autowired
    private ITradeRefundCheckGaohuitongService tradeRefundCheckGaohuitongService;

    @Autowired
    private ITradeRefundGaohuitongService tradeRefundGaohuitongService;

    @Value("${ght.xfhl.addressOverseaXfyinli}")
    private String addressOverseaXfyinli;
    @Value("${ght.xfhl.version}")
    private String version;
    @Value("${gaohuitonghk.bill.path}")
    private String filePath;
    @Value("${gaohuitong.overseas}")
    private String overseasUrl;

    @Override
    public void getFile(Long batchId) throws Exception {
        log.info("高汇通HK：退款：获取退款对账文件batchId：{}", batchId);

        // 查询对账批次表信息
        CheckControl checkControl = checkControlService.getById(batchId);
        List<RouteConf> routeConfList = routeConfService.list(new QueryWrapper<RouteConf>().eq("route_code", RouteCode.GAOHUITONGHK.getId()));
        String mercId = "";

        String checkDate = DateTimeUtil.date10();
        String fileName = "";
        for (RouteConf routeConf : routeConfList) {
            if (!mercId.equals(routeConf.getBankMercId())) {
                mercId = routeConf.getBankMercId();
                String fileNameTmp = mercId + "_refund_" + checkControl.getAccountDate() + ".txt";
                String content = sendMsg(routeConf, checkControl);
                writeFile(content, fileNameTmp, checkControl);
                fileName += fileNameTmp + ",";
            }
        }
        log.info("高汇通HK：退款：批次号batchId：{}，生成退款对账文件fileName：{}", batchId, fileName);
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
        log.info("高汇通HK：退款：对账文件待入库开始, batchId：{}", batchId);

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
        TradeRefundCheckGaohuitong gaohuitong = null;
        int rowNum = 0;
        try {
            for (String fileName : fileNameArray) {
                file = new File(filePath + "/" + fileName);
                fis = new FileInputStream(file);
                isr = new InputStreamReader(fis, "utf-8");
                br = new BufferedReader(isr);

                // 清算日期（settle_date）,交易日期（trade_date）,交易时间 （trade_time）,商户号（agencyId）,子商户号（child_merchant_no）,业务类型（bank_code）,系统订单号（pay_no）,商户订单号(order_no),交易金额（amount）,商户手续费（merchant_fee）,子商户手续费（fee）,支付状态（PayStatus）
                while ((line = br.readLine()) != null) {
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
                    String outrefundno = data[0];
                    TradeRefundCheckGaohuitong one = tradeRefundCheckGaohuitongService.getOne(new QueryWrapper<TradeRefundCheckGaohuitong>().eq("out_refund_no", outrefundno));
                    if(one!=null){
                        continue;
                    }
                    gaohuitong = new TradeRefundCheckGaohuitong();
                    gaohuitong.setAccountDate(checkControl.getAccountDate());
                    gaohuitong.setRouteCode(checkControl.getRouteCode());
                    gaohuitong.setBatchId(batchId);
                    gaohuitong.setCheckStatus("0");
                    gaohuitong.setCheckDate(DateTimeUtil.date10());
                    gaohuitong.setCheckTime(DateTimeUtil.time8());
                    gaohuitong.setBankMercId(data[3]);
                    gaohuitong.setBankTradeNo(data[1]);
                    gaohuitong.setOutTradeNo(data[2]);
                    gaohuitong.setOutRefundNo(outrefundno);
                    gaohuitong.setPrice(new BigDecimal(data[4]).multiply(new BigDecimal(-1)).setScale(2, BigDecimal.ROUND_HALF_UP));
                    gaohuitong.setBankCode(BankCode.WEIXIN.getId());
                    gaohuitong.setRouteCode(RouteCode.GAOHUITONGHK.getId());

                    tradeRefundCheckGaohuitongService.save(gaohuitong);
                }

                rowNum = 0;
            }
        } catch (Exception e) {
            log.error("高汇通HK：退款：对账文件入库失败batchId：{}", batchId, e);
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
            log.info("高汇通HK：退款：对账文件待入库完成, batchId：{}", batchId);
        }

        checkControl.setCheckDate(checkDate);
        checkControl.setCheckStatus("2");
        // 更新总控表批次信息
        checkControlService.updateById(checkControl);
    }

    @Override
    public void lastStatus(Long batchId) throws Exception {
        log.info("高汇通HK：退款：查询前天对账情况开始, batchId：{}", batchId);
        // 查询对账批次表信息
        CheckControl checkControl = checkControlService.getById(batchId);
        String accountDate = checkControl.getAccountDate();
        // 获取前一天的日期
        Date beforeDay = DateTimeUtil.beforeDay(DateTimeUtil.formatStringToDate(accountDate, "yyyy-MM-dd"), -1);
        // 将日期转换成字符串
        String beforeAccountDate = DateTimeUtil.formatTimestamp2String(beforeDay, "yyyy-MM-dd");
        log.info("高汇通HK：退款：beforeAccountDate：{}, 前一天的日期：{}", accountDate, beforeAccountDate);
        CheckControl lastCheckControl = checkControlService.getOne(new QueryWrapper<CheckControl>()
                .eq("account_date", beforeAccountDate)
                .eq("route_code", checkControl.getRouteCode())
                .eq("trade_code", checkControl.getTradeCode())
        );

        // 如果等于null，前一天没有对账数据，直接返回,该地方后续正式运作的时候需要关掉，若找不到前一天数据不允许对账，目前因为第一天的前面没有数据暂时如此操作
        if (null == lastCheckControl) {
            log.info("高汇通HK：退款：没有前一天的对账批次号，完成 ，batchId：{}", batchId);
            return;
        }

        // 如果前一天的对账状态不等于4，说明前一天的对账未完成。异常返回
        if (!"4".equals(lastCheckControl.getCheckStatus())) {
            log.info("高汇通HK：退款：前一天的对账未完成,前一天的批次号：{},状态：{}", lastCheckControl.getId(),
                    lastCheckControl.getCheckStatus());
            throw new BusiException("22202", ApplicationYmlUtil.get("22202") + "[" + accountDate + "]");
        }
        log.info("高汇通HK：退款：查询前天对账情况完成, batchId：{}", batchId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void checkData(Long batchId) throws Exception {
        log.info("高汇通HK：退款：校验对账数据开始, batchId：{}", batchId);
        // 查询对账批次表信息
        CheckControl checkControl = checkControlService.getById(batchId);

        // 如果等于null，直接返回
        if (null == checkControl) {
            log.info("高汇通HK：退款：批次号 {}的批次信息不存在", batchId);
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
        List<TradeRefundCheckGaohuitong> lists = tradeRefundCheckGaohuitongService.list(new QueryWrapper<TradeRefundCheckGaohuitong>()
                .eq("batch_id", checkControl.getId()).eq("check_status", CheckStatus.WAITCHECK.getId()));

        String checkStatus = null;
        String checkDate = DateTimeUtil.date10();
        String checkTime = DateTimeUtil.time8();
        boolean flag = false;

        TradeRefundGaohuitong tradeRefundGaohuitong = null;
        for (TradeRefundCheckGaohuitong obj : lists) {
            flag = false;
            // 如果外部订单或者金额为null,continue
            if (null == obj.getOutTradeNo() || null == obj.getPrice()) {
                continue;
            }

            // 查询交易流水，对账状态0|5
            tradeRefundGaohuitong = tradeRefundGaohuitongService.getOne(new QueryWrapper<TradeRefundGaohuitong>()
                    .eq("out_trade_no", obj.getOutTradeNo()).in("check_status", CheckStatus.WAITCHECK.getId(), CheckStatus.DOUBT.getId()));

            if (null != tradeRefundGaohuitong) {
                if (tradeRefundGaohuitong.getOrderStatus().equals(OrderStatus.SUCCESS.getId())) {
                    // 比较金额是否相等
                    if (tradeRefundGaohuitong.getPrice().compareTo(obj.getPrice()) == 0) {
                        checkStatus = CheckStatus.SUCCESS.getId();
                        tatolSuccessPrice = tatolSuccessPrice.add(tradeRefundGaohuitong.getPrice());
                        tatolSuccessNum++;
                    } else {
                        //状态：4金额差错
                        checkStatus = CheckStatus.DIFF.getId();
                        errorTatolPrice =  errorTatolPrice.add(tradeRefundGaohuitong.getPrice());
                        errorTatolNum++;
                    }
                } else {
                    // 状态：3对方有，我方无
                    checkStatus = CheckStatus.LONG.getId();
                    errorTatolPrice = errorTatolPrice.add(tradeRefundGaohuitong.getPrice());
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
            tradeRefundCheckGaohuitongService.updateById(obj);

            // 如果对账流水有数据，交易流水表没有数据记录，则不更新交易流水表
            if (!flag) {
                tradeRefundGaohuitong.setCheckDate(checkDate);
                tradeRefundGaohuitong.setCheckStatus(checkStatus);
                // 更新交易对账流水
                tradeRefundGaohuitongService.updateById(tradeRefundGaohuitong);
            }

            filePrice = filePrice.add(obj.getPrice());
        }
        fileNum = lists.size();

        Date beforeDay = DateTimeUtil.beforeDay(DateTimeUtil.formatStringToDate(checkControl.getAccountDate(), "yyyy-MM-dd"), 14);
        // update 5to2
        tradeRefundGaohuitongService.update(tradeRefundGaohuitong, new UpdateWrapper<TradeRefundGaohuitong>()
                .set("check_date", checkDate)
                .set("check_status", CheckStatus.SHORT.getId())
                .eq("check_status", CheckStatus.DOUBT.getId())
                .le("refund_date", DateTimeUtil.formatTimestamp2String(beforeDay, "yyyy-MM-dd"))
        );

        tradeRefundGaohuitong = new TradeRefundGaohuitong();
        tradeRefundGaohuitong.setCheckStatus(CheckStatus.WAITCHECK.getId());
        tradeRefundGaohuitong.setOrderStatus(OrderStatus.SUCCESS.getId());
        tradeRefundGaohuitong.setRefundDate(checkControl.getAccountDate());
        // 统计交易流水状态为0的数据
        Map<String, Object> tradeOrderWeixinMap = tradeRefundGaohuitongService.statPrice(tradeRefundGaohuitong);
        // 统计存疑金额
        dubiousPrice = new BigDecimal(tradeOrderWeixinMap.get("total_price") == null ? "0" : tradeOrderWeixinMap.get("total_price").toString());
        dubiousNum = tradeOrderWeixinMap.get("total_price") == null ? 0 : Integer.parseInt(tradeOrderWeixinMap.get("total_price").toString());

        // 将交易流水表对账状态为0(未对账)的改为5(存疑)  0to5
        tradeRefundGaohuitongService.update(new TradeRefundGaohuitong(), new UpdateWrapper<TradeRefundGaohuitong>()
                .set("check_date", checkDate)
                .set("check_status", CheckStatus.DOUBT.getId())
                .eq("check_status", CheckStatus.WAITCHECK.getId())
                .eq("order_status", OrderStatus.SUCCESS.getId())
                .le("refund_date", checkControl.getAccountDate())
        );

        // 查询对账状态为3、4的差错数据
        List<TradeRefundCheckGaohuitong> tradeCheckWeixinList = tradeRefundCheckGaohuitongService.list(
                new QueryWrapper<TradeRefundCheckGaohuitong>().eq("check_date", checkDate)
                        .in("check_status", "3", "4")
        );

        if (null != tradeCheckWeixinList) {
            CheckError checkError = null;
            // 登记对方有我方无,金额差异数据
            for (TradeRefundCheckGaohuitong obj : tradeCheckWeixinList) {
                tradeRefundGaohuitong = tradeRefundGaohuitongService.getOne(
                        new QueryWrapper<TradeRefundGaohuitong>().eq("out_trade_no", obj.getOutTradeNo())
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
                if (null != tradeRefundGaohuitong) {
                    checkError.setOurPrice(tradeRefundGaohuitong.getPrice());
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
        List<TradeRefundGaohuitong> tradeOrderWeixinList = tradeRefundGaohuitongService.list(
                new QueryWrapper<TradeRefundGaohuitong>().eq("check_date", checkDate)
                        .eq("check_status", CheckStatus.SHORT.getId())
        );

        if (null != tradeOrderWeixinList) {
            CheckError checkError = null;
            // 登记短款差异数据
            for (TradeRefundGaohuitong obj : tradeOrderWeixinList) {
                checkError = new CheckError();
                checkError.setBatchId(batchId);
                checkError.setBankTradeNo(obj.getBankTradeNo());
                checkError.setRouteCode(obj.getRouteCode());
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
        log.info("高汇通HK：退款：校验对账数据完成，batchId：{}", batchId);
    }

    protected String sendMsg(RouteConf routeConf, CheckControl checkControl) throws Exception {

        GHTCheckDTO dto = new GHTCheckDTO();
        dto.setVersion(version);
        dto.setAgencyId(routeConf.getBankMercId());
        dto.setTerminal_no(routeConf.getAppId());
        dto.setSettle_date(checkControl.getAccountDate().replaceAll("-", ""));
        dto.setTrade_code(GaohuitongConstants.GHTHK_DOWNLOAD_SETTLE_FILE);
        dto.setFile_type("REFUND");

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
