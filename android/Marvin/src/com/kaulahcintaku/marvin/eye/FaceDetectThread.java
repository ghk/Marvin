/*
 * Copyright (C) 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.kaulahcintaku.marvin.eye;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.os.Handler;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * A worker thread which will detect faces and notify any callbacks if a face is
 * detected.
 *
 * @author raymes@google.com (Raymes Khoury)
 */
public class FaceDetectThread extends Thread implements PreviewCallback {
	
    private class Image {
        byte[] mData;

        int mHeight;

        int mWidth;

        int mFormat;

        boolean mReversed;

        public Image(byte[] data, int width, int height, int format, boolean reversed) {
            mData = data;
            mWidth = width;
            mHeight = height;
            mFormat = format;
            mReversed = reversed;
        }
    }
    
    private byte[] callbackBuffer = new byte[460800];

    private ArrayBlockingQueue<Image> mImages;

    private PointF mImageSize;

    private Face[] mFaces;

    private MarvinEyeUser mUser;
    private Handler mHandler;
    private Camera.Parameters mParams;

    private FaceDetector mFaceDetector;

    private boolean mProcessing;
    
    private boolean mRunning = true;

    public FaceDetectThread(Context context, MarvinEyeUser user, Camera.Parameters params) {
    	mParams = params;
    	mHandler = new Handler(context.getMainLooper());
    	mUser = user;
        mImages = new ArrayBlockingQueue<Image>(1);
        mImageSize = new PointF();
        mFaces = new Face[1];
        mProcessing = false;
    }


    public void addFace(byte[] data, int width, int height, int format, boolean reversed) {
        boolean processing = false;
        synchronized (this) {
            processing = mProcessing;
        }
        if (mImages.isEmpty() && !processing) {
            Image newImage = new Image(data.clone(), width, height, format, reversed);
            mImages.add(newImage);
        }
    }

    @Override
    public void run() {
        while (mRunning) {
            Image current = null;
            try {
                current = mImages.take();
                synchronized (this) {
                    mProcessing = true;
                }
            } catch (InterruptedException e) {
                continue;
            }
            final Face f = findFace(current.mData, current.mWidth, current.mHeight, current.mFormat);
            if (f != null) {
                final PointF point = new PointF();
                f.getMidPoint(point);
                if (current.mReversed) {
                    point.x = mImageSize.x - point.x;
                }
                mHandler.post(new Runnable() {
					public void run() {
	                    mUser.faceDetected(point, f.eyesDistance(), f.confidence(), mImageSize);
					}
				});
            } else {
                mHandler.post(new Runnable() {
					public void run() {
	                    mUser.faceNotDetected();
					}
				});
            }
            synchronized (this) {
                mProcessing = false;
            }

        }

    }

    public Face findFace(byte[] data, int width, int height, int format) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        YuvImage yuvImage = new YuvImage(data, format, width, height, null);
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 50, out);
        byte[] imageBytes = out.toByteArray();
        Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        float factor = (float) 0.8;
        image = Bitmap.createScaledBitmap(
                image, (int) (width * factor), (int) (height * factor), true);
        mImageSize.set(image.getWidth(), image.getHeight());

        if (mFaceDetector == null)
            mFaceDetector = new FaceDetector(image.getWidth(), image.getHeight(), 1);
        int faceCount = mFaceDetector.findFaces(image, mFaces);
        if (faceCount > 0) {
            return mFaces[0];
        } else {
            return null;
        }
    }

    public void imageReady(byte[] data, int width, int height, int format, boolean reversed) {
        addFace(data, width, height, format, reversed);
    }

	@Override
	public void onPreviewFrame(byte[] imageData, Camera camera) {
		Camera.Parameters params = mParams;
		imageReady(imageData, params.getPreviewSize().width,
                params.getPreviewSize().height, params.getPreviewFormat(), false);
		camera.addCallbackBuffer(callbackBuffer);
	}

	public void shutdown(){
		mRunning = false;
		this.interrupt();
	}
}