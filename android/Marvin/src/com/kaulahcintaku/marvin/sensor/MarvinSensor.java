package com.kaulahcintaku.marvin.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;

public class MarvinSensor implements SensorEventListener{
	
	private MarvinSensorUser user;
	private Handler handler;
	private SensorManager manager;
	
	private Sensor compassSensor;
	private int compassAccuracy;

	public MarvinSensor(Context context, MarvinSensorUser user) {
		this.user = user;
		handler = new Handler(context.getMainLooper());
		manager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		compassSensor = manager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		manager.registerListener(this, compassSensor, SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		if(sensor == compassSensor){
			compassAccuracy = accuracy;
		}
	}

	@Override
	public void onSensorChanged(final SensorEvent event) {
		Runnable runnable = null;
		if(event.sensor == compassSensor){
			runnable = new Runnable() {
				public void run() {
					user.onCompassChanged(event.values[0], compassAccuracy);
				}
			};
		}
		if(runnable != null)
			handler.post(runnable);
	}
	
	public void shutdown(){
		manager.unregisterListener(this);
	}

}
