package com.netease.cloud.nos.android.exception;

public class InvalidParameterException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5066837976183983722L;

	public InvalidParameterException() {
	}

	/**
	 * @param message
	 */
	public InvalidParameterException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public InvalidParameterException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidParameterException(String message, Throwable cause) {
		super(message, cause);
	}
}
