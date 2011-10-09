package com.kaulahcintaku.marvin.task;

import com.kaulahcintaku.marvin.MarvinActivity;

import android.util.Log;

public class MarvinTaskThread extends Thread{
	private MarvinTaskLoop loop;
	private boolean isStopped = false;
	
	public MarvinTaskThread(MarvinTaskLoop loop) {
		super();
		this.loop = loop;
	}

	@Override
	public void run() {
		while(!isStopped){
			try{
				loop.loop();
			}
			catch(Exception e){
				Log.e(MarvinActivity.TAG, e.getMessage(), e);
			}
		}
	}
	
	public void stopLoop(){
		isStopped = true;
	}
}
