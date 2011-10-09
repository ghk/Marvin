package com.kaulahcintaku.marvin.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MarvinTask {
	
	protected MarvinTaskContext context;
	protected String name;
	
	public abstract List<MarvinTaskLoop> getLoops();
	
	public void setContext(MarvinTaskContext context){
		this.context = context;
	}
	
	public String getName() {
		return name;
	}
	
	public void onStart(){
	}
	
	public void onStop(){
	}
	
	
}
