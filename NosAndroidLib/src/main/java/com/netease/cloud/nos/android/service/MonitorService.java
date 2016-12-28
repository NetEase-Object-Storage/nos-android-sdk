package com.netease.cloud.nos.android.service;

import com.netease.cloud.nos.android.core.AcceleratorConf;
import com.netease.cloud.nos.android.core.WanAccelerator;
import com.netease.cloud.nos.android.exception.InvalidParameterException;
import com.netease.cloud.nos.android.monitor.Monitor;
import com.netease.cloud.nos.android.monitor.MonitorHttp;
import com.netease.cloud.nos.android.monitor.StatisticItem;
import com.netease.cloud.nos.android.monitor.ISendStat.Stub;
import com.netease.cloud.nos.android.utils.LogUtil;
import com.netease.cloud.nos.android.monitor.MonitorConfig;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class MonitorService extends Service {
	private static final String LOGTAG = LogUtil
			.makeLogTag(MonitorService.class);

	private MsgBinder msgBinder = null;
	private boolean configInit = false;

    public class MsgBinder extends Stub {

        @Override
		public boolean sendStat(StatisticItem item) throws RemoteException {
			if (Monitor.set(item)) {
				LogUtil.d(LOGTAG, "send monitor data immediately");
				postMonitorData();
			}

			return configInit;
        }

        @Override
		public void sendConfig(MonitorConfig config) throws RemoteException {
    		LogUtil.d(LOGTAG, "Receive Monitor config" + config.getMonitorHost());

    		// init AcceleratorConf with Monitor config
    		AcceleratorConf conf = WanAccelerator.getConf();

    		conf.setMontiroHost(config.getMonitorHost());
    		conf.setMonitorInterval(config.getMonitorInterval()); 
    		
    		try {
				conf.setConnectionTimeout(config.getConnectionTimeout());
				conf.setSoTimeout(config.getSoTimeout());
    		} catch (InvalidParameterException e) {
    			e.printStackTrace();
    		}

			LogUtil.d(LOGTAG, "current Monitor config"
					+ WanAccelerator.getConf().getMonitorHost());

			configInit = true;
        }

    }
    
	@Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
		LogUtil.d(LOGTAG, "MonitorService onBind");
		return msgBinder;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
		LogUtil.d(LOGTAG, "MonitorService onCreate");
        super.onCreate();
        msgBinder = new MsgBinder();
    }

    @Override
    public void onDestroy() {
		LogUtil.d(LOGTAG, "MonitorService onDestroy");
    	msgBinder = null;
        super.onDestroy();
    }

	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent intent, int startId) {
		LogUtil.d(LOGTAG, "Service onStart");
		super.onStart(intent, startId);

		postMonitorData();

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogUtil.d(LOGTAG, "Service onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	private void postMonitorData() {

		new Thread(new Runnable() {
			@Override
			public void run() {

				LogUtil.d(LOGTAG, "postMonitorData to host "
						+ WanAccelerator.getConf().getMonitorHost());

				MonitorHttp.post(MonitorService.this,
						WanAccelerator.getConf().getMonitorHost());
			}
		}).start();

	}

}
