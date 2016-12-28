package com.netease.cloud.nos.android.core;

import com.netease.cloud.nos.android.constants.Constants;
import com.netease.cloud.nos.android.exception.InvalidChunkSizeException;
import com.netease.cloud.nos.android.exception.InvalidParameterException;
import com.netease.cloud.nos.android.utils.LogUtil;
import com.netease.cloud.nos.android.utils.Util;
import org.apache.http.client.HttpClient;
import android.text.format.DateUtils;

public class AcceleratorConf {

	private static final String LOGTAG = LogUtil.makeLogTag(AcceleratorConf.class);
	
	private String lbsHost = "http://lbs.eastchina1.126.net/lbs";
	private String lbsIP = "http://106.2.45.249/lbs";
	private String monitorHost = "http://lbs.eastchina1.126.net";
	private String charset = "utf-8";

	private int connectionTimeout = 10 * 1000;
	private int soTimeout = 30 * 1000;
	private int lbsConnectionTimeout = 10 * 1000;
	private int lbsSoTimeout = 10 * 1000;
	private int chunkSize = 1024 * 32;
	private int chunkRetryCount = 2;
	private int queryRetryCount = 2;
	private long refreshInterval = DateUtils.HOUR_IN_MILLIS * 2;
	private long monitorInterval = DateUtils.SECOND_IN_MILLIS * 120;
	private boolean isPipelineEnabled = true; // pipeline is enabled default
	private long pipelineFailoverPeriod = 300 * 1000;
	private int md5FileMaxSize = 1024 * 1024;
	private HttpClient httpClient = null;
	private boolean monitorThreadEnable = false;
	
	public String getLbsHost() {
		return lbsHost;
	}

	public void setLbsHost(String lbsHost) {
		this.lbsHost = lbsHost;
	}

	public String getLbsIP() {
		return lbsIP;
	}

	public void setLbsIP(String lbsIP) throws InvalidParameterException {
		if (!Util.isValidLbsIP(lbsIP)) {
			throw new InvalidParameterException("Invalid LbsIP");
		}
		this.lbsIP = lbsIP;
	}
	
	public String getMonitorHost() {
		return monitorHost;
	}
	
	public void setMontiroHost(String monitorHost) {
		this.monitorHost = monitorHost;
	}
	
	public String getCharset() {
		return charset;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout)
			throws InvalidParameterException {
		if (connectionTimeout <= 0) {
			throw new InvalidParameterException("Invalid ConnectionTimeout:"
					+ connectionTimeout);
		}
		this.connectionTimeout = connectionTimeout;
	}

	public int getSoTimeout() {
		return soTimeout;
	}

	public void setSoTimeout(int soTimeout) throws InvalidParameterException {
		if (soTimeout <= 0) {
			throw new InvalidParameterException("Invalid soTimeout:" + soTimeout);
		}
		this.soTimeout = soTimeout;
	}

	public int getLbsConnectionTimeout() {
		return lbsConnectionTimeout;
	}

	public void setLbsConnectionTimeout(int connectionTimeout)
			throws InvalidParameterException {
		if (connectionTimeout <= 0) {
			throw new InvalidParameterException("Invalid lbsConnectionTimeout:"
					+ connectionTimeout);
		}
		this.lbsConnectionTimeout = connectionTimeout;
	}

	public int getLbsSoTimeout() {
		return lbsSoTimeout;
	}

	public void setLbsSoTimeout(int soTimeout) throws InvalidParameterException {
		if (soTimeout <= 0) {
			throw new InvalidParameterException("Invalid lbsSoTimeout:" + soTimeout);
		}
		this.lbsSoTimeout = soTimeout;
	}

	public int getChunkSize() {
		return chunkSize;
	}

	public void setChunkSize(int chunkSize) throws InvalidChunkSizeException {
		if (chunkSize > Constants.MAX_CHUNK_SIZE
				|| chunkSize < Constants.MIN_CHUNK_SIZE) {
			throw new InvalidChunkSizeException();
		}
		this.chunkSize = chunkSize;
	}

	public int getChunkRetryCount() {
		return chunkRetryCount;
	}

	public void setChunkRetryCount(int chunkRetryCount)
			throws InvalidParameterException {
		if (chunkRetryCount <= 0) {
			throw new InvalidParameterException("Invalid chunkRetryCount:"
					+ chunkRetryCount);
		}
		this.chunkRetryCount = chunkRetryCount;
	}

	public int getQueryRetryCount() {
		return queryRetryCount;
	}

	public void setQueryRetryCount(int queryRetryCount)
			throws InvalidParameterException {
		if (queryRetryCount <= 0) {
			throw new InvalidParameterException("Invalid queryRetryCount:"
					+ queryRetryCount);
		}
		this.queryRetryCount = queryRetryCount;
	}
	
	public long getRefreshInterval() {
		return refreshInterval;
	}

	public void setRefreshInterval(long refreshInterval) {
		if (refreshInterval < 60 * 1000) {
			LogUtil.w(LOGTAG, "Invalid refreshInterval:" + refreshInterval);
			return;
		}
		
		this.refreshInterval = refreshInterval;
	}

	public long getMonitorInterval() {
		return monitorInterval;
	}

	public void setMonitorInterval(long monitorInterval) {
		if (monitorInterval < 60 * 1000) {
			LogUtil.w(LOGTAG, "Invalid monitorInterval:" + monitorInterval);
			return;
		}
		
		this.monitorInterval = monitorInterval;
	}

	public boolean isPipelineEnabled() {
		return isPipelineEnabled;
	}

	public void setPipelineEnabled(boolean enable) {
		this.isPipelineEnabled = enable;
	}
	
	public void setPipelineFailoverPeriod(long period) {
		if (period < 0) {
			LogUtil.w(LOGTAG, "Invalid pipelineFailoverPeriod:" + period);
			return;
		}
		this.pipelineFailoverPeriod = period;
	}
	
	public long getPipelineFailoverPeriod() {
		return pipelineFailoverPeriod;
	}
	
	public int getMd5FileMaxSize() {
		return md5FileMaxSize;
	}

	public void setMd5FileMaxSize(int md5FileMaxSize)
			throws InvalidParameterException {
		if (md5FileMaxSize < 0) {
			throw new InvalidParameterException("Invalid md5FileMaxSize:"
					+ md5FileMaxSize);
		}
		this.md5FileMaxSize = md5FileMaxSize;
	}

	// set httpClient API
	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public HttpClient getHttpClient() {
		return httpClient;
	}

	// set if to use thread API
	public void setMonitorThread(boolean enable) {
		this.monitorThreadEnable = enable;
	}

	public boolean isMonitorThreadEnabled() {
		return monitorThreadEnable;
	}

}
