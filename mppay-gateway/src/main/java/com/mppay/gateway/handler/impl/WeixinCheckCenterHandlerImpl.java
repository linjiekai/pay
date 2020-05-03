package com.mppay.gateway.handler.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mppay.core.config.MapEntryConverter;
import com.mppay.core.constant.RouteCode;
import com.mppay.core.constant.CheckStatus;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.OrderStatus;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.Sign;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.core.utils.CharacterUtil;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.core.utils.HttpClientUtil;
import com.mppay.gateway.handler.CheckCenterHandler;
import com.mppay.service.entity.CheckControl;
import com.mppay.service.entity.CheckError;
import com.mppay.service.entity.RouteConf;
import com.mppay.service.entity.TradeCheckWeixin;
import com.mppay.service.entity.TradeOrderWeixin;
import com.mppay.service.service.ITradeCheckWeixinService;
import com.mppay.service.service.ITradeOrderWeixinService;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;

import lombok.extern.slf4j.Slf4j;

/**
 * 微信交易对账
 *
 */
@Service("weixinCheckCenterHandler")
@Slf4j
public class WeixinCheckCenterHandlerImpl extends BaseCheckCenterHandlerImpl implements CheckCenterHandler {

	@Autowired
	private ITradeCheckWeixinService tradeCheckWeixinService;
	
	@Autowired
	private ITradeOrderWeixinService tradeOrderWeixinService;
	@Value("${weixin.bill.path}")
	private String filePath;
	@Value("${weixin.downloadbill}")
	private String downloadbill;

