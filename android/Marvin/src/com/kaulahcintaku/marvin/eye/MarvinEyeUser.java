package com.kaulahcintaku.marvin.eye;

import android.graphics.PointF;

public interface MarvinEyeUser {
	void onEyeStarted();
	void onEyeStopped();
	void faceDetected(
            PointF face, float eyesDistance, float confidence, PointF imageSize);
    void faceNotDetected();
	
}
