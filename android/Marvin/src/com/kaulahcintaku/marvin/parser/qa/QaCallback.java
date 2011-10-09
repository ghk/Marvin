package com.kaulahcintaku.marvin.parser.qa;

public interface QaCallback {
	boolean isCancelled();
	void onTranslateForwardComplete(String result);
	void onAskWolframComplete(String result);
}
