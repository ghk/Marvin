package com.kaulahcintaku.marvin.body.command;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MarvinCommandUtil {
	public static List<byte[]> toMessageContents(List<MarvinCommand> commands){
		List<byte[]> results = new ArrayList<byte[]>();
		ByteBuffer buffer = ByteBuffer.allocate(254); // 255 - 1 for command count
		byte commandCount = 0;
		for(MarvinCommand command: commands){
			byte[] commandBytes = command.toByteArray();
			if(buffer.remaining() < commandBytes.length + 2){ //2 for command type and length
				byte[] messageContent = new byte[buffer.position() + 1];
				messageContent[0] = commandCount;
				System.arraycopy(buffer.array(), 0, messageContent, 1, buffer.position());
				results.add(messageContent);
				buffer.position(0);
				commandCount = 0;
			}
			buffer.put(command.getCommandType());
			buffer.put((byte)commandBytes.length);
			buffer.put(commandBytes);
			commandCount++;
		}
		if(commandCount > 0){
			byte[] messageContent = new byte[buffer.position() + 1];
			messageContent[0] = commandCount;
			System.arraycopy(buffer.array(), 0, messageContent, 1, buffer.position());
			results.add(messageContent);
		}
		return results;
	}
	
}
