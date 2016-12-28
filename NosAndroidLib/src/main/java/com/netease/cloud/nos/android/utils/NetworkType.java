package com.netease.cloud.nos.android.utils;

import com.netease.cloud.nos.android.core.WanAccelerator;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;


public class NetworkType {

	private String networkType = "";
	private int chunkSize = 1024 * 32;

	public NetworkType(String networkType, int chunkSize) {
		this.networkType = networkType;
		this.chunkSize = chunkSize;
	}	

	public NetworkType(String networkType) {
		this.networkType = networkType;
		this.chunkSize = WanAccelerator.getConf().getChunkSize();	
	}	
	
	public String getNetworkType() {
		return networkType;
	}
	
	public int getChunkSize() {
		return chunkSize;
	}
	

	public static NetworkType getFastMobileNetwork(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		switch (telephonyManager.getNetworkType()) {
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			// ~ 50-100 kbps
		case TelephonyManager.NETWORK_TYPE_CDMA:
			// ~ 14-64 kbps
		case TelephonyManager.NETWORK_TYPE_EDGE:
			// ~ 50-100 kbps
		case TelephonyManager.NETWORK_TYPE_GPRS:
			// ~ 100 kbps
		case TelephonyManager.NETWORK_TYPE_IDEN:
			// ~25 kbps
			return new NetworkType("2g", 1024 * 4);

		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			 // ~ 400-1000 kbps
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			// ~ 600-1400 kbps
		case TelephonyManager.NETWORK_TYPE_UMTS:
			// ~ 400-7000 kbps
			return new NetworkType("3g/4g", 1024 * 32); 

		case TelephonyManager.NETWORK_TYPE_HSPA:
			// ~ 700-1700 kbps
			return new NetworkType("3g/4g", 1024 * 64); 

		case TelephonyManager.NETWORK_TYPE_HSDPA:
			// ~ 2-14 Mbps
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			// ~ 1-23 Mbps
		case TelephonyManager.NETWORK_TYPE_EHRPD:
			// ~ 1-2 Mbps
		case TelephonyManager.NETWORK_TYPE_EVDO_B:
			// ~ 5 Mbps
		case TelephonyManager.NETWORK_TYPE_HSPAP:
			// ~ 10-20 Mbps
		case TelephonyManager.NETWORK_TYPE_LTE:
			// ~ 10+ Mbps
			return new NetworkType("3g/4g", 1024 * 128); 

		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
		default:
			return new NetworkType("2g");

		}
	}

	public static NetworkType getNetWorkType(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			String type = networkInfo.getTypeName();
			if (type.equalsIgnoreCase("WIFI")) {
				return new NetworkType("wifi", 1024 * 128);
			} else if (type.equalsIgnoreCase("MOBILE")) {
				// String proxyHost = android.net.Proxy.getDefaultHost();
				return getFastMobileNetwork(context);
			}
		}
		return new NetworkType("");
	}

}
