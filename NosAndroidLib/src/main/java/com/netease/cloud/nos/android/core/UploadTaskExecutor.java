package com.netease.cloud.nos.android.core;

import com.netease.cloud.nos.android.utils.LogUtil;

public class UploadTaskExecutor {

	private static final String LOGTAG = LogUtil
			.makeLogTag(UploadTaskExecutor.class);

	private volatile UploadTask task;

	public UploadTaskExecutor() {

	}

	public UploadTaskExecutor(UploadTask task) {
		this.task = task;
	}

	public void setTask(UploadTask task) {
		this.task = task;
	}

	public CallRet get() {
		if (task != null) {
			try {
				return task.get();
			} catch (Exception e) {
				LogUtil.e(LOGTAG, "get async task exception", e);
			}
		}
		return null;
	}

	public boolean isUpCancelled() {
		return task != null && task.isUpCancelled();
	}

	public void cancel() {
		if (task != null) {
			try {
				task.cancel();
			} catch (Exception e) {
				LogUtil.e(LOGTAG, "cancel async task exception", e);
			}
		}
	}
}
