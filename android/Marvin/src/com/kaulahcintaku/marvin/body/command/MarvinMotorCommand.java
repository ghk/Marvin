package com.kaulahcintaku.marvin.body.command;

public class MarvinMotorCommand implements MarvinCommand{
	
	private static final byte COMMAND_TYPE = 3;
	
	public enum MotorType {
		Left, Right;
	};
	public enum Direction {
		Forward, Backward;
	};
	
	private MotorType type;
	private Direction direction;
	private byte speed;
	private short time;
	
	public MarvinMotorCommand(MotorType type, Direction direction, byte speed,
			short time) {
		this.type = type;
		this.direction = direction;
		this.speed = speed;
		this.time = time;
	}

	public byte[] toByteArray() {
		return new byte[]{
			(byte) (type == MotorType.Left ? 0 : 1),
			(byte) (direction == Direction.Forward ? 1 : 2),
			speed,
			(byte)(time >> 8),
			(byte)(time & 0xFF)
		};
	}

	public byte getCommandType() {
		return COMMAND_TYPE;
	}
	
}