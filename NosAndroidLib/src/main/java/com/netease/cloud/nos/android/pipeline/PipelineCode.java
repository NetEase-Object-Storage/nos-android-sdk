package com.netease.cloud.nos.android.pipeline;

public class PipelineCode {
	public final static int SUCCESS = 0;
	public final static int CHANNEL_INACTIVE     = 1;
	public final static int CHANNEL_EXCEPTION    = 2;

	public final static int NO_BREAK_RESP        = 3;
	public final static int FAILED_BREAK_RESP    = 4;
	public final static int INVALID_BREAK_OFFSET = 5;

	public final static int NO_UPLOAD_RESP       = 6;
	public final static int FAILED_UPLOAD_RESP   = 7;
	public final static int INVALID_UPLOAD_RESP  = 8;
	public final static int INVALID_UPLOAD_OFFSET = 9;

	public final static int INVALID_SENDOFFSET    = 10;
	public final static int FAILED_READFILE       = 11;
	public final static int UPLOAD_CANCELLED	  = 12;
	
	public final static int BACK_OFFSET	          = 13; 
	
	public final static int UNKNOWN_REASON        = 14;
	
	public static boolean isSuccess(int code) {
		if (code == SUCCESS) {
			return true;
		} else {
			return false;
		}
	}

	public static String getDes(int code) {
		String str = "failed with unknown reason";
		switch (code) {
		case SUCCESS:
			str = "file upload success";
			break;
		case CHANNEL_INACTIVE:
			str = "channel is inactive";
			break;
		case CHANNEL_EXCEPTION:
			str = "channel exception is catched";
			break;
		case UNKNOWN_REASON:
			str = "failed with unknown reason";
			break;
		}
		return str;
	}
}
