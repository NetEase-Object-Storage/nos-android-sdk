package com.netease.cloud.nos.android.core;

public interface Callback {
	void onUploadContextCreate(Object fileParam, String oldUploadContext,
			String newUploadContext);

	void onProcess(Object fileParam, long current, long total);

	void onSuccess(CallRet ret);

	void onFailure(CallRet ret);
	
	void onCanceled(CallRet ret);
}
