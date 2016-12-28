package com.netease.cloud.nos.android.core;

import java.util.Map;

public class WanNOSObject {

	private String uploadToken;
	private String nosBucketName;
	private String nosObjectName;
	private String contentMD5;
	private String contentType;
	private Map<String, String> userMetadata;

	public WanNOSObject() {

	}

	public WanNOSObject(String uploadToken, String nosBucketName,
			String nosObjectName, String contentMD5,
			Map<String, String> userMetadata) {
		this.uploadToken = uploadToken;
		this.nosBucketName = nosBucketName;
		this.nosObjectName = nosObjectName;
		this.contentMD5 = contentMD5;
		this.userMetadata = userMetadata;
	}

	public String getContentMD5() {
		return contentMD5;
	}

	public void setContentMD5(String contentMD5) {
		this.contentMD5 = contentMD5;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Map<String, String> getUserMetadata() {
		return userMetadata;
	}

	public void setUserMetadata(Map<String, String> userMetadata) {
		this.userMetadata = userMetadata;
	}

	public String getUploadToken() {
		return uploadToken;
	}

	public void setUploadToken(String uploadToken) {
		this.uploadToken = uploadToken;
	}

	public String getNosBucketName() {
		return nosBucketName;
	}

	public void setNosBucketName(String nosBucketName) {
		this.nosBucketName = nosBucketName;
	}

	public String getNosObjectName() {
		return nosObjectName;
	}

	public void setNosObjectName(String nosObjectName) {
		this.nosObjectName = nosObjectName;
	}
}
