package com.kaulahcintaku.marvin.body.command;

public interface MarvinCommand {
	byte[] toByteArray();
	byte getCommandType();
}
