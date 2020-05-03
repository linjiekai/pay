package com.mppay.gateway.handler;

import com.mppay.gateway.dto.RequestMsg;
import com.mppay.gateway.dto.ResponseMsg;

public interface UnifiedHandler {

	public ResponseMsg execute(RequestMsg requestMsg) throws Exception;
}
