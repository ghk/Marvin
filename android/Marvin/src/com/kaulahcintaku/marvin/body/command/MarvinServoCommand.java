package com.kaulahcintaku.marvin.body.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarvinServoCommand implements MarvinCommand{
	
	private static final byte COMMAND_TYPE = 4;
	
	public enum ServoType {
		Horizontal, Vertical;
	};
	
	private static Map<ServoType, Integer> MIN_VALUES;
	private static Map<ServoType, Integer> MAX_VALUES;
	private static Map<ServoType, Integer> DEFAULT_VALUES;
	
	static{
		MIN_VALUES = new HashMap<ServoType, Integer>();
		MIN_VALUES.put(ServoType.Horizontal, 0);
		MIN_VALUES.put(ServoType.Vertical, 80);
		
		MAX_VALUES = new HashMap<ServoType, Integer>();
		MAX_VALUES.put(ServoType.Horizontal, 180);
		MAX_VALUES.put(ServoType.Vertical, 180);
		
		DEFAULT_VALUES = new HashMap<ServoType, Integer>();
		DEFAULT_VALUES.put(ServoType.Horizontal, 90);
		DEFAULT_VALUES.put(ServoType.Vertical, 170);
	}
	
	public static List<MarvinCommand> getDefaultServoCommands(){
		return Arrays.asList(new MarvinCommand[]{
				new MarvinServoCommand(ServoType.Horizontal),
				new MarvinServoCommand(ServoType.Vertical),
		});
	}
	
	private ServoType type;
	private int target;
	
	public MarvinServoCommand(ServoType type){
		this(type, DEFAULT_VALUES.get(type));
	}
	
	public MarvinServoCommand(ServoType type, int target){
		if(target > MAX_VALUES.get(type))
			throw new IllegalArgumentException("for type "+type+" cannot > than "+MAX_VALUES.get(type)+ "value: "+target);
		if(target < MIN_VALUES.get(type))
			throw new IllegalArgumentException("for type "+type+" cannot < than "+MIN_VALUES.get(type)+ "value: "+target);
		
		this.type = type;
		this.target = target;
	}

	public byte[] toByteArray() {
		return new byte[]{
			(byte) (type == ServoType.Vertical ? 0 : 1),
			(byte) target
		};
	}

	public byte getCommandType() {
		return COMMAND_TYPE;
	}
	
}