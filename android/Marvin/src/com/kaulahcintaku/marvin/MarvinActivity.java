package com.kaulahcintaku.marvin;

import java.util.List;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.kaulahcintaku.marvin.body.MarvinBody;
import com.kaulahcintaku.marvin.body.MarvinBodyUser;
import com.kaulahcintaku.marvin.body.bluetooth.BluetoothBody;
import com.kaulahcintaku.marvin.ear.MarvinEar;
import com.kaulahcintaku.marvin.ear.MarvinEarUser;
import com.kaulahcintaku.marvin.eye.MarvinEye;
import com.kaulahcintaku.marvin.eye.MarvinEyeUser;
import com.kaulahcintaku.marvin.mouth.MarvinMouth;
import com.kaulahcintaku.marvin.sensor.MarvinSensor;
import com.kaulahcintaku.marvin.sensor.MarvinSensorUser;

	
public class MarvinActivity extends Activity  implements MarvinEarUser, MarvinBodyUser, MarvinSensorUser, MarvinEyeUser{
	public static final String TAG = "marvin";
	
	private Marvin marvin;
	private MarvinMouth mouth;
	private MarvinBody body;
	private MarvinEar ear;
	private MarvinEye eye;
	private MarvinSensor sensor;
	
	private PowerManager powerManager;
	private WakeLock wakeLock;
	
	private Handler handler;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        handler = new Handler();
        ear = new MarvinEar(this, this);
        body = new BluetoothBody(this, this);
        mouth = new MarvinMouth(this);
        sensor = new MarvinSensor(this, this);
        eye = new MarvinEye((SurfaceView) findViewById(R.id.cameraview), this, this);
        
        marvin = new Marvin(this, mouth, body, ear);
        
        acquireWakeLock();
        
        findViewById(R.id.shutdownButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				finish();
			}
		});
        findViewById(R.id.killTaskButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				marvin.killCurrentTask();
			}
		});
    }
    
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	
	@Override
	protected void onDestroy() {
		body.shutdown();
		mouth.shutDown();
		ear.shutdown();
		sensor.shutdown();
		marvin.shutdown();
		if(eye != null)
			eye.shutdown();
		
		super.onDestroy();
	}
    
    /** Make sure the screen doesn't shut down while the user is working. */
    private void acquireWakeLock() {
      powerManager = (PowerManager) getSystemService(POWER_SERVICE);
      wakeLock = powerManager.newWakeLock(
          PowerManager.SCREEN_DIM_WAKE_LOCK,
          getClass().getName());
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	wakeLock.release();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	wakeLock.acquire();
    }

	
	public void log(int tvId, String text){
		((TextView)findViewById(tvId)).setText(text);
	}
	
	private void toggle(int radioId, boolean on){
		((RadioButton)findViewById(radioId)).setChecked(on);
	}
	
	@Override
	public void onEarListening() {
		toggle(R.id.earRadio, true);
		log(R.id.log2, null);
		
		marvin.onEarListening();
	}

	@Override
	public int onEarListeningError(int errorCode, String error) {
		toggle(R.id.earRadio, false);
		log(R.id.earLog, error);
		
		marvin.onEarListeningError(errorCode, error);
		return 0;
	}

	@Override
	public int onEarHear(List<String> hearingResults) {
		toggle(R.id.earRadio, false);
		log(R.id.earLog, hearingResults == null ? "NULL" : hearingResults.toString());

		
		marvin.onEarHear(hearingResults);
		return 0;
	}
	
	@Override
	public void onEarStopListening() {
		toggle(R.id.earRadio, false);
		marvin.onEarStopListening();
	}
	
	@Override
	public void onCompassChanged(float value, int accuracy) {
		log(R.id.compassTextView, value+"");
		marvin.onCompassChanged(value, accuracy);
	}


	@Override
	public void onBodyConnected() {
		log(R.id.bodyLog, null);
		toggle(R.id.bodyRadio, true);
		
		toggle(R.id.earRadio, false);
	}
	
	@Override
	public void onBodyDisconnected() {
		toggle(R.id.bodyRadio, false);
		
		toggle(R.id.earRadio, false);
	}
	

	@Override
	public void onBodyConnectFailed(String error) {
		log(R.id.bodyLog, error);
	}

	@Override
	public void onBodyPingSucceeded() {
		toggle(R.id.pingRadio, true);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				toggle(R.id.pingRadio, false);
			}
		}, 500);
	}

	@Override
	public void onEyeStarted() {
		toggle(R.id.eyeRadio, true);
	}

	@Override
	public void onEyeStopped() {
		toggle(R.id.eyeRadio, false);
	}

	@Override
	public void faceDetected(PointF face, float eyesDistance, float confidence,
			PointF imageSize) {
		log(R.id.eyeLog, face.x+" "+face.y+" "+" - "+eyesDistance+" - "+confidence);
	}

	@Override
	public void faceNotDetected() {
		log(R.id.eyeLog, "face not detected");
	}

	
}