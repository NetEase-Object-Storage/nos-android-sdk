package com.netease.cloud.nos.android.http;

import org.json.JSONObject;

public class HttpResult {
	private int statusCode;
	private JSONObject msg;
	private Exception exception;

	public HttpResult(int statusCode, JSONObject msg, Exception e) {
		this.statusCode = statusCode;
		this.msg = msg;
		this.exception = e;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public JSONObject getMsg() {
		return msg;
	}

	public void setMsg(JSONObject msg) {
		this.msg = msg;
	}

	public Exception getException() {
		return exception;
	}

	protected void setException(Exception exception) {
		this.exception = exception;
	}

}
