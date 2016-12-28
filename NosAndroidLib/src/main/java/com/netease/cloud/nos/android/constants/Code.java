package com.netease.cloud.nos.android.constants;

public class Code {
	public final static int HTTP_SUCCESS = 200;
	public final static int BAD_REQUEST  = 400;
	public final static int INVALID_TOKEN = 403;
	public final static int CACHE_EXPIRED = 404;
	public final static int LBS_ERROR = BAD_REQUEST;

	public final static int SERVER_ERROR = 500;
	public final static int CALLBACK_ERROR = 520;
	public final static int UPLOADING_CANCEL = 600;
	public final static int INVALID_OFFSET = 699;
	public final static int INVALID_LBS_DATA = 700;
	public final static int INVALID_RESPONSE_DATA = 701;
	
	public final static int HTTP_EXCEPTION = 799;
	public final static int HTTP_NO_RESPONSE = 899;
	public final static int CONNECTION_TIMEOUT = 900;
	public final static int CONNECTION_REFUSED = 901;
	public final static int CONNECTION_RESET = 902;
	public final static int SOCKET_TIMEOUT = 903;
	public final static int SSL_FAILED	= 904;
	
	public final static int UNKNOWN_REASON = 999;
	
	public final static int MONITOR_SUCCESS = 0;
	public final static int MONITOR_FAIL = 1;
	public final static int MONITOR_CANCELED = 2;

	public static boolean isOK(int code) {
		if (code == HTTP_SUCCESS) {
			return true;
		} else {
			return false;
		}
	}

	public static String getDes(int code) {
		String str = "could not upload file with unknown reason, please contact with us";
		switch (code) {
		case HTTP_SUCCESS:
			str = "file upload success";
			break;
		case BAD_REQUEST:
			str = "bad request, please confirm the sdk usage";
			break;
		case INVALID_TOKEN:
			str = "could not upload file with invalid token, please change your token before uploading";
			break;
		case SERVER_ERROR:
			str = "could not upload file with server inner error, please contact with us";
			break;
		case HTTP_EXCEPTION:
			str = "could not upload file with http exception, please wait for network recover";
			break;
		case HTTP_NO_RESPONSE:
			str = "could not upload file with no http response, please contact with us";
			break;
		case CALLBACK_ERROR:
			str = "could not upload file with callback error.";
			break;
		case INVALID_OFFSET:
			str = "could not upload file with invalid break point offset.";
			break;
		case UNKNOWN_REASON:
			str = "could not upload file with unknown reason, please contact with us";
			break;
		}
		return str;
	}
}
