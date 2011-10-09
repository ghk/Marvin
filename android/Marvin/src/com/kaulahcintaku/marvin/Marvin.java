package com.kaulahcintaku.marvin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Handler;
import android.util.Log;

import com.kaulahcintaku.marvin.body.MarvinBody;
import com.kaulahcintaku.marvin.body.command.MarvinCommand;
import com.kaulahcintaku.marvin.ear.MarvinEar;
import com.kaulahcintaku.marvin.ear.MarvinEarUser;
import com.kaulahcintaku.marvin.mouth.MarvinMouth;
import com.kaulahcintaku.marvin.sensor.MarvinSensorUser;
import com.kaulahcintaku.marvin.task.ListenException;
import com.kaulahcintaku.marvin.task.MarvinTask;
import com.kaulahcintaku.marvin.task.MarvinTaskContext;
import com.kaulahcintaku.marvin.task.MarvinTaskLoop;
import com.kaulahcintaku.marvin.task.MarvinTaskThread;
import com.kaulahcintaku.marvin.task.WaitForCommandTask;

public class Marvin implements MarvinTaskContext, Runnable, MarvinEarUser, MarvinSensorUser{
	
	private boolean isRunning;
	
	private MarvinActivity marvinActivity;
	private MarvinMouth mouth;
	private MarvinBody body;
	private MarvinEar ear;
	
	private MarvinTask defaultTask = new WaitForCommandTask();
	private MarvinTask currentTask;
	private MarvinTask nextTask;
	
	private boolean currentTaskCancelled;
	private List<MarvinTaskThread> currentTaskThreads = new ArrayList<MarvinTaskThread>();
	private Map<String, Object> configurations = new HashMap<String, Object>();
	
	private Handler handler;
	
	private float compassValue;
	private int compassAccuracy;
	private Object compassValueNotifier = new Object();
	
	private List<String> listenResults;
	private String listenError;
	private boolean isListening;
	private boolean listeningFinished;
	private boolean listeningCancelled;
	private Object listenNotifier = new Object();
	
	private int[] logs = new int[]{R.id.log1, R.id.log2, R.id.log3, R.id.log4, R.id.log5, R.id.log6};
	
	
	public Marvin(MarvinActivity marvinActivity, MarvinMouth mouth, MarvinBody body, MarvinEar ear) {
		this.marvinActivity = marvinActivity;
		this.mouth = mouth;
		this.body = body;
		this.ear = ear;
		handler = new Handler(marvinActivity.getMainLooper());
		new Thread(this).start();
	}

	@Override
	public void speak(final String text) {
		handler.post(new Runnable() {
			public void run() {
				mouth.speak(text);
			}
		});
	}

	@Override
	public float getDirection() {
		return compassValue;
	}


	@Override
	public void setConfiguration(String key, Object value) {
		configurations.put(key, value);
	}

	@Override
	public Object getConfiguration(String key) {
		return configurations.get(key);
	}
	
	@Override
	public void finish() {
		finish(null);
	}

	@Override
	public void finish(MarvinTask nextTask) {
		if(nextTask == null)
			nextTask = defaultTask;
		
		this.nextTask = nextTask;
		killCurrentTask();
	}
	
	public void killCurrentTask(){
		currentTaskCancelled = true;
		for(MarvinTaskThread thread: currentTaskThreads){
			thread.stopLoop();
			thread.interrupt();
		}
	}

	@Override
	public void run() {
		isRunning = true;
		nextTask = defaultTask;
		while(isRunning){
			currentTask = nextTask;
			log(R.id.taskLog, currentTask.getName());
			for(int logId: logs){
				log(logId, null);
			}
			currentTaskCancelled = false;
			currentTask.setContext(this);
			currentTask.onStart();
			
			List<MarvinTaskLoop> loops = currentTask.getLoops();
			currentTaskThreads = new ArrayList<MarvinTaskThread>();
			for(MarvinTaskLoop loop: loops){
				currentTaskThreads.add(new MarvinTaskThread(loop));
			}
			for(MarvinTaskThread thread: currentTaskThreads){
				thread.start();
			}
			for(MarvinTaskThread thread: currentTaskThreads){
				while(thread.isAlive() && isRunning){
					try{
						thread.join();
					}
					catch(InterruptedException ie){
					}
				}
			}
			currentTask.onStop();
		}
	}
	
	
	public void shutdown(){
		isRunning = false;
		killCurrentTask();
	}

	@Override
	public boolean isLoopCanceled() {
		return currentTaskCancelled;
	}
	
	@Override
	public float waitForDirectionChanged() throws InterruptedException{
		synchronized (compassValueNotifier) {
			float currentValue = compassValue;
			while(compassValue != currentValue){
				compassValueNotifier.wait();
			}
			return compassValue;
		}
		
	}


	@Override
	public void onCompassChanged(float value, int accuracy) {
		if(accuracy != compassAccuracy){
			Log.d(MarvinActivity.TAG, "new compass accuracy: "+accuracy);
		}
		synchronized (compassValueNotifier) {
			compassValue = value;
			compassAccuracy = accuracy;
			compassValueNotifier.notifyAll();
		}
	}
	
	@Override
	public void log(final int tvId, final String text) {
		handler.post(new Runnable() {
			public void run() {
				marvinActivity.log(tvId, text);
			}
		});
	}
	
	@Override
	public void runBodyCommands(List<MarvinCommand> commands){
		body.runCommands(commands);
	}
	
	@Override
	public List<String> listenForVoice() throws InterruptedException,
			ListenException {
		synchronized(listenNotifier){
			if(isListening)
				throw new ListenException("already listening");
			isListening = true;
			listeningFinished = false;
			listeningCancelled = false;
			try{
				listenResults = null;
				ear.listen(0);
				while(!listeningFinished && !listeningCancelled){
					listenNotifier.wait();
				}
				if(listeningCancelled)
					throw new ListenException("listening cancelled");
					
				if(listenResults == null)
					throw new ListenException(listenError);
				return listenResults;
			}
			finally{
				isListening = false;
			}
		}
	}

	@Override
	public void cancelListening() {
		synchronized (listenNotifier){
			if(!isListening)
				return;
			
			listeningCancelled = true;
			listenNotifier.notifyAll();
		}
	}

	@Override
	public void onEarListening() {
	}

	@Override
	public void onEarStopListening() {
	}

	@Override
	public int onEarListeningError(int errorCode, String error) {
		synchronized (listenNotifier){
			listenError = error;
			listeningFinished = true;
			listenNotifier.notifyAll();
		}
		return 0;
	}

	@Override
	public int onEarHear(List<String> hearingResults) {
		synchronized (listenNotifier){
			listenResults = hearingResults;
			listeningFinished = true;
			listenNotifier.notifyAll();
		}
		return 0;
	}

}
