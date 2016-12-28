package com.netease.cloud.nos.android.exception;

public class InvalidChunkSizeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9081338843636519886L;

	public InvalidChunkSizeException() {
	}

	/**
	 * @param message
	 */
	public InvalidChunkSizeException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public InvalidChunkSizeException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidChunkSizeException(String message, Throwable cause) {
		super(message, cause);
	}

}
