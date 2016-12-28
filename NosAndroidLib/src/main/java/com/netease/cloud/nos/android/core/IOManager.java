package com.netease.cloud.nos.android.core;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.json.JSONObject;

import com.netease.cloud.nos.android.constants.Code;
import com.netease.cloud.nos.android.constants.Constants;
import com.netease.cloud.nos.android.http.HttpGetTask;
import com.netease.cloud.nos.android.http.HttpResult;
import com.netease.cloud.nos.android.utils.LogUtil;
import com.netease.cloud.nos.android.utils.Util;

import android.content.Context;

public class IOManager {

	private static final String LOGTAG = LogUtil.makeLogTag(IOManager.class);

	public static HttpResult getLBSAddress(Context ctx, String bucketName, boolean useLBSKey) {
		String urls = WanAccelerator.getConf().getLbsHost() + ";" + WanAccelerator.getConf().getLbsIP();
		HttpResult result = null;
		String lbsIP = Util.getData(ctx, bucketName + Constants.LBS_KEY);
		
		if (useLBSKey && lbsIP != null) {
			urls = lbsIP + ";" + urls;
		}

		LogUtil.d(LOGTAG, "get lbs address with multiple urls: " + urls);
		
		String[] urlArray = urls.split(";");
		for (String url : urlArray) {
			LogUtil.d(LOGTAG, "get lbs address with url: " + url);
			result = executeQueryTask(
					Util.buildLBSUrl(url, bucketName), ctx, null);
			if (result.getStatusCode() == Code.HTTP_SUCCESS) {
				JSONObject msg = result.getMsg();
				LogUtil.d(LOGTAG, "LBS address result: " + msg.toString());
				result = Util.setLBSData(ctx, bucketName, msg);
				if (result.getStatusCode() == Code.HTTP_SUCCESS)
					return result;
			}

			LogUtil.w(LOGTAG, "failed to query LBS url " + url 
					+ " result: " + result.getStatusCode() 
					+ " msg: " + result.getMsg().toString());
		}

		if (result == null) {
			result = new HttpResult(Code.LBS_ERROR, new JSONObject(), null);
		}
		
		return result;
	}

	private static HttpResult executeQueryTask(String url, Context ctx,
			Map<String, String> map) {
		final HttpResult[] result = { null };
		final CountDownLatch latch = Util.acquireLock();

		HttpGetTask task = new HttpGetTask(url, ctx, map,
				new RequestCallback() {
					@Override
					public void onResult(HttpResult rs) {
						if (rs.getStatusCode() != Code.HTTP_SUCCESS) {
							LogUtil.w(LOGTAG, "http query failed status code: "
									+ rs.getStatusCode());
						} else {
							LogUtil.d(LOGTAG, "http query success");
						}
						result[0] = rs;
						Util.releaseLock(latch);
					}
				});
		ExecutorService executor = Util.getExecutorService();
		executor.execute(task);
		Util.setLock(latch);
		return result[0];
	}

}
