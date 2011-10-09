package com.kaulahcintaku.marvin.ear;

import java.util.List;

public interface MarvinEarUser {
	void onEarListening();
	void onEarStopListening();
	int onEarListeningError(int errorCode, String error);
	int onEarHear(List<String> hearingResults);
}