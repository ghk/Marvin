package com.kaulahcintaku.marvin.task;

import java.util.List;

import com.kaulahcintaku.marvin.body.command.MarvinCommand;

public interface MarvinTaskContext {
	
	void speak(String text);
	List<String> listenForVoice() throws InterruptedException, ListenException;
	void cancelListening();
	
	void log(int tvId, String text);
	
	boolean isLoopCanceled();
	void finish();
	void finish(MarvinTask nextTask);
	
	float getDirection();
	float waitForDirectionChanged() throws InterruptedException;
	void runBodyCommands(List<MarvinCommand> commands);
	
	void setConfiguration(String key, Object value);
	Object getConfiguration(String key);
	
}
