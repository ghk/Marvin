package com.kaulahcintaku.marvin.body.command;

public class MarvinSwitchCommand implements MarvinCommand{
	
	private static final byte COMMAND_TYPE = 1;
	
	public enum SwitchType {
		 Servos, LED_0;
	};
	private SwitchType type;
	private boolean on;
	
	public MarvinSwitchCommand(SwitchType type, boolean on){
		this.type = type;
		this.on = on;
	}

	public byte[] toByteArray() {
		return new byte[]{
			(byte) type.ordinal(),
			(byte) (on ? 255 : 0), 
		}; 
	}

	public byte getCommandType() {
		return COMMAND_TYPE;
	}
	
}