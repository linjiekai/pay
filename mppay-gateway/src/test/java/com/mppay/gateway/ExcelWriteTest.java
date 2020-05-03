/**
 * 
 */
package com.mppay.gateway;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.mppay.core.sign.AESCoder;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.*;

/**
 * 写出Excel单元测试
 * 
 * @author looly
 */
public class ExcelWriteTest {

	@Test
	@Ignore
	public void writeTest2() {
		ExcelReader reader = ExcelUtil.getReader(ResourceUtil.getStream("E:\\工作\\数据库数据\\200408\\shop_order.xlsx"),0);
		List<List<Object>> read = reader.read();
		for(List<Object> item : read){
			Object o = item.get(6);
			System.out.print(o+",");
		}
	}

	@Test
	@Ignore
	public void writeTest3() throws Exception {
		Map<String,String> map = new LinkedHashMap<>();
		map.put("CGXRvvsasZYlnYJD9AB6QmrY1K4SJbiU/Ot4JvjO478=","KAR3iRb2SK29sOYn2Cn6PQNrT1e48WDKu1rsrgJBiJU=");
		map.put("X1C73+Su7teWxSx5SE5DM2yt1oDhIYVwbDVgIAucXmg=","/Vl+YfITCH1v5m0VebbT89TMFVwMp4xotj8n065SUbs=");
		map.put("lMWkIopZis4QEZRyr7QSsMbUewWefPhZBA8/sLWHNZQ=","+rrMWznkTaS6Nhtt4fEcBdk6+w12iEpzR3Yl/CuKKUc=");
		Set<String> strings = map.keySet();
		strings.forEach(s->{
			String cardno = null;
			String bankcard = null;
			try {
				cardno = AESCoder.decrypt(s, "G4OVYZye84xquqP7", "IOhUDME4NFD596dm");
				bankcard = AESCoder.decrypt(map.get(s), "G4OVYZye84xquqP7", "IOhUDME4NFD596dm");
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.print(s+"  ::: "+cardno+"     ");
			System.out.println(map.get(s)+"  ::: "+bankcard);
		});
	}

	@Test
	@Ignore
	public void writeTest4() throws Exception {

		List<Integer> list = CollUtil.newArrayList();
		for (int i = 0; i <10 ; i++) {
			list.add(i);
		}
		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, 3,
				0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
		try{

			long l = System.currentTimeMillis();
			for (int i = 0; i <10 ; i++) {
				final int j =i;
				threadPoolExecutor.execute(new Runnable() {
					@Override
					public void run() {
						System.out.print(j+",");
					}
				});
			}
			/*System.out.println();
			long l2 = System.currentTimeMillis();
			for(Integer item : list){
				System.out.print(item+",");
			}
			long l3 = System.currentTimeMillis();
			System.out.println();
			System.out.println("多线程："+(l2-l));
			System.out.println("普通："+(l3-l2));*/
		}finally {
			if(threadPoolExecutor!=null){
				threadPoolExecutor.shutdown();
			}
		}


	}



	@Test
	@Ignore
	public void gg() throws Exception{
		ExcelReader reader = ExcelUtil.getReader(ResourceUtil.getStream("E:\\工作\\数据库数据\\200407\\退款数据汇总20200116(银行卡信息更新)(1).xlsx"));
		List<List<Object>> read = reader.read();
		List<List<?>> rows  =  CollUtil.newArrayList();
		for(List<Object> item : read){
			String o6 = item.get(7)+"";
			String o7 = item.get(8)+"";
			String decrypt6 = "";
			String decrypt7="";
			try{

				decrypt6 = AESCoder.decrypt(o6, "G4OVYZye84xquqP7", "IOhUDME4NFD596dm");
				decrypt7 = AESCoder.decrypt(o7, "G4OVYZye84xquqP7", "IOhUDME4NFD596dm");
			}catch(Exception e){
				decrypt6 = o6;
				decrypt7=o7;
			}

			List<?> row1=CollUtil.newArrayList(item.get(0), item.get(1),item.get(2),item.get(3),item.get(4),item.get(5),item.get(6),decrypt6, decrypt7,item.get(9));
			rows.add(row1);
		}
		String filePath = "E:\\工作\\数据库数据\\200407\\111.xlsx";
		FileUtil.del(filePath);
		// 通过工具类创建writer
		ExcelWriter writer = ExcelUtil.getWriter(filePath);
		// 通过构造方法创建writer
		// ExcelWriter writer = new ExcelWriter("d:/writeTest.xls");

		// 跳过当前行，既第一行，非必须，在此演示用
		writer.passCurrentRow();
		// 合并单元格后的标题行，使用默认标题样式
		writer.merge(read.size(), "测试标题");
		// 一次性写出内容，使用默认样式
		writer.write(read);
		writer.autoSizeColumn(0, true);
		// 关闭writer，释放内存
		writer.close();
	}

	@Test
	@Ignore
	public void writeTest() {
		ExcelReader reader = ExcelUtil.getReader(ResourceUtil.getStream("提现银行卡数据2.xlsx"));
		List<Map<String, Object>> readAll = reader.readAll();

		List<?> row1 = CollUtil.newArrayList("aaaaa", "bb", "cc", "dd", DateUtil.date(), 3.22676575765);
		List<?> row2 = CollUtil.newArrayList("aa1", "bb1", "cc1", "dd1", DateUtil.date(), 250.7676);
		List<?> row3 = CollUtil.newArrayList("aa2", "bb2", "cc2", "dd2", DateUtil.date(), 0.111);
		List<?> row4 = CollUtil.newArrayList("aa3", "bb3", "cc3", "dd3", DateUtil.date(), 35);
		List<?> row5 = CollUtil.newArrayList("aa4", "bb4", "cc4", "dd4", DateUtil.date(), 28.00);

		List<List<?>> rows = CollUtil.newArrayList(row1, row2, row3, row4, row5);
		for (int i = 0; i < 400; i++) {
			// 超大列表写出测试
			rows.add(ObjectUtil.clone(row1));
		}

		String filePath = "e:/writeTest.xlsx";
		FileUtil.del(filePath);
		// 通过工具类创建writer
		ExcelWriter writer = ExcelUtil.getWriter(filePath);
		// 通过构造方法创建writer
		// ExcelWriter writer = new ExcelWriter("d:/writeTest.xls");

		// 跳过当前行，既第一行，非必须，在此演示用
		writer.passCurrentRow();
		// 合并单元格后的标题行，使用默认标题样式
		writer.merge(row1.size() - 1, "测试标题");
		// 一次性写出内容，使用默认样式
		writer.write(rows);
		writer.autoSizeColumn(0, true);
		// 关闭writer，释放内存
		writer.close();
	}



	@Test
	@Ignore
	public void mergeTest() {
		List<?> row1 = CollUtil.newArrayList("aa", "bb", "cc", "dd", DateUtil.date(), 3.22676575765);
		List<?> row2 = CollUtil.newArrayList("aa1", "bb1", "cc1", "dd1", DateUtil.date(), 250.7676);
		List<?> row3 = CollUtil.newArrayList("aa2", "bb2", "cc2", "dd2", DateUtil.date(), 0.111);
		List<?> row4 = CollUtil.newArrayList("aa3", "bb3", "cc3", "dd3", DateUtil.date(), 35);
		List<?> row5 = CollUtil.newArrayList("aa4", "bb4", "cc4", "dd4", DateUtil.date(), 28.00);

		List<List<?>> rows = CollUtil.newArrayList(row1, row2, row3, row4, row5);

		// 通过工具类创建writer
		ExcelWriter writer = ExcelUtil.getWriter("e:/mergeTest.xlsx");
		/*CellStyle style = writer.getStyleSet().getHeadCellStyle();
		StyleUtil.setColor(style, IndexedColors.RED, FillPatternType.SOLID_FOREGROUND);*/

		// 跳过当前行，既第一行，非必须，在此演示用
		writer.passCurrentRow();
		// 合并单元格后的标题行，使用默认标题样式
		writer.merge(row1.size() - 1, "测试标题");
		// 一次性写出内容，使用默认样式
		writer.write(rows);

		// 合并单元格后的标题行，使用默认标题样式
		writer.merge(7, 10, 4, 10, "测试Merge", false);

		// 关闭writer，释放内存
		writer.close();
	}

	@Test
	@Ignore
	public void writeMapTest() {
		Map<String, Object> row1 = new LinkedHashMap<>();
		row1.put("姓名", "张三");
		row1.put("年龄", 23);
		row1.put("成绩", 88.32);
		row1.put("是否合格", true);
		row1.put("考试日期", DateUtil.date());

		Map<String, Object> row2 = new LinkedHashMap<>();
		row2.put("姓名", "李四");
		row2.put("年龄", 33);
		row2.put("成绩", 59.50);
		row2.put("是否合格", false);
		row2.put("考试日期", DateUtil.date());

		ArrayList<Map<String, Object>> rows = CollUtil.newArrayList(row1, row2);

		// 通过工具类创建writer
		ExcelWriter writer = ExcelUtil.getWriter("e:/writeMapTest.xlsx");

		// 设置内容字体
	/*	Font font = writer.createFont();
		font.setBold(true);
		font.setColor(Font.COLOR_RED);
		font.setItalic(true);
		writer.getStyleSet().setFont(font, true);*/

		// 合并单元格后的标题行，使用默认标题样式
		writer.merge(row1.size() - 1, "一班成绩单");
		// 一次性写出内容，使用默认样式
		writer.write(rows, true);
		// 关闭writer，释放内存
		writer.close();
	}

	@Test
	@Ignore
	public void writeMapTest2() {
		Map<String, Object> row1 = MapUtil.newHashMap(true);
		row1.put("姓名", "张三");
		row1.put("年龄", 23);
		row1.put("成绩", 88.32);
		row1.put("是否合格", true);
		row1.put("考试日期", DateUtil.date());

		// 通过工具类创建writer
		ExcelWriter writer = ExcelUtil.getWriter("e:/writeMapTest2.xlsx");

		// 一次性写出内容，使用默认样式
		writer.writeRow(row1, true);
		// 关闭writer，释放内存
		writer.close();
	}
	
	@Test
	@Ignore
	public void writeMapAliasTest() {
		Map<Object, Object> row1 = new LinkedHashMap<>();
		row1.put("name", "张三");
		row1.put("age", 22);
		row1.put("isPass", true);
		row1.put("score", 66.30);
		row1.put("examDate", DateUtil.date());
		Map<Object, Object> row2 = new LinkedHashMap<>();
		row2.put("name", "李四");
		row2.put("age", 233);
		row2.put("isPass", false);
		row2.put("score", 32.30);
		row2.put("examDate", DateUtil.date());
		
		List<Map<Object, Object>> rows = CollUtil.newArrayList(row1, row2);
		// 通过工具类创建writer
		String file = "e:/writeMapAlias.xlsx";
		FileUtil.del(file);
		ExcelWriter writer = ExcelUtil.getWriter(file);
		// 自定义标题
		writer.addHeaderAlias("name", "姓名");
		writer.addHeaderAlias("age", "年龄");
		writer.addHeaderAlias("score", "分数");
		writer.addHeaderAlias("isPass", "是否通过");
		writer.addHeaderAlias("examDate", "考试时间");
		// 合并单元格后的标题行，使用默认标题样式
		writer.merge(4, "一班成绩单");
		// 一次性写出内容，使用默认样式
		writer.write(rows, true);
		// 关闭writer，释放内存
		writer.close();
	}
	
	@Test
	@Ignore
	public void writeMapOnlyAliasTest() {
		Map<Object, Object> row1 = new LinkedHashMap<>();
		row1.put("name", "张三");
		row1.put("age", 22);
		row1.put("isPass", true);
		row1.put("score", 66.30);
		row1.put("examDate", DateUtil.date());
		Map<Object, Object> row2 = new LinkedHashMap<>();
		row2.put("name", "李四");
		row2.put("age", 233);
		row2.put("isPass", false);
		row2.put("score", 32.30);
		row2.put("examDate", DateUtil.date());
		
		List<Map<Object, Object>> rows = CollUtil.newArrayList(row1, row2);
		// 通过工具类创建writer
		String file = "e:/writeMapOnlyAlias.xlsx";
		FileUtil.del(file);
		ExcelWriter writer = ExcelUtil.getWriter(file);
		writer.setOnlyAlias(true);
		// 自定义标题
		writer.addHeaderAlias("name", "姓名");
		writer.addHeaderAlias("age", "年龄");
		// 合并单元格后的标题行，使用默认标题样式
		writer.merge(4, "一班成绩单");
		// 一次性写出内容，使用默认样式
		writer.write(rows, true);
		// 关闭writer，释放内存
		writer.close();
	}


}
