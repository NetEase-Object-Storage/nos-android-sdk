package com.netease.cloud.nos.android.monitor;

import com.netease.cloud.nos.android.exception.InvalidParameterException;
import com.netease.cloud.nos.android.utils.LogUtil;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;

public class MonitorConfig implements Parcelable {
	private static final String LOGTAG = LogUtil.makeLogTag(MonitorConfig.class);

	private String monitorHost = "http://lbs.eastchina1.126.net";
	private int connectionTimeout = 10 * 1000;
	private int soTimeout = 30 * 1000;
	private long monitorInterval = DateUtils.SECOND_IN_MILLIS * 120;

    public MonitorConfig() {
        super();
    }

    public MonitorConfig (
    		String monitorHost,
    		int connectionTimeout,
    		int soTimeout,
    		long monitorInterval) {
        super();

    	this.monitorHost = monitorHost;
    	this.connectionTimeout = connectionTimeout;
    	this.soTimeout = soTimeout;
    	this.monitorInterval = monitorInterval;
    }

	public String getMonitorHost() {
		return monitorHost;
	}
	
	public void setMontiroHost(String monitorHost) {
		this.monitorHost = monitorHost;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
		// Monitor config is parceled
		dest.writeString(monitorHost);
		dest.writeInt(connectionTimeout);
		dest.writeInt(soTimeout);
		dest.writeLong(monitorInterval);
    }

    public static final Parcelable.Creator<MonitorConfig> CREATOR = new Creator<MonitorConfig>() {

        @Override
        public MonitorConfig[] newArray(int size) {
            return new MonitorConfig[size];
        }

        @Override
        public MonitorConfig createFromParcel(Parcel source) {
			// Statistic data is from parcel
			return new MonitorConfig(
					source.readString(),
					source.readInt(),
					source.readInt(),
					source.readLong());
        }
    };    

}
