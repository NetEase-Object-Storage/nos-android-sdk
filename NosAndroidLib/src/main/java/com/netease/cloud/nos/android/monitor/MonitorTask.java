package com.netease.cloud.nos.android.monitor;

import java.util.TimerTask;  
import android.content.Context;
import com.netease.cloud.nos.android.core.WanAccelerator;
import com.netease.cloud.nos.android.utils.LogUtil;

public class MonitorTask extends TimerTask {  
	private static final String LOGTAG = LogUtil.makeLogTag(MonitorTask.class);
	private Context ctx;

	public MonitorTask(Context ctx) {
		super();
		this.ctx = ctx;
	}

	@Override
	public void run() {  
		LogUtil.d(LOGTAG, "run MonitorTask");
		MonitorHttp.post(ctx, WanAccelerator.getConf().getMonitorHost());
	}

}  
