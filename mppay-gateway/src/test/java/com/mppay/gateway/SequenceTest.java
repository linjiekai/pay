package com.mppay.gateway;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.mppay.core.constant.Align;
import com.mppay.service.service.ISeqIncrService;
import com.mppay.service.service.impl.SeqIncrServiceImpl;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SequenceTest extends BaseTest {

	@Test
	public void testCardBing() throws Exception {
		
		
		ISeqIncrService sequenceService = wac.getBean(SeqIncrServiceImpl.class);
		
	
		for (int i = 0; i < 21; i++) {
			String str = sequenceService.nextVal("refund_no", 10, Align.LEFT);
			System.out.println(str);
		}
		
		
	}

}
