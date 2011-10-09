package com.kaulahcintaku.marvin.body.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.kaulahcintaku.marvin.MarvinActivity;
import com.kaulahcintaku.marvin.body.MarvinBody;
import com.kaulahcintaku.marvin.body.MarvinBodyUser;
import com.kaulahcintaku.marvin.body.MarvinMessagingUtil;
import com.kaulahcintaku.marvin.body.MarvinMessagingUtil.MessageType;
import com.kaulahcintaku.marvin.body.command.MarvinCommand;
import com.kaulahcintaku.marvin.body.command.MarvinCommandResult;
import com.kaulahcintaku.marvin.body.command.MarvinCommandUtil;
import com.kaulahcintaku.marvin.body.command.MarvinConnectionException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BluetoothBody implements MarvinBody, Runnable{
	
	private static final String TARGET_ADDRESS = "00:11:06:24:01:05";
	
	private static final int CONNECTED = 1;
	private static final int DISCONNECTED = 2;
	private static final int PING_SUCCEED = 3;
	private static final int CONNECT_FAILED = 4;
	
	private MarvinBodyUser user;
	private Handler handler;
	
	private BluetoothDevice device;
	private BluetoothSocket socket;
	private boolean socketConnected;
	private Thread ownerThread;
	
	private boolean isStopped = false;
	
	public BluetoothBody(Context context, MarvinBodyUser user) {
		this.user = user;
		handler = new Handler(context.getMainLooper()){
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch(msg.what){
				case CONNECTED:
					BluetoothBody.this.user.onBodyConnected();
					break;
				case DISCONNECTED:
					BluetoothBody.this.user.onBodyDisconnected();
					break;
				case PING_SUCCEED:
					BluetoothBody.this.user.onBodyPingSucceeded();
					break;
				case CONNECT_FAILED:
					BluetoothBody.this.user.onBodyConnectFailed((String)msg.obj);
					break;
				}
			}
		};
		ownerThread = new Thread(this);
		ownerThread.start();
	}
	
	@Override
	public List<MarvinCommandResult> runCommands(List<MarvinCommand> commands) {
		if(!isConnected())
			throw new MarvinConnectionException("not connected");
		List<MarvinCommandResult> results = new ArrayList<MarvinCommandResult>();
		List<byte[]> messageContents = MarvinCommandUtil.toMessageContents(commands);
		for(byte[] messageContent: messageContents){
			byte[] header = MarvinMessagingUtil.createHeader(MessageType.Command, messageContent.length);
			MessageReader.CommandReader reader = new MessageReader.CommandReader();
			sendMessage(header, messageContent, reader);
			if(reader.hasError())
				throw new MarvinConnectionException("Error on sending command: "+reader.getErrorCode());
			results.addAll(reader.getResults());
		}
		return results;
	}
	
	
	
	private class ConnectThread implements Runnable{
		
		private String result;
		private boolean initConnectFinished = false;
		private Thread connectThread = null;
		
		public void run() {
			if(!initConnectFinished){
				try{
					result = initConnect();
				}
				finally{
					synchronized (this) {
						initConnectFinished = true;
						if(result == null){
							connectThread = new Thread(this);
							connectThread.start();
						}
						this.notifyAll();
					}
				}
			}
			else{
				result = connect();
			}
		}
		
		public void join() throws InterruptedException{
			synchronized (this) {
				while(!initConnectFinished){
					this.wait();
				}
			}
			if(result == null)
				connectThread.join();
		}
		
		private String initConnect() {
			final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
			if(adapter == null)
				return "adapter is null";
			if(device == null){
				adapter.disable();
				if(!adapter.isEnabled())
					adapter.enable();
					adapter.cancelDiscovery();
					device = adapter.getRemoteDevice(TARGET_ADDRESS);
			}
			Exception e = null;
			
			try{
				socketConnected = false;
				adapter.cancelDiscovery();
			}
			catch(Exception ex){
				socket = null;
				e = ex;
			}
			if(e != null){
				Log.e(MarvinActivity.TAG, e.getMessage(), e);
			}
			return null;
		}
		
		private String connect(){
			Exception e = null;
			try{
				socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
				socket.connect();
				socketConnected = true;
			}
			catch(IOException ioe){
				socket = null;
				e = ioe;
			}
			catch(Exception ex){
				socket = null;
				e = ex;
			}
			if(e != null){
				//Log.e(MainActivity.TAG, e.getMessage(), e);
				return e.getMessage();
			} else{
				return null;
			}
		}
	}
	
	private void sendMessage(int what, String text){
		Message message = Message.obtain(handler, what, text);
		handler.sendMessage(message);
	}
	
	public void disconnect() {
		if(isConnected()){
			try{
				socket.close();
			}
			catch(IOException ioe){
				ioe.printStackTrace();
			}
			finally{
				socket = null;
				sendMessage(DISCONNECTED, null);
			}
		}
		socket = null;
	}
	
	public boolean isConnected() {
		return socket != null && socketConnected;
	}
	
	public boolean ping() {
		if(!isConnected())
			throw new MarvinConnectionException("not connected");
		MessageReader.PingReader reader = new MessageReader.PingReader();
		byte[] header = MarvinMessagingUtil.createHeader(MessageType.Ping, 0);
		try{
			sendMessage(header, null, reader);
			return reader.getResult();
		}
		catch(MarvinConnectionException me){
			Log.e(MarvinActivity.TAG, me.getMessage(), me);
			return false;
		}
	}
	
	
	private synchronized void sendMessage(byte[] header, byte[] content, MessageReader reader) {
		try{
			Log.d(MarvinActivity.TAG, "sending message to bluetooth start");
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			os.write(header);
			if(content != null)
				os.write(content);
			os.flush();
			while(!reader.isFinished()){
				int r = is.read();
				reader.read(r);
			}
		}
		catch(IOException ioe){
			disconnect();
			socket = null;
			throw new MarvinConnectionException(ioe);
		}
		finally{
			Log.d(MarvinActivity.TAG, "sending message to bluetooth finished");
		}
	}

	@Override
	public void run() {
		while(!isStopped){
			int sleepTime = 1000;
			if(!isConnected()){
				ConnectThread connect = new ConnectThread();
				handler.post(connect);
				try{
					connect.join();
					String error = connect.result;
					if(error != null){
						sleepTime = 5000;
						sendMessage(CONNECT_FAILED, error);
					}
					else{
						sendMessage(CONNECTED, null);
					}
				}
				catch(InterruptedException ie){
					
				}
			} else{
				boolean pingSucceed = ping();
				if(!pingSucceed){
					sleepTime = 2000;
				}
				else {
					sendMessage(PING_SUCCEED, null);
				}
			}
			try{
				Thread.sleep(sleepTime);
			}
			catch (InterruptedException e) {
			}
		}
	}

	@Override
	public void shutdown() {
		isStopped = true;
		ownerThread.interrupt();
		try{
			BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
			if(adapter != null)
				adapter.disable();
		}
		catch(Exception e){
			Log.e(MarvinActivity.TAG, e.getMessage(), e);
		}
	}
	
}
