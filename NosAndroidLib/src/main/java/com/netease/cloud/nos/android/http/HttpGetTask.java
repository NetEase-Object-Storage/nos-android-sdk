package com.netease.cloud.nos.android.http;

import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import android.content.Context;
import org.apache.http.HttpEntity;
import java.io.IOException;
import com.netease.cloud.nos.android.constants.Code;
import com.netease.cloud.nos.android.core.RequestCallback;
import com.netease.cloud.nos.android.utils.LogUtil;
import com.netease.cloud.nos.android.utils.Util;

public class HttpGetTask implements Runnable {

	private static final String LOGTAG = LogUtil.makeLogTag(HttpGetTask.class);

	protected volatile HttpGet getRequest;

	protected final String url;
	protected final Context ctx;
	protected final Map<String, String> map;
	protected final RequestCallback callback;

	public HttpGetTask(String url, Context ctx, Map<String, String> map,
			RequestCallback callback) {
		this.url = url;
		this.ctx = ctx;
		this.map = map;
		this.callback = callback;
	}

	@Override
	public void run() {
		HttpEntity httpEntity = null;
		
		try {
			getRequest = Util.newGet(url);
			if (map != null) {
				getRequest = (HttpGet) Util.setHeader(getRequest, map);
			}
			HttpResponse response = Util.getLbsHttpClient(ctx).execute(getRequest);

			if (response != null && response.getStatusLine() != null
					&& (httpEntity = response.getEntity()) != null) {
				int statusCode = response.getStatusLine().getStatusCode();
				String result = EntityUtils.toString(httpEntity);
				JSONObject msg = new JSONObject(result);
				if (statusCode == Code.HTTP_SUCCESS) {
					LogUtil.d(LOGTAG,
							"http get response is correct, response: " + result);
				} else {
					LogUtil.d(LOGTAG, "http get response is failed.");
				}
				callback.onResult(new HttpResult(statusCode, msg, null));
			} else {
				callback.onResult(new HttpResult(Code.HTTP_NO_RESPONSE,
						new JSONObject(), null));
			}
		} catch (Exception e) {
			LogUtil.e(LOGTAG, "http get task exception", e);
			callback.onResult(new HttpResult(Code.HTTP_EXCEPTION,
					new JSONObject(), e));
		} finally {
			if (httpEntity != null) {
				try {
					httpEntity.consumeContent();
				} catch (IOException e) {
					LogUtil.e(LOGTAG, "Consume Content exception", e);
				}
			}

			getRequest = null;
		}
	}
}
