package com.netease.cloud.nos.android.monitor;

import com.netease.cloud.nos.android.monitor.StatisticItem;
import com.netease.cloud.nos.android.monitor.MonitorConfig;

interface ISendStat{
    // define a mathod to push StatisticItem to MonitorService 
    boolean sendStat(in StatisticItem item);
	// send config about monitor to service
    void sendConfig(in MonitorConfig config);
}
