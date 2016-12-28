package com.netease.cloud.nos.android.core;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.netease.cloud.nos.android.constants.Code;
import com.netease.cloud.nos.android.exception.InvalidParameterException;
import com.netease.cloud.nos.android.service.MonitorService;
import com.netease.cloud.nos.android.utils.LogUtil;
import com.netease.cloud.nos.android.utils.Util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import java.util.Timer;
import com.netease.cloud.nos.android.monitor.MonitorTask;

public class WanAccelerator {

	private static final String LOGTAG = LogUtil
			.makeLogTag(WanAccelerator.class);

	private static AcceleratorConf conf;
	private static boolean isInit;
	protected static boolean isOpened;
	private static Timer monitorTimer;
	
	public static Map<String, String> map = new ConcurrentHashMap<String, String>();

	private static void initScheduler(Context ctx) {

		if (WanAccelerator.getConf().isMonitorThreadEnabled()) {
			LogUtil.d(LOGTAG, "init monitor timer");
			monitorTimer = new Timer();
			MonitorTask task = new MonitorTask(ctx);
			monitorTimer.schedule(task, getConf().getMonitorInterval(),
					getConf().getMonitorInterval());
		} else {
			LogUtil.d(LOGTAG, "init scheduler");
			Intent intent = new Intent(ctx, MonitorService.class);
			PendingIntent pintent = PendingIntent.getService(ctx, 0, intent, 0);
			AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
			am.setRepeating(AlarmManager.RTC, 0, getConf().getMonitorInterval(), pintent);
		}

	}

	public static UploadTaskExecutor putFileByHttp(Context context, File file,
			Object fileParam, String uploadContext, WanNOSObject obj,
			Callback callback) throws InvalidParameterException {
		Util.checkParameters(context, file, fileParam, obj, callback);
		return put(context, obj.getUploadToken(), obj.getNosBucketName(),
				obj.getNosObjectName(), file, fileParam, uploadContext,
				callback, false, obj);
	}

	public static UploadTaskExecutor putFileByHttps(Context context, File file,
			Object fileParam, String uploadContext, WanNOSObject obj,
			Callback callback) throws InvalidParameterException {
		Util.checkParameters(context, file, fileParam, obj, callback);
		return put(context, obj.getUploadToken(), obj.getNosBucketName(),
				obj.getNosObjectName(), file, fileParam, uploadContext,
				callback, true, obj);
	}

	private static UploadTaskExecutor put(Context context, String uploadToken,
			String bucketName, String fileName, File file, Object fileParam,
			String uploadContext, Callback callback, boolean isHttps,
			WanNOSObject obj) {
		if (!isInit) {
			isInit = true;
			initScheduler(context);
		}
		try {
			UploadTask task = new UploadTask(context, uploadToken, bucketName,
					fileName, file, fileParam, uploadContext, callback,
					isHttps, obj);

			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
				task.execute();
			} else {
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
			return new UploadTaskExecutor(task);
		} catch (Exception e) {
			callback.onFailure(new CallRet(fileParam, uploadContext,
					Code.UNKNOWN_REASON, "", "", null, e));
			return null;
		}
	}

	public static void setConf(AcceleratorConf conf) {
		WanAccelerator.conf = conf;
	}

	public static AcceleratorConf getConf() {
		if (conf == null) {
			conf = new AcceleratorConf();
		}
		return conf;
	}
}
