package com.kaulahcintaku.marvin.body.dummy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.kaulahcintaku.marvin.body.MarvinBody;
import com.kaulahcintaku.marvin.body.MarvinMessagingUtil;
import com.kaulahcintaku.marvin.body.MarvinMessagingUtil.MessageType;
import com.kaulahcintaku.marvin.body.command.MarvinCommand;
import com.kaulahcintaku.marvin.body.command.MarvinCommandResult;
import com.kaulahcintaku.marvin.body.command.MarvinCommandUtil;
import com.kaulahcintaku.marvin.body.command.MarvinConnectionException;

public class DummyBody implements MarvinBody{
	
	public static interface DummyBodyLogger {
		void log(String log);
	}
	
	public static DummyBodyLogger SYSOUT_LOGGER = new DummyBodyLogger() {
		public void log(String log) {
			System.out.println(log);
		}
	};
	
	public static DummyBodyLogger NULL_LOGGER = new DummyBodyLogger() {
		public void log(String log) {
		}
	};
	
	private boolean connected = false;
	private DummyBodyLogger logger;
	
	public DummyBody(){
		this(null);
	}
	
	public DummyBody(DummyBodyLogger logger) {
		if(logger == null)
			logger = NULL_LOGGER;
		this.logger = logger;
	}

	public void connect() {
		connected = true;
		logger.log("connect()");
	}

	public void disconnect() {
		connected = false;
		logger.log("disconnect()");
	}

	public boolean isConnected() {
		return connected;
	}

	public boolean ping() {
		logger.log("ping()");
		if(!connected)
			throw new MarvinConnectionException();
		byte[] header = MarvinMessagingUtil.createHeader(MessageType.Ping, 0);
		logger.log("sending message, header: "+Arrays.toString(header)+" content: "+Arrays.toString(new byte[0]));
		return true;
	}

	@Override
	public List<MarvinCommandResult> runCommands(List<MarvinCommand> commands){
		logger.log("runCommands()");
		if(!connected)
			throw new MarvinConnectionException();
		List<byte[]> messageContents = MarvinCommandUtil.toMessageContents(commands);
		for(byte[] messageContent: messageContents){
			byte[] header = MarvinMessagingUtil.createHeader(MessageType.Command, messageContent.length);
			logger.log("sending message, header: "+Arrays.toString(header)+" content: "+Arrays.toString(messageContent));
		}
		List<MarvinCommandResult> results = new ArrayList<MarvinCommandResult>();
		for(int i = 0; i < commands.size(); i++){
			results.add(MarvinCommandResult.SUCCESS);
		}
		return results;
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}
}
