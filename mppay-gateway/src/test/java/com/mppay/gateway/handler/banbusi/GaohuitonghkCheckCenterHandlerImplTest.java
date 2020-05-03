package com.mppay.gateway.handler.banbusi;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mppay.core.config.SpringContextHolder;
import com.mppay.core.constant.ConstEC;
import com.mppay.core.constant.TradeCode;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.core.utils.DateTimeUtil;
import com.mppay.gateway.handler.CheckCenterHandler;
import com.mppay.service.entity.CheckControl;
import com.mppay.service.service.ICheckControlService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import lombok.extern.slf4j.Slf4j;


@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class GaohuitonghkCheckCenterHandlerImplTest {

    @Autowired
    protected ApplicationContext ctx;
    @Autowired
    private ICheckControlService checkControlService;
    @Autowired
    private CheckCenterHandler gaohuitonghkCheckCenterHandler;
    @Autowired
    private CheckCenterHandler gaohuitonghkRefundCheckCenterHandler;
    @Value("${ght.xfhl.addressOverseaXfyinli}")
    private String addressOverseaXfyinli;
    @Value("${ght.xfhl.version}")
    private String version;
    @Value("${gaohuitong.overseas}")
    private String overseasUrl;

    @Test
    public void tradeCheck() throws Exception {
        CheckControl checkControl = new CheckControl();
        checkControl.setAccountDate(DateTimeUtil.date10(DateTimeUtil.beforeDay(6)));
        checkControl.setRouteCode("GAOHUITONGHK");
        checkControl.setTradeCode(TradeCode.CONSUMER.getId());
        checkControl.setCreateDate(DateTimeUtil.date10());
      //  checkControlService.save(checkControl);
        gaohuitonghkCheckCenterHandler.check(928l);
    }

    @Test
    public void refundCheck() throws Exception {
        CheckControl checkControl = new CheckControl();
        checkControl.setAccountDate(DateTimeUtil.date10(DateTimeUtil.beforeDay(1)));
        checkControl.setRouteCode("GAOHUITONGHK");
        checkControl.setTradeCode(TradeCode.TRADEREFUND.getId());
        checkControl.setCreateDate(DateTimeUtil.date10());
       // checkControlService.save(checkControl);
        gaohuitonghkRefundCheckCenterHandler.check(933l);
    }


    @Test
    public void gg() throws Exception {
        List<CheckControl> checkControlList = checkControlService.list(
                new QueryWrapper<CheckControl>().in("check_status", 0, 1, 2).eq("route_code","GAOHUITONGHK")
                        .orderByAsc("id")
        );
        CheckCenterHandler checkCenterHandler = null;
        String serviceName = null;
        for (CheckControl checkControl : checkControlList) {
            try {
                serviceName = checkControl.getRouteCode().toLowerCase();
                if (checkControl.getTradeCode().equals(TradeCode.TRADEREFUND.getId())) {
                    serviceName += "Refund";
                }
                serviceName += ConstEC.CHECKCENTERHANDLER;
                checkCenterHandler = SpringContextHolder.getBean(serviceName);
                checkCenterHandler.check(checkControl.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    @Test
    public void ww() {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("提现银行卡数据2.xlsx");
        ExcelReader reader = ExcelUtil.getReader(resourceAsStream);
        List<Map<String, Object>> readAll = reader.readAll();
        List<List<?>> rows  =  CollUtil.newArrayList();
        for(Map<String, Object> map : readAll){

            List<?> row1= CollUtil.newArrayList("aaaaa", "bb", "cc", "dd", DateUtil.date(), 3.22676575765);
            rows.add(row1);
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
        writer.merge(3000, "测试标题");
        // 一次性写出内容，使用默认样式
        writer.write(rows);
        writer.autoSizeColumn(0, true);
        // 关闭writer，释放内存
        writer.close();
    }


}
