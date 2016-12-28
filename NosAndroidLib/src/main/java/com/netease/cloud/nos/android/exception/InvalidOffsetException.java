package com.netease.cloud.nos.android.exception;

public class InvalidOffsetException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2354050554608013051L;

	public InvalidOffsetException() {
	}

	/**
	 * @param message
	 */
	public InvalidOffsetException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public InvalidOffsetException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidOffsetException(String message, Throwable cause) {
		super(message, cause);
	}
}
