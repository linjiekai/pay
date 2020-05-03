package com.mppay.gateway;

import org.junit.Test;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.request.AlipayDataDataserviceBillDownloadurlQueryRequest;
import com.alipay.api.request.AlipayFundTransToaccountTransferRequest;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayDataDataserviceBillDownloadurlQueryResponse;
import com.alipay.api.response.AlipayFundTransToaccountTransferResponse;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;

public class AlipayTest {
	public static String privateKey = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCs6HfxbZZzqctDsL4+YBdQi70+4mH8wlTePrtvjzgvVe5x3Ssgb+VgCxkd/VTkYSJdUt+AmC6BJ0+LrDuSuBTQpd228UB48ngLRbpZu0209aHEcPis8WnLNetdCmV3VWT8Hlech3toLZuRu4WiDlnFCjqeruLBumWunArBO8Rce5n6L6IiX+D9iqVXuVaYIcC1EEnPbK6ZNHA2nx7RQU17hi1uOfSDE4QDF17R0pZetpcD89A16ShSHDFxVbQwtqq/R3sbuhcNuBh29pTJkIFobouV7hmGZY3irkxr5Z2cn/EgCQxP1WP1cv8cQ3s08QlMxf0ilIaScaI7rNUYcFLLAgMBAAECggEBAKsDhdX4kIy3S/4YKiFhQs38V+GBYG7w+aWSoCn3mhdfQ2XE3OnJIjF66I27yKHU3Hs3Ay2Z1q0Q52D6qTU08glnc8GEHUNCda6YeXZUhcfMBgJXxeyvHPbKzo4IQVDk2etuSzLa4RipuytJMCOXU/xJYK8fw9Dd/cHUPeLerRjV2ctjoa4suhcG8zVeHGt9wuHqg4hsumHVuQvf0oVHwYYaKZy4TRY2i5bo/5lwrGrKlHJEL685YR2mHnjagN6iID9nKNlhMJxK+1eB+ck7CGIeBflaPvVTdkWK7tOL7Tv0BHVDZI7GWR0SrumQOmyILV8wmyTIhObnBUxnitF+n0ECgYEA4WVC6HX1AipBCtZ1nNot7YCbbklg5YviGRmuhYGFxP8frziRW/0Y64ehLBlSPt1unLh265m1oFGvnz0EcCLWF1Z5pUNE/0qF30gOqj/HZmyM7Pm4w/e68u64MKyAC+4w9o/NPnu17lgwIErTF7dUihilQL7ERu+BneyHIDfKimECgYEAxGK+wduIbFTceJ0GJ4HHG5aCFR1odMvGqAHpBrUF8xspdpBcuTcG1fYyXTkaQLf/OkzoPE1lwVP0STDZlBz32LRbCA2JuIwJI3qgrblAOgCbcvynPEI9ILulLLNmNrTAbEJj7mUqynZ7dlWORzep/RZVT03mNXUBcu6NUt0YZKsCgYEAnVjqj4uhIgTLaTxz2K77Nvz8BM7XBQhVK76g9fSIpRCRVuo/l58loW8KldWLc/VzPZcM9cZVY/kCiEC5c2ruWFZ3D2bZqPqmYExWaIWjtx36d4iRcsTjTqpWDSyvKyT6K3YSCxej1yLbXf++Q6UpMEqqOnuyW+7hZpTzdAteqqECgYBRlmB/s0/TCdJqfXngEeeBS58RXJ1X4IzQobKI6c3l+MPaERGklPrPCzBTUHTarFZphyf4XYNHtTRI7/WEtiA1tIAZMV6k//CP/9MfOVY4gqYSOGrV+wLgRXTSxFlTovZKQ662jOshH+Yc/GBWsAZ8MboRb2CTlt+aOwcVhOssbwKBgQC2ScCgn28byvWx+JnunoCZXGUT6hgTbFa2RSyfYxGndXFxqSngao8YCzj0g0+1cNf39EqNJ5pSFWg65fm79CSglq7VP6N3iLNi5meWTVqvNCTp+CSo3bZ+vwY4bQ0Mhr+vvVezbJfk2YCqYurS4WtshClQaCj3F8IK/nj48zWsjg==";
	public static String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA9zlY98XWzFIRxaDWuVsriOsot5/DdXJWqLh5bNqpZajDaZmkCDYXvRnqdOUjdd0LD2Pj7/S8v2yeBQY5w5nCumQatEWjDpK1F/1zNs8TZidh6SABbb766yRpDgvmBglsQccMlAKbYpYx/+CHBq5ta0g7clynrfETkJ6xcZ0D1KCCVSpX74ITi4+wTh++PB3GRIOWCLR1A4rZmKUt+dtJ51XijQx7Uf8C74tTKNgfqMlFU6QvlV98YuGtDsALd5Xvx96U7ijIwSnbCkfxGCFVEFm5MWr4erC5E+VcgV2Vuk2ZOMs3UHEwRdDTcQaJDvFwJHw/AlgHXlXfDKA7IuYQHwIDAQAB";

