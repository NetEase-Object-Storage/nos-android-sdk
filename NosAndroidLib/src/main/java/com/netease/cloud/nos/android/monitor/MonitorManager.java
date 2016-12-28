package com.netease.cloud.nos.android.monitor;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.netease.cloud.nos.android.utils.LogUtil;
import com.netease.cloud.nos.android.service.MonitorService;
import com.netease.cloud.nos.android.core.WanAccelerator;
import com.netease.cloud.nos.android.monitor.ISendStat;


public class MonitorManager {
	private static final String LOGTAG = LogUtil.makeLogTag(MonitorManager.class);

    private static boolean monitorConfigInit = false;
	
	// create an object to manage Monitor Service
	private Context ctx = null;
	private StatisticItem item = null;
	private ISendStat instSendStat = null;
    private ServiceConnection instConn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        	instSendStat = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        	instSendStat = ISendStat.Stub.asInterface(service);
            LogUtil.d(LOGTAG, "Stat onServiceConnected, " + "instSendStat=" + instSendStat);
			// send config
			instSendConfig();
			// send statistic data
			instSendStatItem();
            instEndService();
        }
    };
	
    public MonitorManager(Context ctx, StatisticItem item) {
    	this.ctx = ctx;
    	this.item = item;
    }
    
    /**
     * send config to MonitorService
     */
    public void instSendConfig() {

    	if (instSendStat == null) {
			LogUtil.w(LOGTAG, "instSendStat is null, not bind to MonitorService");
			return;
    	}

		if (monitorConfigInit)
			return;

    	try {
    		MonitorConfig config = new MonitorConfig(WanAccelerator.getConf().getMonitorHost(),
    				WanAccelerator.getConf().getConnectionTimeout(),
    				WanAccelerator.getConf().getSoTimeout(),
    				WanAccelerator.getConf().getMonitorInterval());
    		instSendStat.sendConfig(config);
			LogUtil.d(LOGTAG, "send config to MonitorService");
    	} catch (Exception e) {
			LogUtil.e(LOGTAG,
					"send MonitorConfig exception: " + e.getMessage()
					+ "instSendStat=" + instSendStat);
			e.printStackTrace();
        }
    }

    /**
     * send statistic data to MonitorService
     */
    public void instSendStatItem() {
    	if (instSendStat == null) {
			LogUtil.w(LOGTAG, "instSendStat is null, not bind to MonitorService");
			return;
    	}
    		
    	try {
    		monitorConfigInit = instSendStat.sendStat(item);
			LogUtil.d(LOGTAG, "send statistic to MonitorService, get configInit "
							+ monitorConfigInit);
    	} catch (Exception e) {
			LogUtil.e(LOGTAG,
					"send Statistic data exception: " + e.getMessage()
					+ "instSendStat=" + instSendStat);
			e.printStackTrace();
        }
    }

    /* bind Stat Service */
    public void instStartService() {
    	if (instSendStat != null) {
    		// Stat Service has binded
    		return;
    	}
    	
        Intent service = new Intent(ctx, MonitorService.class);
        ctx.bindService(service, instConn, Context.BIND_AUTO_CREATE);
		LogUtil.d(LOGTAG, "bind MonitorService, instSendStat=" + instSendStat);
    }

    /* unbind Monitor Service */
    public void instEndService() {
    	ctx.unbindService(instConn);
		LogUtil.d(LOGTAG, "unbind MonitorService success");
    }

	// static func to manage MonitService
    private static boolean running = false;
    private static int refCount = 0;
    private static ISendStat iSendStat = null;
    private static ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            iSendStat = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iSendStat = ISendStat.Stub.asInterface(service);
            LogUtil.d(LOGTAG, "Stat onServiceConnected, " + "iSendStat=" + iSendStat);
        }
    };
	
    /**
     * send statistic data to MonitorService
     */
    public static void sendStatItem(Context ctx, StatisticItem item) {
    	if (iSendStat == null) {
			LogUtil.d(LOGTAG, "iSendStat is null, bind to MonitorService");
			// init Monitor Manager
			runService(ctx);
			new MonitorManager(ctx, item).instStartService();
			return;
    	}
    		
    	try {
        	iSendStat.sendStat(item);
        } catch (Exception e) {
			LogUtil.e(LOGTAG,
					"send Statistic data exception: " + e.getMessage()
					+ "iSendStat=" + iSendStat);
			e.printStackTrace();
        }
    }

    /* bind Stat Service */
    public static synchronized void startService(Context ctx) {
    	if (refCount++ > 0) {
    		LogUtil.d(LOGTAG, "MonitorService has binded: refCount=" + refCount);
    		return;
    	}
    	
    	if (iSendStat != null) {
    		// Stat Service has started
    		return;
    	}
    	
		Context context = ctx.getApplicationContext();
		Intent service = new Intent(context, MonitorService.class);
		context.bindService(service, conn, Context.BIND_AUTO_CREATE);
		LogUtil.d(LOGTAG, "bind MonitorService, iSendStat=" + iSendStat);
    }

    /* unbind Stat Service */
    public static synchronized void endService(Context ctx) {
    	if ((refCount == 0) || (refCount-- > 1)) {
			LogUtil.d(LOGTAG,
					"MonitorService has binded to else or unbinded: refCount="
							+ refCount);
    		return;
    	}

    	ctx.getApplicationContext().unbindService(conn);
		LogUtil.d(LOGTAG, "unbind MonitorService success");
    }

    private static synchronized void runService(Context ctx) {
    	if (running) {
    		return;
    	}

    	running = true;
		LogUtil.d(LOGTAG, "init MonitorService");
		ctx.startService(new Intent(ctx, MonitorService.class));
    }

}	
