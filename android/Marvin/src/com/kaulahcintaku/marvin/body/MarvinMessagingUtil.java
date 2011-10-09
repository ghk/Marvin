package com.kaulahcintaku.marvin.body;

public class MarvinMessagingUtil {
	private static final byte HEADER1 = 89;
	private static final byte HEADER2 = 25;
	
	public static enum MessageType{
		Ping,
		Configure,
		Fetch,
		Command
	};
	
	public static byte[] createHeader(MessageType type, int contentLength){
		byte[] results = new byte[4];
		results[0] = HEADER1;
		results[1] = HEADER2;
		results[2] = (byte)type.ordinal();
		results[3] = (byte)contentLength;
		return results;
	}
}