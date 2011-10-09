package com.kaulahcintaku.marvin.body.command;

public class MarvinConnectionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MarvinConnectionException() {
	}

	public MarvinConnectionException(String detailMessage) {
		super(detailMessage);
	}

	public MarvinConnectionException(Throwable throwable) {
		super(throwable);
	}

	public MarvinConnectionException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
