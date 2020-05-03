package com.mppay.gateway.controller.notify;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.constant.OrderStatus;
import com.mppay.core.constant.PlatformType;
import com.mppay.core.constant.TradeType;
import com.mppay.core.exception.BusiException;
import com.mppay.core.sign.gaohuitong.GaohuitongConstants;
import com.mppay.core.sign.gaohuitong.GaohuitongMessgeUtil;
import com.mppay.core.utils.ResponseUtil;
import com.mppay.core.utils.StringUtil;
import com.mppay.gateway.handler.utils.RequestMsgUtil;
import com.mppay.gateway.scheduled.CheckCenterScheduled;
import com.mppay.gateway.scheduled.CheckControlScheduled;
import com.mppay.gateway.scheduled.TradeOrderQueryScheduled;
import com.mppay.gateway.scheduled.TradeRefundScheduled;
import com.mppay.service.entity.MercOrder;
import com.mppay.service.entity.RouteConf;
import com.mppay.service.entity.TradeOrder;
import com.mppay.service.mapper.TradeOrderMapper;
import com.mppay.service.service.IMercOrderService;
import com.mppay.service.service.ITradeOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/monitor")
@Slf4j
public class MonitorController {

	//0 关闭 1 开启
	public static Integer SCHEDULED_SWITCH = 1;
	//0 关闭 1 开启
	public static Integer MQ_SWITCH = 1;

	@Autowired
	public ITradeOrderService iTradeOrderService;
	@Autowired
	public IMercOrderService mercOrderService;


	@GetMapping("/scheduled")
    public void scheduled(HttpServletRequest request, HttpServletResponse response, String info)  {
		Integer allStatus = 0;
		try {
			log.info("定时器监控请求info[{}]", info);
			switch (info) {
			case "status":
				allStatus = CheckControlScheduled.SCHEDULER_STATUS
						+ CheckCenterScheduled.SCHEDULER_STATUS 
						+ TradeOrderQueryScheduled.SCHEDULER_STATUS
						+ TradeRefundScheduled.SCHEDULER_STATUS
						+ TradeRefundScheduled.SCHEDULER_STATUS2
						;
				break;
			case "start":
				SCHEDULED_SWITCH = 1;
				allStatus = 1;
				break;
			case "stop":
				SCHEDULED_SWITCH = 0;
				allStatus = 1;
				break;
			case "switch":
				allStatus = SCHEDULED_SWITCH;
				break;
			default:
				SCHEDULED_SWITCH = 0;
				allStatus = 0;
			}
			log.info("定时器监控结果info[{}],allStatus[{}]", info, allStatus);
			response.getWriter().write("" + allStatus);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("定时器监控", e);
			try {
				response.getWriter().write("" + allStatus);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	@GetMapping("/mq")
    public void mq(HttpServletRequest request, HttpServletResponse response, String info)  {
		Integer allStatus = 0;
		try {
			log.info("定时器监控请求info[{}]", info);
			switch (info) {
			case "status":
				allStatus = 0
					;
				break;
			case "start":
				MQ_SWITCH = 1;
				allStatus = 1;
				break;
			case "stop":
				MQ_SWITCH = 0;
				allStatus = 1;
				break;
			case "switch":
				allStatus = SCHEDULED_SWITCH;
				break;
			default:
				allStatus = 0;
			}
			log.info("定时器监控结果info[{}],allStatus[{}]", info, allStatus);
			response.getOutputStream().write(("" + allStatus).getBytes());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("定时器监控", e);
			try {
				response.getOutputStream().write(("" + allStatus).getBytes());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	@Value("${scheduled.switch}")
	public void setSwitch(Integer schedulerSwitch) {
		SCHEDULED_SWITCH = schedulerSwitch;
	}


	/**
	 * @Description(描述): 	解析高汇通响应
	 * @auther: Jack Lin
	 * @param :[response]
	 * @return :java.lang.Object
	 * @date: 2020/4/2 12:20
	 */
	@RequestMapping("/encryptResponse")
	public Object encryptResponse(@RequestBody JSONObject response)throws Exception{
		RouteConf routeConf = RequestMsgUtil.getRouteConf(PlatformType.XFYLMALL.getId(), PlatformType.XFYLMALL.getCode(), TradeType.QUICK.getId(), GaohuitongConstants.GHT_ROUTE, null);
		String s = response.getString("response");
		Map map = GaohuitongMessgeUtil.parseMap(s);
		String keyStr = StringUtil.getRandom(16);
		PublicKey publicKey = GaohuitongMessgeUtil.getPublicKey(routeConf.getPublicKeyPath());
		PrivateKey privateKey = GaohuitongMessgeUtil.getPrivateKey(routeConf.getKeyPath());
		String responseHandle = GaohuitongMessgeUtil.responseHandle(map, keyStr,publicKey, privateKey);
		return responseHandle;
	}

	@GetMapping("/queryByOrderNo")
	public Object queryOrderNo(@RequestParam("orderNo") String orderNo )throws Exception{
		MercOrder mercOrder = mercOrderService.getOne(new QueryWrapper<MercOrder>().eq("order_no", orderNo).last("limit 1"));
		Optional.ofNullable(mercOrder).orElseThrow(()->new BusiException(11003));
		List<TradeOrder> tradeOrders = iTradeOrderService.list(new QueryWrapper<TradeOrder>().eq("merc_order_no", mercOrder.getMercOrderNo()));
		return common(orderNo,tradeOrders);
	}

	@GetMapping("/queryByOutTradeNo")
	public Object queryByOutTradeNo(@RequestParam("outTradeNo") String outTradeNo )throws Exception{
		List<TradeOrder> tradeOrders = iTradeOrderService.list(new QueryWrapper<TradeOrder>().eq("out_trade_no", outTradeNo));
		Optional.ofNullable(tradeOrders).orElseThrow(()->new BusiException(11003));
		MercOrder mercOrder = mercOrderService.getOne(new QueryWrapper<MercOrder>().eq("merc_order_no", tradeOrders.get(0).getMercOrderNo()).last("limit 1"));
		return common(mercOrder.getOrderNo(),tradeOrders);
	}


	Map<String,Object> common(String orderNo,List<TradeOrder> tradeOrders){
		Map<String,Object> map = new LinkedHashMap<>();
		map.put("orderNo",orderNo);
		JSONArray jsonArray = new JSONArray();
		for(TradeOrder item : tradeOrders){
			JSONObject object = new JSONObject();
			object.put("outTradeNo",item.getOutTradeNo());
			object.put("status", OrderStatus.parse(item.getOrderStatus()).getName());
			jsonArray.add(object);
		}
		map.put("outTradeNos",jsonArray);
		return map;
	}

}
