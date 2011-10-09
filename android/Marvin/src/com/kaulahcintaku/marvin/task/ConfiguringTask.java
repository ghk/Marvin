package com.kaulahcintaku.marvin.task;

import java.util.ArrayList;
import java.util.List;

import com.kaulahcintaku.marvin.R;

public class ConfiguringTask extends MarvinTask{
	
	private String key;
	private Object value;
	
	public ConfiguringTask(String key, Object value) {
		name = "Configure";
		this.key = key;
		this.value = value;
	}
	
	@Override
	public List<MarvinTaskLoop> getLoops() {
		List<MarvinTaskLoop> results = new ArrayList<MarvinTaskLoop>();
		results.add(new MarvinTaskLoop() {
			public void loop() {
				configure();
			}
		});
		return results;
	}
	
	public void configure(){
		try{
			context.setConfiguration(key, value);
			context.log(R.id.log1, key+": "+value);
			context.speak("baiklah");
			Thread.sleep(2000);
		}
		catch (InterruptedException e) {
		}
		finally{
			context.finish();
		}
	}
}
