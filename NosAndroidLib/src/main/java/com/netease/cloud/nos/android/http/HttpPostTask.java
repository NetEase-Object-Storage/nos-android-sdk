package com.netease.cloud.nos.android.http;

import java.io.IOException;
import java.util.concurrent.Callable;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;

import com.netease.cloud.nos.android.constants.Code;
import com.netease.cloud.nos.android.constants.Constants;
import com.netease.cloud.nos.android.utils.LogUtil;
import com.netease.cloud.nos.android.utils.Util;

public class HttpPostTask implements Callable<HttpResult> {

	private static final String LOGTAG = LogUtil.makeLogTag(HttpPostTask.class);

	protected volatile HttpPost postRequest;

	protected final String url;
	protected final String token;
	protected final Context ctx;
	protected final byte[] chunkData;

	public HttpPostTask(String url, String token, Context ctx, byte[] chunkData) {
		this.url = url;
		this.token = token;
		this.ctx = ctx;
		this.chunkData = chunkData;
	}

	@Override
	public HttpResult call() throws Exception {
		LogUtil.d(LOGTAG, "http post task is executing");
		HttpResult rs = null;
		try {
			postRequest = Util.newPost(url);
			postRequest.addHeader(Constants.HEADER_TOKEN, token);
			postRequest.setEntity(buildHttpEntity(chunkData));
			HttpResponse response = Util.getHttpClient(ctx)
					.execute(postRequest);

			if (response != null && response.getStatusLine() != null
					&& response.getEntity() != null) {
				int statusCode = response.getStatusLine().getStatusCode();
				String result = EntityUtils.toString(response.getEntity());
				if (statusCode == Code.HTTP_SUCCESS) {
					LogUtil.d(LOGTAG,
							"http post response is correct, response: "
									+ result);
				} else {
					rs = new HttpResult(statusCode, null, null);
					LogUtil.d(LOGTAG,
							"http post response is failed, status code: "
									+ statusCode);
					if (response.getEntity() != null) {
						LogUtil.d(LOGTAG,
								"http post response is failed, result: "
										+ result);
					}
				}
				rs = new HttpResult(statusCode, new JSONObject(result), null);
			} else {
				rs = new HttpResult(Code.HTTP_NO_RESPONSE, null, null);
			}
		} catch (Exception e) {
			LogUtil.e(LOGTAG, "http post exception", e);
			rs = new HttpResult(Code.HTTP_EXCEPTION, null, e);
		} finally {
			postRequest = null;
		}
		return rs;
	}

	private HttpEntity buildHttpEntity(byte[] isa) throws IOException {
		ByteArrayEntity en = new ByteArrayEntity(isa);
		return en;
	}
}
