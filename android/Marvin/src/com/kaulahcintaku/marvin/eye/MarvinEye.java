package com.kaulahcintaku.marvin.eye;

import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.kaulahcintaku.marvin.MarvinActivity;

public class MarvinEye implements SurfaceHolder.Callback{
	
	private Camera camera;
	private FaceDetectThread detectThread;
	private SurfaceView cameraView;
	
	private Context context;
	private MarvinEyeUser user;
	
    public static interface ImageReadyCallback {
        public void imageReady(byte[] data, int width, int height, int format, boolean reversed);
    }
	
	public MarvinEye(SurfaceView cameraView, Context context, MarvinEyeUser user) {
		this.context = context;
		this.cameraView = cameraView;
		this.user = user;
		
		SurfaceHolder sh = cameraView.getHolder();
    	sh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		sh.addCallback(this);
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		camera = Camera.open();
        Camera.Parameters params = camera.getParameters();
        camera.setDisplayOrientation(90);
		params.setRotation(90);
		params.set("orientation", "landscape");
		params.set("rotation", 90);
		params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		Camera.Size previewSize = params.getPreviewSize();
		List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
		int i = 0;
		for (Camera.Size cs : previewSizes) {
			Log.d(MarvinActivity.TAG, "Camera - supports:(" + (i++) + ") " + cs.width + "x" + cs.height);
		}
		
		previewSize.width = 720;
		previewSize.height = 480;
			
		params.setSceneMode("portrait");
		Log.d(MarvinActivity.TAG, "w: "+previewSize.width+" h: "+previewSize.height);
		List<Integer> formats = params.getSupportedPreviewFormats();
		for (Integer format : formats) {
			Log.d(MarvinActivity.TAG, "Camera - supports preview format: "+format);
		}
		params.setPreviewSize(previewSize.width, previewSize.height);
		camera.setParameters(params);
		
		detectThread = new FaceDetectThread(context, user, params);
		detectThread.start();
		camera.setPreviewCallback(detectThread);
		try{
			camera.setPreviewDisplay(cameraView.getHolder());
			camera.startPreview();
			Log.d(MarvinActivity.TAG, "preview started");
		}catch(Exception e){
			Log.e(MarvinActivity.TAG, e.getMessage(), e);
		}
		user.onEyeStarted();
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		shutdown();
	}
	
	public void shutdown(){
		if(camera != null){
			camera.stopPreview();
			camera.release();
			detectThread.stop();
			camera = null;
		}
		user.onEyeStopped();
	}
}
