package com.kaulahcintaku.marvin.body;

public interface MarvinBodyUser {
	void onBodyConnected();
	void onBodyDisconnected();
	void onBodyConnectFailed(String error);
	void onBodyPingSucceeded();
}
