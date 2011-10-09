package com.kaulahcintaku.marvin.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.kaulahcintaku.marvin.MarvinActivity;
import com.kaulahcintaku.marvin.R;
import com.kaulahcintaku.marvin.body.command.MarvinCommand;
import com.kaulahcintaku.marvin.body.command.MarvinServoCommand;
import com.kaulahcintaku.marvin.body.command.MarvinSwitchCommand;
import com.kaulahcintaku.marvin.body.command.MarvinServoCommand.ServoType;
import com.kaulahcintaku.marvin.body.command.MarvinSwitchCommand.SwitchType;

public class HeadTask extends MarvinTask{
	
	public static class HeadPositions{
		private List<HeadPosition> positions = new ArrayList<HeadPosition>();
		public HeadPositions(int startVertical, int startHorizontal) {
			positions.add(new HeadPosition(startVertical, startHorizontal, 0));
		}
		
		public HeadPositions then(int vertical, int horizontal, int timeDiff){
			int previousTime = positions.get(positions.size() - 1).TargetTime;
			positions.add(new HeadPosition(vertical, horizontal, previousTime + timeDiff));
			return this;
		}
		
		public Object[] getCurrentPosition(int currentTime){
			HeadPosition prev = null;
			for(HeadPosition next: positions){
				if(prev != null){
					if(prev.TargetTime < currentTime && next.TargetTime > currentTime){
						float ratio = (currentTime - prev.TargetTime) / (float) (next.TargetTime - prev.TargetTime);
						int vertical = prev.Vertical + (int)(ratio * (next.Vertical - prev.Vertical));
						int horizontal = prev.Horizontal + (int)(ratio * (next.Horizontal - prev.Horizontal));
						return new Object[]{vertical, horizontal, true};
					}
				}
				
				prev = next;
			}
			return new Object[]{prev.Vertical, prev.Horizontal, false};
		}
	}
	
	public static class HeadPosition{
		public int Vertical;
		public int Horizontal;
		public int TargetTime;
		
		public HeadPosition(int vertical, int horizontal, int targetTime) {
			super();
			Vertical = vertical;
			Horizontal = horizontal;
			TargetTime = targetTime;
		}
	}
	
	private HeadPositions positions;
	private long startMillis;
	public HeadTask(HeadPositions positions) {
		super();
		this.positions = positions;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		startMillis = System.currentTimeMillis();
	}
	
	@Override
	public void onStop() {
		try{
			context.runBodyCommands(Arrays.asList(new MarvinCommand[]{new MarvinSwitchCommand(SwitchType.Servos, false)}));
		}
		catch(Exception e){
			Log.e(MarvinActivity.TAG, e.getMessage(), e);
			context.log(R.id.log4, e.getMessage());
		}
		super.onStop();
	}
	
	@Override
	public List<MarvinTaskLoop> getLoops() {
		List<MarvinTaskLoop> results = new ArrayList<MarvinTaskLoop>();
		results.add(new MarvinTaskLoop() {
			public void loop() {
				run();
			}
		});
		return results;
	}
	
	
	public void run(){
		Object[] current = positions.getCurrentPosition((int) (System.currentTimeMillis() - startMillis));
		context.log(R.id.log1, "v: "+current[0]+ " h: "+current[1]);
		try{
			context.runBodyCommands(getCommands(current));
			try{
				Thread.sleep(500);
			}
			catch(InterruptedException ie){
			}
		}
		catch(Exception e){
			Log.e(MarvinActivity.TAG, e.getMessage(), e);
			context.log(R.id.log2, e.getMessage());
			context.finish();
		}
		
		if(!(Boolean)current[2]){
			try{
				Thread.sleep(500);
			}
			catch(InterruptedException ie){
			}
			context.finish();
		}
	}
	
	private static List<MarvinCommand> getCommands(Object[] pos){
		List<MarvinCommand> commands = new ArrayList<MarvinCommand>();
		commands.add(new MarvinSwitchCommand(SwitchType.Servos, true));
		commands.add(new MarvinServoCommand(ServoType.Vertical,  (Integer)pos[0]));
		commands.add(new MarvinServoCommand(ServoType.Horizontal, (Integer)pos[1]));
		return commands;
	}
}
