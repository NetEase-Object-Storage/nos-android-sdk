package com.netease.cloud.nos.android.core;

import android.util.Base64;
import com.netease.cloud.nos.android.constants.Code;

public class CallRet {

	private Object fileParam;
	private String uploadContext;
	private int httpCode;
	private String response;
	private Exception exception;
	private String requestId;
	private String callbackRetMsg;

	public CallRet(Object fileParam, String uploadContext, int httpCode,
			String requestId, String callbackRetMsg, String response,
			Exception exception) {
		this.fileParam = fileParam;
		this.uploadContext = uploadContext;
		this.httpCode = httpCode;
		this.requestId = requestId;
		this.callbackRetMsg = new String(Base64.decode(callbackRetMsg,
				Base64.DEFAULT));
		this.response = response;
		this.exception = exception;
	}

	public Object getFileParam() {
		return fileParam;
	}

	public void setFileParam(Object fileParam) {
		this.fileParam = fileParam;
	}

	public String getUploadContext() {
		return uploadContext;
	}

	public void setUploadContext(String uploadContext) {
		this.uploadContext = uploadContext;
	}

	public int getHttpCode() {
		return httpCode;
	}

	public void setHttpCode(int httpCode) {
		this.httpCode = httpCode;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getCallbackRetMsg() {
		return callbackRetMsg;
	}

	public void setCallbackRetMsg(String callbackRetMsg) {
		this.callbackRetMsg = callbackRetMsg;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public boolean isOK() {
		if (this.httpCode == Code.HTTP_SUCCESS) {
			return true;
		} else {
			return false;
		}
	}

}
