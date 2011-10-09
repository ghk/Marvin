package com.kaulahcintaku.marvin.ear;

import java.util.ArrayList;

import com.kaulahcintaku.marvin.MarvinActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

public class MarvinEar implements RecognitionListener{
	
	private boolean hasListenDelayed = false;
	
	private boolean isListeningStarted = false;
	private boolean isListeningCancelled = false;
	private boolean isListeningFinished = false;
	
	private Thread listenginStartedChecker;
	
	private final MarvinEarUser user;
	private final Handler handler;
	
	private final SpeechRecognizer sr;
	
	
	public MarvinEar(Context context, MarvinEarUser user) {
		this.user = user;
		
		handler = new Handler(context.getMainLooper());
		sr = SpeechRecognizer.createSpeechRecognizer(context);
		sr.setRecognitionListener(this);
	}
	
	private Intent createIntent(){
		Intent srIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        srIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
		        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        srIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
		        "com.kaulahcintaku.marvin");
        srIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
		        "id-ID");
        return srIntent;
	}

	@Override
	public void onBeginningOfSpeech() {
	}

	@Override
	public void onBufferReceived(byte[] arg0) {
	}

	@Override
	public void onEndOfSpeech() {
		Log.d(MarvinActivity.TAG, "end of speech");
		user.onEarStopListening();
	}
	
	@Override
	public void onError(int errorCode) {
        isListeningFinished = true;
        
		String error = null;
		switch(errorCode){
		case SpeechRecognizer.ERROR_AUDIO:
			error = "ERROR AUDIO";
			break;
		case SpeechRecognizer.ERROR_CLIENT:
			error = "ERROR CLIENT";
			break;
		case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
			error = "ERROR INSUFFICIENT PERMISSION";
			break;
		case SpeechRecognizer.ERROR_NETWORK:
			error = "ERROR NETWORK";
			break;
		case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
			error = "ERROR NETWORK TIMED OUT";
			break;
		case SpeechRecognizer.ERROR_NO_MATCH:
			error = "ERROR NO MATCH";
			break;
		case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
			error = "ERROR RECOGNIZER BUSY";
			break;
		case SpeechRecognizer.ERROR_SERVER:
			error = "ERROR SERVER";
			break;
		case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
			error = "ERROR SPEECH TIMED OUT";
			break;
		}
		user.onEarListeningError(errorCode, error);
	}

	@Override
	public void onEvent(int arg0, Bundle arg1) {
		Log.d(MarvinActivity.TAG, "event: "+arg0);
	}

	@Override
	public void onPartialResults(Bundle arg0) {
	}

	@Override
	public synchronized void onReadyForSpeech(Bundle arg0) {
		isListeningStarted = true;
		
		listenginStartedChecker.interrupt();
		user.onEarListening();
	}

	@Override
	public void onResults(Bundle results) {
        isListeningFinished = true;
        
        ArrayList<String> hearingResults = results
        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        
        user.onEarHear(hearingResults);
	}
	
	@Override
	public void onRmsChanged(float arg0) {
	}
	
	public synchronized void listen(long delay){
		if(hasListenDelayed)
			return;
		
		handler.postDelayed(new ListeningPoster(), delay);
		hasListenDelayed = true;
	}
	
	
	public void cancel(){
		isListeningCancelled = true;
		try{
			sr.cancel();
		}
		catch(Exception e){
			Log.e(MarvinActivity.TAG, e.getMessage(), e);
		}
	}

	public void shutdown(){
		isListeningCancelled = true;
		try{
			sr.cancel();
		}
		catch(Exception e){
			Log.e(MarvinActivity.TAG, e.getMessage(), e);
		}
	}

	private class ListeningPoster implements Runnable{
		public void run() {
			synchronized(MarvinEar.this){
				hasListenDelayed = false;
				
				try{
					sr.cancel();
				}
				catch(Exception e){
				}
				
				isListeningStarted = false;
				isListeningCancelled = false;
				isListeningFinished = false;
				
				listenginStartedChecker = new Thread(new ListeningStartChecker());
				listenginStartedChecker.start();
				
				sr.startListening(createIntent());
			}
		}
	}
	
	private class ListeningStartChecker implements Runnable{
		@Override
		public void run() {
			int timeout = 2000;
			long startMillis = System.currentTimeMillis();
			while(startMillis + timeout > System.currentTimeMillis() && !isListeningStarted){
				try{
					Thread.sleep(timeout);
				}
				catch(InterruptedException ie){
				}
			}
			if(!isListeningStarted && !isListeningCancelled && !isListeningFinished){
				Log.d(MarvinActivity.TAG, "restart listening");
				listen(1000);
			}
		}
	}


}
