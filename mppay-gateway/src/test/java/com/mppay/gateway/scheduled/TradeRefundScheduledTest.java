package com.mppay.gateway.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class TradeRefundScheduledTest {


    @Autowired
    TradeRefundScheduled tradeRefundScheduled;

    @Test
    public void tradeRefund() {
        tradeRefundScheduled.tradeRefund();
    }

    @Test
    public void tradeRefundQuery() {
    }
}