	public static void main(String[] args) throws Exception {
//		pay();

//		query();
//		
//		refundQuery();

//		download();
		
//		page();
		
		oauthToken();
	}

	private static void pay() {
		System.out.println();
		// 实例化客户端
		AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", "2019032063559949",
				privateKey, "json", "UTF-8", publicKey, "RSA2");
		// 实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
		AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
		// SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
		AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
		model.setBody("名品猫商品");
		model.setSubject("名品猫商品");
		model.setOutTradeNo("2019042400000722");
		model.setTimeoutExpress("90m");
		model.setTotalAmount("0.02");
		model.setProductCode("QUICK_MSECURITY_PAY");
		request.setBizModel(model);
		request.setNotifyUrl("https://test-mppay.mingpinmao.cn/notify/alipay/offline");
		try {
			// 这里和普通的接口调用不同，使用的是sdkExecute
			AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
			System.out.println(response.getBody());// 就是orderString 可以直接给客户端请求，无需再做处理。
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
	}

	private static void page() throws Exception {
		AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", "2019032063559949",
				privateKey, "json", "UTF-8", publicKey, "RSA2");
//		AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();// 创建API对应的request
//		alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
//		alipayRequest.setNotifyUrl("http://domain.com/CallBack/notify_url.jsp");// 在公共参数中设置回跳和通知地址
//		alipayRequest.setBizContent("{" + " \"body\":\"商品\"," +"" + " \"out_trade_no\":\"20150320010101002\"," + " \"total_amount\":\"88.88\","
//				+ " \"subject\":\"Iphone6 16G\"," + " \"product_code\":\"QUICK_WAP_PAY\"" + " }");// 填充业务参数
//		String form = "";
//		try {
//			form = alipayClient.pageExecute(alipayRequest).getBody(); // 调用SDK生成表单
//			
//			System.out.println(form);
//		} catch (AlipayApiException e) {
//			e.printStackTrace();
//		}
//		httpResponse.setContentType("text/html;charset=" + CHARSET);
//		httpResponse.getWriter().write(form);// 直接将完整的表单html输出到页面
//		httpResponse.getWriter().flush();
//		httpResponse.getWriter().close();
		
		AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
	    alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
	    alipayRequest.setNotifyUrl("http://domain.com/CallBack/notify_url.jsp");//在公共参数中设置回跳和通知地址
	    alipayRequest.setBizContent("{" +
			"    \"out_trade_no\":\"20150320010101002\"," +
			"    \"total_amount\":88.88," +
//			"    \"subject\":\"aaaa\"," +
			"    \"product_code\":\"QUICK_WAP_WAY\"" +
			"  }");//填充业务参数
	    String form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
	    System.out.println(form);
	}

	private static void query() throws AlipayApiException {
		AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", "2019032063559949",
				privateKey, "json", "UTF-8", publicKey, "RSA2");
		AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
		request.setBizContent("{" + "\"out_trade_no\":\"20150320010101001\","
				+ "\"trade_no\":\"2014112611001004680 073956707\"," + "\"org_pid\":\"2088101117952222\"" + "  }");
		AlipayTradeQueryResponse response = alipayClient.execute(request);
		if (response.isSuccess()) {
			System.out.println("调用成功");
		} else {
			System.out.println("调用失败");
		}
	}

	private static void orderRefund() throws AlipayApiException {
		AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", "2019032063559949",
				privateKey, "json", "UTF-8", publicKey, "RSA2");
		AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
		request.setBizContent(
				"{" + "\"out_trade_no\":\"20150320010101001\"," + "\"trade_no\":\"2014112611001004680073956707\","
						+ "\"refund_amount\":200.12," + "\"refund_currency\":\"USD\"," + "\"refund_reason\":\"正常退款\","
						+ "\"out_request_no\":\"HZ01RF001\"," + "\"operator_id\":\"OP001\","
						+ "\"store_id\":\"NJ_S_001\"," + "\"terminal_id\":\"NJ_T_001\"," + "      \"goods_detail\":[{"
						+ "        \"goods_id\":\"apple-01\"," + "\"alipay_goods_id\":\"20010001\","
						+ "\"goods_name\":\"ipad\"," + "\"quantity\":1," + "\"price\":2000,"
						+ "\"goods_category\":\"34543238\"," + "\"categories_tree\":\"124868003|126232002|126252004\","
						+ "\"body\":\"特价手机\"," + "\"show_url\":\"http://www.alipay.com/xxx.jpg\"" + "        }],"
						+ "      \"refund_royalty_parameters\":[{" + "        \"royalty_type\":\"transfer\","
						+ "\"trans_out\":\"2088101126765726\"," + "\"trans_out_type\":\"userId\","
						+ "\"trans_in_type\":\"userId\"," + "\"trans_in\":\"2088101126708402\"," + "\"amount\":0.1,"
						+ "\"amount_percentage\":100," + "\"desc\":\"分账给2088101126708402\"" + "        }],"
						+ "\"org_pid\":\"2088101117952222\"" + "  }");
		AlipayTradeRefundResponse response = alipayClient.execute(request);
		if (response.isSuccess()) {
			System.out.println("调用成功");
		} else {
			System.out.println("调用失败");
		}
	}

	private static void refundQuery() throws AlipayApiException {
		AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", "2019032063559949",
				privateKey, "json", "UTF-8", publicKey, "RSA2");
		AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
		request.setBizContent("{" + "\"trade_no\":\"20150320010101001\","
				+ "\"out_trade_no\":\"2014112611001004680073956707\","
				+ "\"out_request_no\":\"2014112611001004680073956707\"," + "\"org_pid\":\"2088101117952222\"" + "  }");
		AlipayTradeFastpayRefundQueryResponse response = alipayClient.execute(request);
		if (response.isSuccess()) {
			System.out.println("调用成功");
		} else {
			System.out.println("调用失败");
		}
	}

	private static void download() throws AlipayApiException {
		AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", "2019032063559949",
				privateKey, "json", "UTF-8", publicKey, "RSA2");
		AlipayDataDataserviceBillDownloadurlQueryRequest request = new AlipayDataDataserviceBillDownloadurlQueryRequest();
		request.setBizContent("{" + "\"bill_type\":\"trade\"," + "\"bill_date\":\"2019-04-24\"" + "  }");
		AlipayDataDataserviceBillDownloadurlQueryResponse response = alipayClient.execute(request);

		if (response.isSuccess()) {
			System.out.println("调用成功");
		} else {
			System.out.println("调用失败");
		}
	}
	
	private static void oauthToken() throws AlipayApiException {
		AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", "2019032063559949",
				privateKey, "json", "UTF-8", publicKey, "RSA2");
		AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
		request.setGrantType("authorization_code");
		request.setCode("4b203fe6c11548bcabd8da5bb087a83b");
		request.setRefreshToken("201208134b203fe6c11548bcabd8da5bb087a83b");
		AlipaySystemOauthTokenResponse response = alipayClient.execute(request);
		if(response.isSuccess()){
		System.out.println("调用成功");
		} else {
		System.out.println("调用失败");
		}
	}
	
	@Test
	public void testWithdrOrder() throws Exception {
		
		AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", "2019032063559949",
				privateKey, "json", "UTF-8", publicKey, "RSA2");
		
		AlipayFundTransToaccountTransferRequest request = new AlipayFundTransToaccountTransferRequest();
		request.setBizContent("{" +
		"\"out_biz_no\":\"3142321423432\"," +
		"\"payee_type\":\"ALIPAY_LOGONID\"," +
		"\"payee_account\":\"abc@sina.com\"," +
		"\"amount\":\"12.23\"," +
		"\"payer_show_name\":\"上海交通卡退款\"," +
		"\"payee_real_name\":\"张三\"," +
		"\"remark\":\"转账备注\"" +
		"  }");
		AlipayFundTransToaccountTransferResponse response = alipayClient.execute(request);
		if(response.isSuccess()){
		System.out.println("调用成功");
		} else {
		System.out.println("调用失败");
		}
	}
}
