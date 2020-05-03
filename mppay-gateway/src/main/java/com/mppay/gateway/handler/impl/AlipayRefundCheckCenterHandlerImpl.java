package com.mppay.gateway.handler.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mppay.core.constant.CheckStatus;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.OrderStatus;
import com.mppay.core.constant.RouteCode;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.Sign;
import com.mppay.core.sign.alipay.AlipaySignature;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.core.utils.HttpClientUtil;
import com.mppay.gateway.handler.CheckCenterHandler;
import com.mppay.service.entity.CheckControl;
import com.mppay.service.entity.CheckError;
import com.mppay.service.entity.RouteConf;
import com.mppay.service.entity.TradeRefund;
import com.mppay.service.entity.TradeRefundAlipay;
import com.mppay.service.entity.TradeRefundCheckAlipay;
import com.mppay.service.service.ITradeRefundAlipayService;
import com.mppay.service.service.ITradeRefundCheckAlipayService;
import com.mppay.service.service.ITradeRefundService;

import lombok.extern.slf4j.Slf4j;

@Service("alipayRefundCheckCenterHandler")
@Slf4j
public class AlipayRefundCheckCenterHandlerImpl extends BaseCheckCenterHandlerImpl implements CheckCenterHandler {

	@Autowired
	private ITradeRefundCheckAlipayService tradeRefundCheckAlipayService;
	
	@Autowired
	private ITradeRefundAlipayService tradeRefundAlipayService;
	
	@Autowired
	private ITradeRefundService tradeRefundService;
	
