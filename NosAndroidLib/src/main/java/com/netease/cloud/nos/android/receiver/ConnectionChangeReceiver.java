package com.netease.cloud.nos.android.receiver;

import com.netease.cloud.nos.android.utils.Util;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ConnectionChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Util.netStateChange(context);
	}

}
