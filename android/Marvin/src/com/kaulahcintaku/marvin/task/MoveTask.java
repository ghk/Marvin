package com.kaulahcintaku.marvin.task;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.kaulahcintaku.marvin.Marvin;
import com.kaulahcintaku.marvin.MarvinActivity;
import com.kaulahcintaku.marvin.R;
import com.kaulahcintaku.marvin.body.command.MarvinCommand;
import com.kaulahcintaku.marvin.body.command.MarvinMotorCommand;
import com.kaulahcintaku.marvin.body.command.MarvinMotorCommand.Direction;
import com.kaulahcintaku.marvin.body.command.MarvinMotorCommand.MotorType;

public class MoveTask extends MarvinTask{
	
	private float targetDirection;
	
	private int leftSpeed;
	private int rightSpeed;
	private int moveTime;
	
	private MarvinTaskLoop loop;
	
	public MoveTask(float targetDirection) {
		super();
		name = "Turn";
		this.targetDirection = targetDirection;
		while(targetDirection < 0)
			targetDirection += 360;
		while(targetDirection > 360)
			targetDirection -= 360;
		loop = new MarvinTaskLoop() {
			@Override
			public void loop() {
				turn();
			}
		};
	}
	
	
	public MoveTask(int leftSpeed, int rightSpeed, int moveTime) {
		super();
		name = "Move";
		this.leftSpeed = leftSpeed;
		this.rightSpeed = rightSpeed;
		this.moveTime = moveTime;
		loop = new MarvinTaskLoop() {
			@Override
			public void loop() {
				move();
			}
		};
	}

	@Override
	public List<MarvinTaskLoop> getLoops() {
		List<MarvinTaskLoop> loops = new ArrayList<MarvinTaskLoop>();
		loops.add(loop);
		return loops;
	}
	
	public void turn(){
		float dir = context.getDirection();
		float diff = targetDirection - dir;
		if(diff > 180){
			diff -= 360;
		}
		else if(diff < -180){
			diff += 360;
		}
		
		context.log(R.id.log1, "dir: "+dir+" target: "+targetDirection+" diff: "+diff);
		if(diff < 2 && diff > -2){
			try{
				long millis = System.currentTimeMillis();
				context.runBodyCommands(getMotorCommands(0, 0, 1));
				Thread.sleep(5000);
			}
			catch(Exception e){
			}
			context.finish();
		}
		else{
			try{
				int speed = 255;
				int time = 80;
				if(diff > -60 && diff < 60){
					speed = 192;
				}
				if(diff > -30 && diff < 30){
					time = 60;
				}
				if(diff < 0){
					//move left
					context.runBodyCommands(getMotorCommands(-speed, speed, time));
				}
				else{
					//move right
					context.runBodyCommands(getMotorCommands(speed, -speed, time));
				}
				try{
					Thread.sleep(0);
				}
				catch(InterruptedException ie){
				}
			}
			catch(Exception e){
				Log.e(MarvinActivity.TAG, e.getMessage(), e);
				context.log(R.id.log4, e.getMessage());
				try{
					Thread.sleep(2000);
				}
				catch(InterruptedException ie){
				}
				context.finish();
			}
		}
	}
	
	public void move(){
		try{
			context.log(R.id.log1, "l: "+leftSpeed+" r: "+rightSpeed+" t: "+moveTime);
			try{
				context.runBodyCommands(getMotorCommands(leftSpeed, rightSpeed, moveTime));
			}
			catch(Exception e){
				Log.e(MarvinActivity.TAG, e.getMessage(), e);
				context.log(R.id.log4, e.getMessage());
			}
			Thread.sleep(moveTime);
		}
		catch(InterruptedException ie){
		}
		finally{
			context.finish();
		}
	}
	
	@Override
	public void onStop() {
		try{
			context.runBodyCommands(getMotorCommands(0, 0, 1));
		}
		catch(Exception e){
		}
	}
	
	private static List<MarvinCommand> getMotorCommands(int left, int right, int time){
		List<MarvinCommand> commands = new ArrayList<MarvinCommand>();
		commands.add(new MarvinMotorCommand(MotorType.Left, left >= 0 ? Direction.Forward : Direction.Backward, (byte) (Math.abs(left)), (byte)time));
		commands.add(new MarvinMotorCommand(MotorType.Right, right >= 0 ? Direction.Forward : Direction.Backward, (byte) (Math.abs(right)), (byte)time));
		return commands;
	}
}