	@Override
	public void getFile(Long batchId) throws Exception {
		log.info("获取对账文件batchId[{}]", batchId);

		// 查询对账批次表信息
		CheckControl checkControl = checkControlService.getById(batchId);

		List<RouteConf> routeConfList = routeConfService.list(new QueryWrapper<RouteConf>().eq("route_code", RouteCode.ALIPAY.getId()));
		
		String appId = "";
		
		String checkDate = DateTimeUtil.date10();
		
		String fileName = "";
		for (RouteConf routeConf : routeConfList) {
			
			if (!appId.equals(routeConf.getAppId())) {
				appId = routeConf.getAppId();
				
				String fileNameTmp = appId + "_refund_" + checkControl.getAccountDate() + ".csv";
				String content = sendMsg(routeConf, checkControl);
				writeFile(content, appId, fileNameTmp, checkControl);
				
				fileName += fileNameTmp +",";
			}
		}

		log.info("批次号batchId[{}]生成对账文件fileName[{}]", batchId, fileName);
		
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
		log.info("对账文件待入库, batchId[{}]", batchId);

		// 查询对账批次表信息
		CheckControl checkControl = checkControlService.getById(batchId);

		String checkDate = DateTimeUtil.date10();
		String filePath = ApplicationYmlUtil.get("alipay.bill.path");

		String[] fileNameArray = checkControl.getFileName().split("\\,");
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		File file = null;
		String line = null;
		String[] data = null;
		TradeRefundCheckAlipay tradeRefundCheckAlipay = null;
		int rowNum = 0;
		String bankMercId = null;
		try {
			for (String fileName : fileNameArray) {
				file = new File(filePath + File.separator + fileName);
				fis = new FileInputStream(file);
				isr = new InputStreamReader(fis, ConstEC.ENCODE_GBK);
				br = new BufferedReader(isr);

				//支付宝交易号,商户订单号,业务类型,商品名称,创建时间,完成时间,门店编号,门店名称,操作员,终端号,对方账户,订单金额（元）,商家实收（元）,支付宝红包（元）,集分宝（元）,支付宝优惠（元）,商家优惠（元）,券核销金额（元）,券名称,商家红包消费金额（元）,卡消费金额（元）,退款批次号/请求号,服务费（元）,分润（元）,备注
				while ((line = br.readLine()) != null) {
					log.info("batchId[{}], line[{}]", batchId, line);
					if (1 == rowNum) {
						bankMercId = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
						bankMercId = bankMercId.substring(0, 16);
					}
					
					// 从第6行开始
					if (5 > rowNum) {
						rowNum++;
						continue;
					}
					
					data = line.split(",");
					// 如果小于15列,不处理
					if (data.length < 15) {
						continue;
					}

					if (!"退款".equals(data[2].trim())) {
						continue;
					}
					
					tradeRefundCheckAlipay = new TradeRefundCheckAlipay();
					tradeRefundCheckAlipay.setAccountDate(checkControl.getAccountDate());
					tradeRefundCheckAlipay.setRouteCode(checkControl.getRouteCode());
					tradeRefundCheckAlipay.setBatchId(batchId);
					tradeRefundCheckAlipay.setCheckStatus("0");
					tradeRefundCheckAlipay.setCheckDate(DateTimeUtil.date10());
					tradeRefundCheckAlipay.setCheckTime(DateTimeUtil.time8());
					tradeRefundCheckAlipay.setBankMercId(bankMercId);
					tradeRefundCheckAlipay.setBankTradeNo(data[0]);
					tradeRefundCheckAlipay.setOutTradeNo(data[1]);
					tradeRefundCheckAlipay.setOutRefundNo(data[21]);
					tradeRefundCheckAlipay.setPrice(new BigDecimal(data[12]).multiply(new BigDecimal(-1)).setScale(2, BigDecimal.ROUND_HALF_UP));
					
					tradeRefundCheckAlipay.setBankReturnStatus(OrderStatus.SUCCESS.getId());

					tradeRefundCheckAlipayService.save(tradeRefundCheckAlipay);
				}

				rowNum = 0;
			}
		} catch (Exception e) {
			log.error("对账文件入库失败batchId[" + batchId +"]", e);
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

		log.info("beforeAccountDate[{}], 前一天的日期[{}]", accountDate, beforeAccountDate);

		CheckControl lastCheckControl = new CheckControl();
		lastCheckControl.setAccountDate(beforeAccountDate);
		lastCheckControl.setRouteCode(checkControl.getRouteCode());
		lastCheckControl.setTradeCode(checkControl.getTradeCode());

		lastCheckControl = checkControlService.getOne(new QueryWrapper<CheckControl>()
				.eq("account_date", beforeAccountDate)
				.eq("route_code", checkControl.getRouteCode())
				.eq("trade_code", checkControl.getTradeCode())
				);

		// 如果等于null，前一天没有对账数据，直接返回,该地方后续正式运作的时候需要关掉，若找不到前一天数据不允许对账，目前因为第一天的前面没有数据暂时如此操作
		if (null == lastCheckControl) {
			log.info("没有前一天的对账批次号");
			return;
		}

		// 如果前一天的对账状态不等于4，说明前一天的对账未完成。异常返回
		if (!"4".equals(lastCheckControl.getCheckStatus())) {
			log.error("前一天的对账未完成,前一天的批次号[{}],状态[{}]", lastCheckControl.getId(),
					lastCheckControl.getCheckStatus());
			throw new BusiException("22202", ApplicationYmlUtil.get("22202") + "[" + accountDate + "]");
		}
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void checkData(Long batchId) throws Exception {
		// 查询对账批次表信息
		CheckControl checkControl = checkControlService.getById(batchId);
				
		// 批次信息不存在，直接返回
		if (null == checkControl) {
			log.error("批次号[{}]的批次信息不存在", batchId);
			return;
		}

		BigDecimal tatolSuccessPrice = new BigDecimal(0); // 平账总金额
		Integer tatolSuccessNum = 0; // 平账总笔数
		BigDecimal longPrice = new BigDecimal(0); // 对方有我方无总金额
		Integer longNum = 0; // 对方有我方无总笔数
		BigDecimal shortPrice = new BigDecimal(0); // 对方无我方有总金额
		Integer shortNum = 0; // 对方无我方有总笔数
		BigDecimal errorPrice = new BigDecimal(0); // 金额差错总金额
		Integer errorNum = 0; // 金额差错总笔数
		BigDecimal errorTatolPrice = new BigDecimal(0); // 对账差异总金额
		Integer errorTatolNum = 0; // 对账差异总笔数
		BigDecimal dubiousPrice = new BigDecimal(0); // 存疑总金额
		Integer dubiousNum = 0; // 存疑总笔数
		BigDecimal filePrice = new BigDecimal(0); // 对账文件总金额
		Integer fileNum = 0; // 对账文件总笔数

		// 查询所有未对账的退款数据
		List<TradeRefundCheckAlipay> lists = tradeRefundCheckAlipayService.list(new QueryWrapper<TradeRefundCheckAlipay>()
				.eq("batch_id", checkControl.getId()).eq("check_status", CheckStatus.WAITCHECK.getId()));
		
		String checkStatus = null;
		String checkDate = DateTimeUtil.date10();
		String checkTime = DateTimeUtil.time8();
		boolean flag = false;

		// 没对账数据，直接返回
//		if (null == lists || lists.size() <= 0) {
//			log.info("批次号[{}]没有查询到本批次的对账数据", batchId);
//			return;
//		}

		TradeRefundAlipay tradeRefundAlipay = null;
		TradeRefund tradeRefund = null;
		String orderStatus = null;
		
		for (TradeRefundCheckAlipay obj : lists) {
			flag = false;
			orderStatus = null;
			// 如果外部订单或者金额为null,continue
			if (null == obj.getOutTradeNo() || null == obj.getPrice() || null == obj.getOutRefundNo()
					|| null == obj.getBankRefundNo()) {
				continue;
			}

			// 查询退款流水，对账状态0
			tradeRefundAlipay = tradeRefundAlipayService.getOne(new QueryWrapper<TradeRefundAlipay>()
					.eq("out_trade_no", obj.getOutTradeNo()).in("check_status", CheckStatus.WAITCHECK.getId(), CheckStatus.DOUBT.getId()));


			if (null != tradeRefundAlipay) {
				// 判断退款对账流水数据状态
				if (OrderStatus.SUCCESS.getId().equals(obj.getBankReturnStatus())) {
					// 比较金额是否相等
					if (tradeRefundAlipay.getApplyPrice().compareTo(obj.getPrice()) == 0) {
						checkStatus = "1";
						tatolSuccessPrice = tatolSuccessPrice.add(tradeRefundAlipay.getApplyPrice());
						tatolSuccessNum++;
						// 如果金额相等，判断充值流水
						if (OrderStatus.REFUND_WAIT.getId().equals(tradeRefundAlipay.getOrderStatus())) {
							orderStatus = "S";
						}
					} else {
						// 金额匹配不相同，金额差错
						checkStatus = "4";
						errorTatolPrice = errorTatolPrice.add(tradeRefundAlipay.getApplyPrice());
						errorTatolNum++;
					}
				} else {
					// 没有数据记录: 对方有，我方无
					checkStatus = "3";
					errorTatolPrice = errorTatolPrice.add(obj.getPrice());
					errorTatolNum++;
				}
			} else {
				flag = true;
				// 没有数据记录: 对方有，我方无
				checkStatus = "3";
				errorTatolPrice = errorTatolPrice.add(obj.getPrice());
				errorTatolNum++;
			}

			obj.setCheckDate(checkDate);
			obj.setCheckTime(checkTime);
			obj.setCheckStatus(checkStatus);
			// 更新对账流水
			tradeRefundCheckAlipayService.updateById(obj);

			// 如果对账流水有数据，充值退款流水表没有数据记录，则不更新充值退款流水表
			if (!flag) {
				tradeRefundAlipay.setCheckDate(checkDate);
				tradeRefundAlipay.setCheckStatus(checkStatus);
				tradeRefundAlipay.setOrderStatus(orderStatus);
				tradeRefundAlipay.setActualPrice(obj.getPrice());
				tradeRefundAlipay.setBankRefundNo(obj.getBankRefundNo());
				// 更新充值退款流水
				tradeRefundAlipayService.updateById(tradeRefundAlipay);

				tradeRefund = tradeRefundService.getOne(new QueryWrapper<TradeRefund>()
						.eq("refund_no", tradeRefundAlipay.getRefundNo()));
				tradeRefund = new TradeRefund();
				tradeRefund.setOrderStatus(orderStatus);
				tradeRefund.setOutRefundNo(obj.getOutRefundNo());
				tradeRefund.setBankRefundNo(obj.getBankRefundNo());
				tradeRefund.setActualPrice(obj.getPrice());
				tradeRefund.setFundBank(obj.getFundBank());
				tradeRefundService.updateById(tradeRefund);
			}

			filePrice = filePrice.add(obj.getPrice());
		}
		fileNum = lists.size();
		
		
		Date beforeDay = DateTimeUtil.beforeDay(DateTimeUtil.formatStringToDate(checkControl.getAccountDate(), "yyyy-MM-dd"), 14);
		// 将15天之前的数据 update 5to2
		tradeRefundAlipayService.update(new TradeRefundAlipay(), new UpdateWrapper<TradeRefundAlipay>()
				.set("check_date", checkDate)
				.set("check_status", CheckStatus.SHORT.getId())
				.eq("check_status", CheckStatus.DOUBT.getId())
				.le("refund_date", DateTimeUtil.formatTimestamp2String(beforeDay, "yyyy-MM-dd"))
				);
		
		tradeRefundAlipay = new TradeRefundAlipay();
		tradeRefundAlipay.setCheckStatus(CheckStatus.WAITCHECK.getId());
		tradeRefundAlipay.setOrderStatus(OrderStatus.SUCCESS.getId());
		tradeRefundAlipay.setRefundDate(checkControl.getAccountDate());
		// 统计交易流水状态为0的数据
		Map<String, Object> tradeRefundAlipayMap = tradeRefundAlipayService.statPrice(tradeRefundAlipay);
		// 统计存疑金额
		dubiousPrice = new BigDecimal(tradeRefundAlipayMap.get("total_price") == null ? "0" : tradeRefundAlipayMap.get("total_price").toString());
		dubiousNum = tradeRefundAlipayMap.get("total_price") == null ? 0 : Integer.parseInt(tradeRefundAlipayMap.get("total_price").toString());
		
		// update 0to5
		tradeRefundAlipayService.update(new TradeRefundAlipay(), new UpdateWrapper<TradeRefundAlipay>()
				.set("check_date", checkDate)
				.set("check_status", CheckStatus.DOUBT.getId())
				.eq("check_status", CheckStatus.WAITCHECK.getId())
				.le("refund_date", checkControl.getAccountDate())
				);
		
		// 查询对账状态为3、4的差错数据
		List<TradeRefundCheckAlipay> tradeRefundCheckAlipayList = tradeRefundCheckAlipayService.list(
				new QueryWrapper<TradeRefundCheckAlipay>().eq("check_date", checkDate)
				.in("check_status", "3", "4")
				);
				
		if (null != tradeRefundCheckAlipayList) {
			CheckError checkError = null;
			// 登记对方有我方无，金额差异数据
			for (TradeRefundCheckAlipay obj : tradeRefundCheckAlipayList) {
				tradeRefundAlipay = new TradeRefundAlipay();
				tradeRefundAlipay.setOutRefundNo(obj.getOutRefundNo());
				tradeRefundAlipay.setRouteCode(obj.getRouteCode());
				tradeRefundAlipay.setBankTradeNo(obj.getBankTradeNo());

				tradeRefundAlipay = tradeRefundAlipayService.getOne(
						new QueryWrapper<TradeRefundAlipay>().eq("out_refund_no", obj.getOutRefundNo())
						);
				checkError = new CheckError();
				checkError.setBatchId(batchId);
				checkError.setBankTradeNo(obj.getBankTradeNo());
				checkError.setBankCode(obj.getBankCode());
				checkError.setRouteCode(obj.getRouteCode());
				checkError.setOutTradeNo(obj.getOutTradeNo());
				checkError.setErrorStatus(obj.getCheckStatus());
				checkError.setTradePrice(obj.getPrice());
				checkError.setOppositePrice(obj.getPrice());
				if (null != tradeRefundAlipay) {
					checkError.setOurPrice(tradeRefundAlipay.getPrice());
				}
				checkError.setErrorDate(DateTimeUtil.date10());
				checkError.setAccountDate(checkControl.getAccountDate());
				checkError.setTradeCode(checkControl.getTradeCode());
				checkErrorService.save(checkError);

				if ("3".equals(obj.getCheckStatus())) {
					longPrice = longPrice.add(obj.getPrice());
					longNum++;
					continue;
				}

				errorPrice = errorPrice.add(obj.getPrice()); // 金额差错总金额
				errorNum++; // 金额差错总笔数
			}
		}

		// 查询对账状态为2的短款数据
		List<TradeRefundAlipay> tradeRefundAlipayList = tradeRefundAlipayService.list(
				new QueryWrapper<TradeRefundAlipay>().eq("check_date", checkDate)
				.eq("check_status", CheckStatus.SHORT.getId())
				);
		
		if (null != tradeRefundAlipayList) {
			CheckError checkError = null;
			// 登记短款差异数据
			for (TradeRefundAlipay obj : tradeRefundAlipayList) {
				checkError = new CheckError();
				checkError.setBatchId(batchId);
				checkError.setBankTradeNo(obj.getBankTradeNo());
				checkError.setBankCode(obj.getBankCode());
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
	}

	protected String sendMsg(RouteConf routeConf, CheckControl checkControl) throws Exception {
		
		//组装银行统一下单请求报文
		Map<String, Object> contentMap = new HashMap<String, Object>();
		contentMap.put("bill_type", "trade");
		contentMap.put("bill_date", checkControl.getAccountDate());

		String content = JSONObject.toJSONString(contentMap);
		
		//组装银行统一下单请求报文
		Map<String, Object> bankMap = new HashMap<String, Object>();
		
		bankMap.put("app_id", routeConf.getAppId());
		bankMap.put("method", "alipay.data.dataservice.bill.downloadurl.query");
		bankMap.put("charset", ConstEC.ENCODE_UTF8);
		bankMap.put("sign_type", "RSA2");
		bankMap.put("format", "JSON");
		bankMap.put("timestamp", DateTimeUtil.formatTimestamp2String(new Date(), "yyy-MM-dd HH:mm:ss"));
		bankMap.put("version", "1.0");
		bankMap.put("biz_content", content);
		
		String plain = Sign.getPlain(bankMap, true);
		String sign = AlipaySignature.rsa256Sign(plain, routeConf.getPrivateKey(), ConstEC.ENCODE_UTF8);
		
		bankMap.put("sign", sign);
		
		String paramStr = Sign.getPlainURLEncoder(bankMap, ConstEC.ENCODE_UTF8);
		
		content = "biz_content=" + URLEncoder.encode(content, ConstEC.ENCODE_UTF8);
		
		log.info("请求支付宝查询订单接口报文[{}]", bankMap);
		
		String responseStr = HttpClientUtil.httpsRequest(ApplicationYmlUtil.get("alipay.gatewayurl") + "?" + paramStr, "POST",	content);
		
		log.info("响应支付宝查询订单接口报文[{}]", responseStr);
		Map<String, Object> resultMap = null;
		try {
			resultMap = verify(responseStr, "alipay_data_dataservice_bill_downloadurl_query_response", routeConf);
		} catch(BusiException e) {
			throw new BusiException(e.getCode(), e.getMsg());
		}
		
		//如果账单不存在返回null
		if (null != resultMap.get("code") && "40004".equals(resultMap.get("code").toString())) {
			if (null != resultMap.get("sub_code") && "isp.bill_not_exist".equals(resultMap.get("sub_code").toString())) {
				return null;
			}
		}
		
		String billDownloadUrl = (String) resultMap.get("bill_download_url");
		if (StringUtils.isBlank(billDownloadUrl)) {
			throw new BusiException(22301);
		}
		
		return billDownloadUrl;
	}

	protected void writeFile(String downloadUrl, String appId, String fileName, CheckControl checkControl) throws Exception {
		String filePath = ApplicationYmlUtil.get("alipay.bill.path");

		File file = new File(filePath);

		FileOutputStream fos = null;

		try {
			byte[] data = null;
			
			//如果downloadUrl为空,则新建个空文件
			if (StringUtils.isBlank(downloadUrl)) {
				file = new File(filePath + File.separator + fileName);
				if (file.exists()) {
					log.info("对账文件已经存在，删除已存在的文件[{}]", file.getPath());
					file.delete();
				}
				file.createNewFile();
				return;
			} else {
				data = HttpClientUtil.httpRequestRetByteArray(downloadUrl, "GET", null);
			}
			
			// 如果目录不存在，则新建目录
			if (!file.exists()) {
				file.mkdir();
			}

			file = new File(filePath + File.separator + "refund_" + checkControl.getAccountDate() + ".zip");
			log.info("批次号[{}], 文件路径[{}]", checkControl.getId(), file.getPath());
			if (file.exists()) {
				log.info("对账文件已经存在，删除已存在的文件[{}]", file.getPath());
				file.delete();
			}

			file.createNewFile();

			fos = new FileOutputStream(file);

			fos.write(data);
			fos.flush();
			fos.close();
			
			unZip(file.getPath(), filePath + File.separator + fileName);
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
	
	private void unZip(String zipFilePath, String filePath) throws Exception {

		int size = 1024;
		
		ZipFile zipFile = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        File file;
        ZipEntry entry;
        byte[] buff = new byte[size];
        try {
        	zipFile = new ZipFile(zipFilePath, ConstEC.ENCODE_GBK);
        	
        	Enumeration<?> emu = zipFile.getEntries();
        	
        	while (emu.hasMoreElements()) {
            	try {
            		entry = (ZipEntry) emu.nextElement();
                    if (entry.isDirectory()) {
                        continue;
                    }
                    
                    if (entry.getName().indexOf("业务明细.csv") < 0 ) {
                    	continue;
                    }
                    
                    bis = new BufferedInputStream(zipFile.getInputStream(entry));
                    file = new File(filePath);
                    fos = new FileOutputStream(file);
                    bos = new BufferedOutputStream(fos, size);
                    int n = 0;
                    while ((n = bis.read(buff, 0, size)) != -1) {
                        fos.write(buff, 0, n);
                    }
            	} catch (Exception e) {
            		e.printStackTrace();
            	} finally {
            		if (null != bos) {
            			bos.flush();
                        bos.close();
            		}
            		if (null != fos) {
            			fos.close();
            		}
            		if (null != bis) {
            			bis.close();
            		}
    			}
            }
        } finally {
    		if (null != zipFile) {
    			zipFile.close();
    		}
		}
        
    }
	
	private Map<String, Object> verify(String jsonStr, String nodeName, RouteConf routeConf) throws Exception {
		Map<String, Object> resultMap = (Map<String, Object>) JSONObject.parseObject(jsonStr, Map.class);
		
		Map<String, Object> bodyMap = (Map<String, Object>) resultMap.get(nodeName);
		String sign = resultMap.get("sign").toString();
		
		int signDataStartIndex = jsonStr.indexOf(nodeName) + nodeName.length() + 2;
		
		String content = AlipaySignature.extractSignContent(jsonStr, signDataStartIndex);
		
		boolean verify = AlipaySignature.rsa256CheckContent(content, sign, routeConf.getPublicKey(), "UTF-8");
		
		if (!verify) {
			log.error("支付宝返回报文验签失败,待签名串[{}],支付宝返回签名串[{}]", content,
					sign);
			throw new BusiException("11012", ApplicationYmlUtil.get("11012"));
		}
		
		
		if (!ConstEC.SUCCESS_10000.equals(bodyMap.get("code"))) {
			log.error("支付宝请求处理失败,错误信息[{}]", bodyMap);
			if (!"40004".equals(bodyMap.get("code")) && !"isp.bill_not_exist".equals(bodyMap.get("sub_code"))) {
				throw new BusiException(resultMap.get("code").toString(), resultMap.get("msg").toString());
			}
		}
		
		return bodyMap;
		
	}

}
