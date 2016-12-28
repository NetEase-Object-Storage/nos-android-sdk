package com.netease.cloud.nos.android.core;

import com.netease.cloud.nos.android.http.HttpResult;

public interface RequestCallback {
	void onResult(HttpResult result);
}
