package com.kaulahcintaku.marvin.body.bluetooth;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.kaulahcintaku.marvin.MarvinActivity;
import com.kaulahcintaku.marvin.body.command.MarvinCommandResult;

public interface MessageReader{
		void read(int b);
		boolean isFinished();
		
	public static class PingReader implements MessageReader{
		private boolean isFinished = false;
		private boolean result = false;
		private int replyCount = 0;
		private boolean hasError = false;
		@Override
		public boolean isFinished() {
			return isFinished;
		}
		@Override
		public void read(int b) {
			if(hasError){
				isFinished = true;
			}
			else if(replyCount == 0 && b != 0){
				hasError = true;
			}
			replyCount++;
			if(replyCount >= 3){
				result = true;
				isFinished = true;
			}
		}
		
		public boolean getResult(){
			return result;
		}
	}
	
	public static class CommandReader implements MessageReader{
		private int commandCount = 0;
		private int expectedCount = 0;
		private int readCount = 0;
		private boolean hasError = false;
		private List<MarvinCommandResult> results = new ArrayList<MarvinCommandResult>();
		private boolean isFinished = false;
		private byte errorCode = 0;
		
		@Override
		public boolean isFinished() {
			return isFinished;
		}
		@Override
		public void read(int b) {
			if(hasError){
				isFinished = true;
				Log.d(MarvinActivity.TAG, "error data: "+b);
			}
			else if(readCount == 0 && b != 0){
				errorCode = (byte) b;
				hasError = true;
			}
			else if(readCount == 1){
				expectedCount = b;
				if(expectedCount == 0)
					isFinished = true;
			}
			else if(readCount > 1){
				results.add(MarvinCommandResult.fromRetVal((byte) b));
				commandCount++;
				if(commandCount >= expectedCount){
					isFinished = true;
				}
			}
			readCount++;
		}
		
		public byte getErrorCode() {
			return errorCode;
		}
		
		public boolean hasError() {
			return hasError;
		}
		
		public List<MarvinCommandResult> getResults() {
			return results;
		}
	}
}
	