package com.netease.cloud.nos.android.monitor;

import com.netease.cloud.nos.android.constants.Constants;
import android.os.Parcel;
import android.os.Parcelable;

public class StatisticItem implements Parcelable {

	private /* final */ String platform = "android";
	private String clientIP;
	private /* final */ String sdkVersion = "1.0";
	private String lbsIP;
	private String uploaderIP;
	private long fileSize;
	private String netEnv;
	private long lbsUseTime;
	private long uploaderUseTime;
	private int lbsSucc = 0;
	private int uploaderSucc = 0;
	private int lbsHttpCode = 200;
	private int uploaderHttpCode = 200;
	private int chunkRetryCount = 0;
	private int queryRetryCount = 0;
	private int uploadRetryCount = 0;
	private String bucketName;
	private int uploadType = Constants.UPLOAD_TYPE_UNKNOWN;	

    public StatisticItem() {
        super();
    }

    public StatisticItem(
    		final String platform,
    		String clientIP,
    		final String sdkVersion,
    		String lbsIP,
    		String uploaderIP,
    		long fileSize,
    		String netEnv,
    		long lbsUseTime,
    		long uploaderUseTime,
    		int lbsSucc,
    		int uploaderSucc,
    		int lbsHttpCode,
    		int uploaderHttpCode,
    		int chunkRetryCount,
    		int queryRetryCount,
    		int uploadRetryCount,
    		String bucketName,
    		int uploadType) {
        super();

    	this.platform = platform;
    	this.clientIP = clientIP;
    	this.sdkVersion = sdkVersion;
    	this.lbsIP = lbsIP;
    	this.uploaderIP = uploaderIP;
    	this.fileSize = fileSize;
    	this.netEnv = netEnv;
    	this.lbsUseTime = lbsUseTime;
    	this.uploaderUseTime = uploaderUseTime;
    	this.lbsSucc = lbsSucc;
    	this.uploaderSucc = uploaderSucc;
    	this.lbsHttpCode = lbsHttpCode;
    	this.uploaderHttpCode = uploaderHttpCode;
    	this.chunkRetryCount = chunkRetryCount;
    	this.queryRetryCount = queryRetryCount;
    	this.uploadRetryCount = uploadRetryCount;
    	this.bucketName = bucketName;
    	this.uploadType = uploadType;	
    
    }

	
	public String getClientIP() {
		return clientIP;
	}

	public void setClientIP(String clientIP) {
		this.clientIP = clientIP;
	}

	public String getLbsIP() {
		return lbsIP;
	}

	public void setLbsIP(String lbsIP) {
		this.lbsIP = lbsIP;
	}

	public String getUploaderIP() {
		return uploaderIP;
	}

	public void setUploaderIP(String uploaderIP) {
		this.uploaderIP = uploaderIP;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getNetEnv() {
		return netEnv;
	}

	public void setNetEnv(String netEnv) {
		this.netEnv = netEnv;
	}

	public long getLbsUseTime() {
		return lbsUseTime;
	}

	public void setLbsUseTime(long lbsUseTime) {
		this.lbsUseTime = lbsUseTime;
	}

	public long getUploaderUseTime() {
		return uploaderUseTime;
	}

	public void setUploaderUseTime(long uploaderUseTime) {
		this.uploaderUseTime = uploaderUseTime;
	}

	public int getLbsSucc() {
		return lbsSucc;
	}

	public void setLbsSucc(int lbsSucc) {
		this.lbsSucc = lbsSucc;
	}

	public int getUploaderSucc() {
		return uploaderSucc;
	}

	public void setUploaderSucc(int uploaderSucc) {
		this.uploaderSucc = uploaderSucc;
	}

	public int getLbsHttpCode() {
		return lbsHttpCode;
	}

	public void setLbsHttpCode(int lbsHttpCode) {
		this.lbsHttpCode = lbsHttpCode;
	}

	public int getUploaderHttpCode() {
		return uploaderHttpCode;
	}

	public void setUploaderHttpCode(int uploaderHttpCode) {
		this.uploaderHttpCode = uploaderHttpCode;
	}

	public int getChunkRetryCount() {
		return chunkRetryCount;
	}

	public void setChunkRetryCount(int chunkRetryCount) {
		this.chunkRetryCount = chunkRetryCount;
	}

	public String getPlatform() {
		return platform;
	}

	public String getSdkVersion() {
		return sdkVersion;
	}

	public int getQueryRetryCount() {
		return queryRetryCount;
	}

	public void setQueryRetryCount(int queryRetryCount) {
		this.queryRetryCount = queryRetryCount;
	}

	public int getUploadRetryCount() {
		return uploadRetryCount;
	}

	public void setUploadRetryCount(int uploadRetryCount) {
		this.uploadRetryCount = uploadRetryCount;
	}

	public String getBucketName() {
		return bucketName;
	}
	
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	public int getUploadType() {
		return uploadType;
	}

	public void setUploadType(int uploadType) {
		this.uploadType = uploadType;
	}

	
    @Override
    public int describeContents() {
        return 0;
    }
	

    @Override
    public void writeToParcel(Parcel dest, int flags) {
		// Statistic data is parceled
        dest.writeString(platform);
        dest.writeString(clientIP);
        dest.writeString(sdkVersion);
        dest.writeString(lbsIP);
        dest.writeString(uploaderIP);
    	dest.writeLong(fileSize);
        dest.writeString(netEnv);
    	dest.writeLong(lbsUseTime);
    	dest.writeLong(uploaderUseTime);
    	dest.writeInt(lbsSucc);
    	dest.writeInt(uploaderSucc);
    	dest.writeInt(lbsHttpCode);
    	dest.writeInt(uploaderHttpCode);
    	dest.writeInt(chunkRetryCount);
    	dest.writeInt(queryRetryCount);
    	dest.writeInt(uploadRetryCount);
    	dest.writeString(bucketName);
    	dest.writeInt(uploadType);
    }

    public static final Parcelable.Creator<StatisticItem> CREATOR = new Creator<StatisticItem>() {

        @Override
        public StatisticItem[] newArray(int size) {
            return new StatisticItem[size];
        }

        @Override
        public StatisticItem createFromParcel(Parcel source) {
			// Statistic data is from parcel
			return new StatisticItem(
					source.readString(),
					source.readString(),
					source.readString(),
					source.readString(),
					source.readString(),
					source.readLong(),
					source.readString(),
					source.readLong(),
					source.readLong(),
					source.readInt(),
					source.readInt(),
					source.readInt(),
					source.readInt(),
					source.readInt(),
					source.readInt(),
					source.readInt(),
					source.readString(),
					source.readInt());
        }
    };    
    
}