	@Override
	public void getFile(Long batchId) throws Exception {
		log.info("获取对账文件batchId[{}]", batchId);

		// 查询对账批次表信息
		CheckControl checkControl = checkControlService.getById(batchId);

		List<RouteConf> routeConfList = routeConfService.list(new QueryWrapper<RouteConf>().eq("route_code", RouteCode.WEIXIN.getId()));
		
		String appId = null;
		String mercId = "";
		String key = null;
		
		String checkDate = DateTimeUtil.date10();
		
		String fileName = "";
		for (RouteConf routeConf : routeConfList) {
			
			if (!mercId.equals(routeConf.getBankMercId())) {
				appId = routeConf.getAppId();
				mercId = routeConf.getBankMercId();
				key = routeConf.getPrivateKey();
				
				String fileNameTmp = mercId + "_trade_" + checkControl.getAccountDate() + ".txt";
				String content = sendMsg(appId, mercId, key, checkControl);
				writeFile(content, mercId, fileNameTmp, checkControl);
				
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

		String[] fileNameArray = checkControl.getFileName().split("\\,");
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		File file = null;
		String line = null;
		String[] data = null;
		TradeCheckWeixin tradeCheckWeixin = null;
		int rowNum = 0;
		try {
			for (String fileName : fileNameArray) {
				file = new File(filePath + "/" + fileName);
				fis = new FileInputStream(file);
				isr = new InputStreamReader(fis, "utf-8");
				br = new BufferedReader(isr);

				// 交易时间,公众账号ID,商户号,子商户号,设备号,微信订单号,商户订单号,用户标识,交易类型,交易状态,付款银行,货币种类,总金额, 代金券或立减优惠金额,商品名称,商户数据包,手续费,费率 
				while ((line = br.readLine()) != null) {
					log.info("batchId[{}], line[{}]", batchId, line);
					// 从第二行开始
					if (0 == rowNum) {
						rowNum++;
						continue;
					}
					line = line.replace("`", "");
					data = line.split(",");
					// 如果小于15列,不处理
					if (data.length < 15) {
						continue;
					}

					tradeCheckWeixin = new TradeCheckWeixin();
					tradeCheckWeixin.setAccountDate(checkControl.getAccountDate());
					tradeCheckWeixin.setRouteCode(checkControl.getRouteCode());
					tradeCheckWeixin.setBatchId(batchId);
					tradeCheckWeixin.setCheckStatus("0");
					tradeCheckWeixin.setCheckDate(DateTimeUtil.date10());
					tradeCheckWeixin.setCheckTime(DateTimeUtil.time8());
					tradeCheckWeixin.setBankMercId(data[2]);
					tradeCheckWeixin.setBankTradeNo(data[5]);
					tradeCheckWeixin.setOutTradeNo(data[6]);
					tradeCheckWeixin.setOpenId(data[7]);
					tradeCheckWeixin.setTradeType(data[8]);
					tradeCheckWeixin.setPrice(new BigDecimal(data[12]));

					tradeCheckWeixinService.save(tradeCheckWeixin);
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

		CheckControl lastCheckControl = checkControlService.getOne(new QueryWrapper<CheckControl>()
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

		// 如果等于null，直接返回
		if (null == checkControl) {
			log.error("批次号[{}]的批次信息不存在", batchId);
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
		List<TradeCheckWeixin> lists = tradeCheckWeixinService.list(new QueryWrapper<TradeCheckWeixin>()
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

		TradeOrderWeixin tradeOrderWeixin = null;
		for (TradeCheckWeixin obj : lists) {
			flag = false;
			// 如果外部订单或者金额为null,continue
			if (null == obj.getOutTradeNo() || null == obj.getPrice()) {
				continue;
			}

			// 查询交易流水，对账状态0|5
			tradeOrderWeixin = tradeOrderWeixinService.getOne(new QueryWrapper<TradeOrderWeixin>()
				.eq("out_trade_no", obj.getOutTradeNo()).in("check_status", CheckStatus.WAITCHECK.getId(), CheckStatus.DOUBT.getId()));

			if (null != tradeOrderWeixin) {
				if (tradeOrderWeixin.getOrderStatus().equals(OrderStatus.SUCCESS.getId())) {
					// 比较金额是否相等
					if (tradeOrderWeixin.getPrice().compareTo(obj.getPrice()) == 0) {
						checkStatus = CheckStatus.SUCCESS.getId();
						tatolSuccessPrice = tatolSuccessPrice.add(tradeOrderWeixin.getPrice());
						tatolSuccessNum++;
					} else {
						//状态：4金额差错
						checkStatus = CheckStatus.DIFF.getId();
						errorTatolPrice =  errorTatolPrice.add(tradeOrderWeixin.getPrice());
						errorTatolNum++;
					}
				} else {
					// 状态：3对方有，我方无
					checkStatus = CheckStatus.LONG.getId();
					errorTatolPrice = errorTatolPrice.add(tradeOrderWeixin.getPrice());
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
			tradeCheckWeixinService.updateById(obj);

			// 如果对账流水有数据，交易流水表没有数据记录，则不更新交易流水表
			if (!flag) {
				tradeOrderWeixin.setCheckDate(checkDate);
				tradeOrderWeixin.setCheckStatus(checkStatus);
				// 更新交易对账流水
				tradeOrderWeixinService.updateById(tradeOrderWeixin);
			}

			filePrice = filePrice.add(obj.getPrice());
		}
		fileNum = lists.size();
		
		// update 5to2
		tradeOrderWeixinService.update(tradeOrderWeixin, new UpdateWrapper<TradeOrderWeixin>()
				.set("check_date", checkDate)
				.set("check_status", CheckStatus.SHORT.getId())
				.eq("check_status", CheckStatus.DOUBT.getId())
				.eq("trade_date", checkControl.getAccountDate())
				);

		tradeOrderWeixin = new TradeOrderWeixin();
		tradeOrderWeixin.setCheckStatus(CheckStatus.WAITCHECK.getId());
		tradeOrderWeixin.setOrderStatus(OrderStatus.SUCCESS.getId());
		tradeOrderWeixin.setTradeDate(checkControl.getAccountDate());
		// 统计交易流水状态为0的数据
		Map<String, Object> tradeOrderWeixinMap = tradeOrderWeixinService.statPrice(tradeOrderWeixin);
		// 统计存疑金额
		dubiousPrice = new BigDecimal(tradeOrderWeixinMap.get("total_price") == null ? "0" : tradeOrderWeixinMap.get("total_price").toString());
		dubiousNum = tradeOrderWeixinMap.get("total_price") == null ? 0 : Integer.parseInt(tradeOrderWeixinMap.get("total_price").toString());

		// 将交易流水表对账状态为0(未对账)的改为5(存疑)  0to5
		tradeOrderWeixinService.update(new TradeOrderWeixin(), new UpdateWrapper<TradeOrderWeixin>()
				.set("check_date", checkDate)
				.set("check_status", CheckStatus.DOUBT.getId())
				.eq("check_status", CheckStatus.WAITCHECK.getId())
				.eq("order_status", OrderStatus.SUCCESS.getId())
				.eq("trade_date", checkControl.getAccountDate())
				);
		
		// 查询对账状态为3、4的差错数据
		List<TradeCheckWeixin> tradeCheckWeixinList = tradeCheckWeixinService.list(
				new QueryWrapper<TradeCheckWeixin>().eq("check_date", checkDate)
				.in("check_status", "3", "4")
				);

		if (null != tradeCheckWeixinList) {
			CheckError checkError = null;
			// 登记对方有我方无,金额差异数据
			for (TradeCheckWeixin obj : tradeCheckWeixinList) {

				tradeOrderWeixin = tradeOrderWeixinService.getOne(
						new QueryWrapper<TradeOrderWeixin>().eq("out_trade_no", obj.getOutTradeNo())
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
				if (null != tradeOrderWeixin) {
					checkError.setOurPrice(tradeOrderWeixin.getPrice());
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
		List<TradeOrderWeixin> tradeOrderWeixinList = tradeOrderWeixinService.list(
				new QueryWrapper<TradeOrderWeixin>().eq("check_date", checkDate)
				.eq("check_status", CheckStatus.SHORT.getId())
				);

		if (null != tradeOrderWeixinList) {
			CheckError checkError = null;
			// 登记短款差异数据
			for (TradeOrderWeixin obj : tradeOrderWeixinList) {
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

	protected String sendMsg(String appid, String mercId, String key, CheckControl checkControl) throws Exception {
		
		//组装银行统一下单请求报文
		Map<String, Object> bankMap = new HashMap<String, Object>();
		bankMap.put("appid", appid);
		bankMap.put("mch_id", mercId);
		bankMap.put("nonce_str", CharacterUtil.getRandomString(32));
		bankMap.put("bill_date", checkControl.getAccountDate().replaceAll("-", ""));
		bankMap.put("bill_type", ConstEC.SUCCESS);
		
		String plain = Sign.getPlain(bankMap);
		plain += "&key=" + key;
		String sign = Sign.signToHex(plain);
		
		bankMap.put("sign", sign);
		
		// 组装报文
		XStream xStream = new XStream(new DomDriver("UTF-8", new XmlFriendlyNameCoder("-_", "_")));
		xStream.alias("xml", Map.class);
		xStream.registerConverter(new MapEntryConverter());
		String requestXml = xStream.toXML(bankMap);
		
		log.info("批次号[{}], 统一下单请求报文是:[{}]", checkControl.getId(), requestXml);
		// 3、调用微信统一下单接口 发送请求报文
		byte[] responseData = HttpClientUtil.httpsRequestRetByteArray(downloadbill, "POST",
				requestXml);

		String resultStr = new String(responseData, ConstEC.ENCODE_UTF8);

		if (resultStr.indexOf("return_code") > 0) {
			if (!(resultStr.indexOf("No Bill Exist") > 0)) {
				log.error("[{}]获取对账文件失败 ", checkControl.getId());
				throw new BusiException("22301", ApplicationYmlUtil.get("22301"));
			}
			resultStr = "";
		}

		return resultStr;
	}

	protected void writeFile(String content, String mercId, String fileName, CheckControl checkControl) throws Exception {

		File file = new File(filePath);

		FileOutputStream fos = null;

		try {
			// 如果目录不存在，则新建目录
			if (!file.exists()) {
				file.mkdir();
			}

			file = new File(filePath + File.separator + fileName);
			log.info("批次号[{}], 文件路径[{}]", checkControl.getId(), file.getPath());
			if (file.exists()) {
				log.info("对账文件已经存在，删除已存在的文件[{}]", file.getPath());
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
