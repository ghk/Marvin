package com.kaulahcintaku.marvin;

import java.io.ByteArrayOutputStream;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;

public class CameraPreviewHolder implements PreviewCallback{
	
	private byte[] currentBuffer = null;
	

	public CameraPreviewHolder(int width, int height) {
	}

	@Override
	public void onPreviewFrame(byte[] frame, Camera arg1) {
		YuvImage yuvimage = new YuvImage(frame,ImageFormat.NV21,arg1.getParameters().getPreviewSize().width,arg1.getParameters().getPreviewSize().height,null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		yuvimage.compressToJpeg(new Rect(0,0,arg1.getParameters().getPreviewSize().width,arg1.getParameters().getPreviewSize().height), 80, baos);
		currentBuffer = baos.toByteArray();
	}
	
	public byte[] getCurrentBuffer() {
		return currentBuffer;
	}

}
