package com.mppay.gateway.handler;

public interface CheckCenterHandler {

	/**
	 * 本批次对账
	 * @param checkControl
	 */
	public void check(Long batchId) throws Exception;
	
	/**
	 * 获取对账文件
	 * @param checkControl
	 */
	public void getFile(Long batchId) throws Exception;

	/**
	 * 对账文件入库
	 * @param checkControl
	 */
	public void importData(Long batchId) throws Exception;

	/**
	 * 前一天对账状态
	 * @param checkControl
	 */
	public void lastStatus(Long batchId) throws Exception;

	/**
	 * 数据对账
	 * @param checkControl
	 */
	public void checkData(Long batchId) throws Exception;
	
}
