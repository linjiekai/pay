package com.mppay.gateway;

import com.mppay.core.constant.Align;
import com.mppay.service.service.ISeqIncrService;

public class MyThread implements Runnable {

	private ISeqIncrService sequenceService;
	private String name;

	private int index = 1;

	public MyThread(ISeqIncrService sequenceService, String name) {
		this.sequenceService = sequenceService;
		this.name = name;
	}

	@Override
	public void run() {
		long ct = System.currentTimeMillis();
		System.out.println(sequenceService.nextVal("user_no", 10, Align.LEFT) + ":" + name + "--" + index++ + " -- " + (ct - System.currentTimeMillis()));
		/*
		 * for (int i = 0; i < 10; i++) { long ct = System.currentTimeMillis();
		 * System.out.println(sequenceService.nextVal("user_no", 10, Align.LEFT) + ":" +
		 * name + "--" + index++ + " -- " + (ct - System.currentTimeMillis())); }
		 */
	}

}
