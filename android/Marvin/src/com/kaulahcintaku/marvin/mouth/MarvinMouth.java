package com.kaulahcintaku.marvin.mouth;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;

public class MarvinMouth implements OnInitListener, OnUtteranceCompletedListener{
	private TextToSpeech textToSpeech;
	
	public MarvinMouth(Context context){
		textToSpeech = new TextToSpeech(context, this);
		textToSpeech.setOnUtteranceCompletedListener(this);
	}

	@Override
	public void onInit(int status) {
	}
	
	public void speak(String text){
		textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null);
	}
	
	public void shutDown(){
		textToSpeech.stop();
		textToSpeech.shutdown();
	}

	@Override
	public void onUtteranceCompleted(String utteranceId) {
		// TODO Auto-generated method stub
		
	}
}
