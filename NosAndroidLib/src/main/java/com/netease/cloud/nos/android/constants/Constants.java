package com.netease.cloud.nos.android.constants;

public class Constants {

	public final static String LBS_KEY = "netease_pomelo_nos_lbs";
	public final static String UPLOAD_SERVER_KEY = "netease_pomelo_nos_server";
	public final static String HTTPS_UPLOAD_SERVER_KEY = "netease_pomelo_nos_https_server";
	public final static String LBS_STATUS = "netease_pomelo_nos_lbs_status";
	public final static String LBS_TIME = "netease_pomelo_nos_lbs_time";
	public final static String BUCKET_NUMBER = "netease_pomelo_bucket_number";	
	public final static String BUCKET_NAME = "netease_pomelo_bucket_name";	
	public final static String NET_TYPE = "netease_pomelo_nos_net_type";

	public final static String HEADER_TOKEN = "x-nos-token";

	public final static String MONITOR_SERVICE_ACTION = "com.netease.pomelo.nos.monitor";

	public final static int MAX_CHUNK_SIZE = 4 * 1024 * 1024;
	public final static int MIN_CHUNK_SIZE = 4 * 1024;

	public final static int CODE_RETRY = -1;
	public final static int CODE_INVALID_TOKEN = -2;
	public final static int CODE_SERVER_ERROR = -3;
	public final static int CODE_HTTP_EXCEPTION = -4;
	public final static int CODE_NO_RESPONSE = -5;

	public final static String UPLOAD_VERSION = "1.0";
	public final static String LBS_VERSION = "1.0";
	public final static String SDK_VERSION = "1.0.0";

	public final static String TEMP_FILE = "/nos_tmp";

	public final static int UPLOAD_TYPE_NORMAL   = 0;
	public final static int UPLOAD_TYPE_PIPELINE = 1;
	public final static int UPLOAD_TYPE_MIXED    = 2;
	public final static int UPLOAD_TYPE_UNKNOWN  = 1000; 

}
