package com.netease.cloud.nos.android.monitor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.zip.GZIPOutputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import com.netease.cloud.nos.android.constants.Code;
import com.netease.cloud.nos.android.constants.Constants;
import com.netease.cloud.nos.android.core.WanAccelerator;
import com.netease.cloud.nos.android.utils.LogUtil;
import com.netease.cloud.nos.android.utils.Util;
import com.netease.cloud.nos.android.monitor.MonitorManager;


public class Monitor {
	private static final String LOGTAG = LogUtil.makeLogTag(Monitor.class);
	private static List<StatisticItem> LIST = null;
	private static final int maxListNum = 500 ;
	private static boolean prompt = false;
	
	public static ByteArrayOutputStream getPostData(List<StatisticItem> list) {
		if (list == null || list.size() == 0) {
			return null;
		}
		GZIPOutputStream gos = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			gos = new GZIPOutputStream(bos);
			JSONArray array = new JSONArray();
			for (StatisticItem item : list) {
				array.put(toJSON(item));
			}
			JSONObject jo = new JSONObject();
			jo.put("items", array);
			LogUtil.e(LOGTAG, "monitor result: " + jo.toString());
			gos.write(jo.toString().getBytes("UTF-8"));
			gos.flush();
			gos.finish();
		} catch (IOException e) {
			LogUtil.e(LOGTAG, "get post data io exception", e);
		} catch (JSONException e) {
			LogUtil.e(LOGTAG, "get post data json exception", e);
		} finally {
			if(gos != null) {
				try {
					gos.close();
				} catch (IOException e) {
					LogUtil.e(LOGTAG, "gos close exception", e);
				}
			}
		}
		return bos;
	}

	public static synchronized void clean() {
		if (LIST != null)
			LIST.clear();
	}

	public static void add(Context ctx, StatisticItem item) {
		if (WanAccelerator.getConf().isMonitorThreadEnabled()) {
			LogUtil.d(LOGTAG, "monitor add item for thread");
			if (Monitor.set(item)) {
				LogUtil.d(LOGTAG, "send monitor data immediately");
				MonitorTask task = new MonitorTask(ctx);
				new Timer().schedule(task, 0);
			}
			return;
		}		
		
		MonitorManager.sendStatItem(ctx, item);
	}
	
	public static synchronized boolean set(StatisticItem item) {
		if (LIST == null) {
			LIST = new ArrayList<StatisticItem>();			
		}
			
		LIST.add(item);

		if (LIST.size() >= maxListNum && !prompt) {
			// send it immediately
			LogUtil.d(LOGTAG, "monitor item num " + LIST.size() +
					" >= " + maxListNum);
			prompt = true;
			return true;
		}
	
		return false;
	}

	public static synchronized List<StatisticItem> get() {
		List<StatisticItem> list = LIST;
		LIST = null;
		prompt = false;
		return list;
	}
	
	private static JSONObject toJSON(StatisticItem item) {
		JSONObject data = new JSONObject();
		try {
			data.put("a", item.getPlatform());
			if (item.getClientIP() != null && !item.getClientIP().equals("")) {
				data.put("b", Util.ipToLong(item.getClientIP()));
			}
			data.put("c", item.getSdkVersion());
			if (item.getLbsIP() != null && !item.getLbsIP().equals("")) {
				data.put("d", Util.ipToLong(Util.getIPString(item.getLbsIP())));
			}
			data.put("e", Util.ipToLong(Util.getIPString(item.getUploaderIP())));
			data.put("f", item.getFileSize());
			data.put("g", item.getNetEnv());
			if (item.getLbsUseTime() != Code.MONITOR_SUCCESS) {
				data.put("h", item.getLbsUseTime());
			}
			data.put("i", item.getUploaderUseTime());
			if (item.getLbsSucc() != Code.MONITOR_SUCCESS) {
				data.put("j", item.getLbsSucc());
			}
			if (item.getUploaderSucc() != Code.MONITOR_SUCCESS) {
				data.put("k", item.getUploaderSucc());
			}
			if (item.getLbsHttpCode() != Code.HTTP_SUCCESS) {
				data.put("l", item.getLbsHttpCode());
			}
			if (item.getUploaderHttpCode() != Code.HTTP_SUCCESS) {
				data.put("m", item.getUploaderHttpCode());
			}
			if (item.getUploadRetryCount() != Code.MONITOR_SUCCESS) {
				data.put("n", item.getUploadRetryCount());
			}
			if (item.getChunkRetryCount() != Code.MONITOR_SUCCESS) {
				data.put("o", item.getChunkRetryCount());
			}
			if (item.getQueryRetryCount() != Code.MONITOR_SUCCESS) {
				data.put("p", item.getQueryRetryCount());
			}
			if (item.getBucketName() != null && !item.getBucketName().equals("")) {
				data.put("q", item.getBucketName());
			}
			if (item.getUploadType() != Constants.UPLOAD_TYPE_UNKNOWN) {
				data.put("r", item.getUploadType());
			}
		} catch (JSONException e) {
			LogUtil.e(LOGTAG, "parse statistic item json exception", e);
		}
		return data;
	}
}
