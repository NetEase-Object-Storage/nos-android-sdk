package com.netease.cloud.nos.android.monitor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import com.netease.cloud.nos.android.constants.Code;
import com.netease.cloud.nos.android.utils.LogUtil;
import com.netease.cloud.nos.android.utils.Util;
import com.netease.cloud.nos.android.monitor.StatisticItem;
import java.util.List;


public class MonitorHttp {

	private static final String LOGTAG = LogUtil.makeLogTag(MonitorHttp.class);

	public static void post(Context ctx, String url) {
		HttpPost postMethod = Util.newPost(Util.getMonitorUrl(url));
		postMethod.addHeader("Content-Encoding", "gzip");
		List<StatisticItem> list = Monitor.get();
		ByteArrayOutputStream bos = Monitor.getPostData(list);
		if(bos == null) {
			LogUtil.d(LOGTAG, "post data is null");
			return;
		}
		postMethod.setEntity(new ByteArrayEntity(bos.toByteArray()));
		try {
			HttpResponse response = Util.getHttpClient(ctx).execute(postMethod);
			if (response != null && response.getStatusLine() != null
					&& response.getEntity() != null) {
				int statusCode = response.getStatusLine().getStatusCode();
				String result = EntityUtils.toString(response.getEntity());
				if (statusCode == Code.HTTP_SUCCESS) {
					LogUtil.d(LOGTAG,
							"http post response is correct, response: "
									+ result);
				} else {
					LogUtil.d(LOGTAG,
							"http post response is failed, status code: "
									+ statusCode);
					if (response.getEntity() != null) {
						LogUtil.d(LOGTAG,
								"http post response is failed, result: "
										+ result);
					}
				}
			}
		} catch (ClientProtocolException e) {
			LogUtil.e(LOGTAG,
					"post monitor data failed with client protocol exception",
					e);
		} catch (IOException e) {
			LogUtil.e(LOGTAG, "post monitor data failed with io exception", e);
		} finally {
//			Monitor.clean();
			if (list != null)
				list.clear();

			if(bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					LogUtil.e(LOGTAG, "bos close exception", e);
				}
			}
		}

	}
}
