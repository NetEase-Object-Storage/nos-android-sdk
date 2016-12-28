package com.netease.cloud.nos.android.utils;

import android.util.Log;

/**
 * Log utility class
 * 
 * @author py(hzpengyang@corp.netease.com)
 * 
 */
public class LogUtil {

	public static String makeLogTag(@SuppressWarnings("rawtypes") Class cls) {
		return "NetEaseNosService_" + cls.getSimpleName();
	}

	public static final int VERBOSE = Log.VERBOSE;
	public static final int DEBUG = Log.DEBUG;
	public static final int INFO = Log.INFO;
	public static final int WARN = Log.WARN;
	public static final int ERROR = Log.ERROR;
	public static final int ASSERT = Log.ASSERT;

	private static int level = VERBOSE;

	public static int getLevel() {
		return level;
	}

	public static void setLevel(int level) {
		LogUtil.level = level;
	}

	private LogUtil() {
	}

	/**
	 * Send a VERBOSE log message and log the exception.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public static int v(String tag, String msg) {
		if (VERBOSE >= level) {
			return Log.v(tag, msg);
		}
		return 0;
	}

	/**
	 * Send a VERBOSE log message and log the exception.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 */
	public static int v(String tag, String msg, Throwable tr) {
		if (VERBOSE >= level) {
			return Log.v(tag, msg, tr);
		}
		return 0;
	}

	/**
	 * Send a DEBUG log message and log the exception.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public static int d(String tag, String msg) {
		if (DEBUG >= level) {
			return Log.d(tag, msg);
		}
		return 0;
	}

	/**
	 * Send a DEBUG log message and log the exception.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 */
	public static int d(String tag, String msg, Throwable tr) {
		if (DEBUG >= level) {
			return Log.d(tag, msg, tr);
		}
		return 0;
	}

	/**
	 * Send a INFO log message and log the exception.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public static int i(String tag, String msg) {
		if (INFO >= level) {
			return Log.i(tag, msg);
		}
		return 0;
	}

	/**
	 * Send a INFO log message and log the exception.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 */
	public static int i(String tag, String msg, Throwable tr) {
		if (INFO >= level) {
			return Log.i(tag, msg, tr);
		}
		return 0;
	}

	/**
	 * Send an ERROR log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public static int e(String tag, String msg) {
		if (ERROR >= level) {
			return Log.e(tag, msg);
		}
		return 0;
	}

	/**
	 * Send a ERROR log message and log the exception.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 */
	public static int e(String tag, String msg, Throwable tr) {
		if (ERROR >= level) {
			return Log.e(tag, msg, tr);
		}
		return 0;
	}

	/**
	 * Send a WARN log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public static int w(String tag, String msg) {
		if (WARN >= level) {
			return Log.w(tag, msg);
		}
		return 0;
	}

	public static int w(String tag, Throwable tr) {
		if (WARN >= level) {
			return Log.w(tag, tr);
		}
		return 0;
	}

	/**
	 * Send a WARN log message and log the exception.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 */
	public static int w(String tag, String msg, Throwable tr) {
		if (WARN >= level) {
			return Log.w(tag, msg, tr);
		}
		return 0;
	}

}
