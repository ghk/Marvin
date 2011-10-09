package com.kaulahcintaku.marvin.task;

import java.util.ArrayList;
import java.util.List;

import com.kaulahcintaku.marvin.R;
import com.kaulahcintaku.marvin.SpeechTexts;
import com.kaulahcintaku.marvin.task.VoiceToTaskParser.ParseResult;

public class WaitForCommandTask extends MarvinTask{
	
	private boolean isCalling;
	
	public WaitForCommandTask() {
		name = "Wait for Command";
	}

	@Override
	public List<MarvinTaskLoop> getLoops() {
		List<MarvinTaskLoop> results = new ArrayList<MarvinTaskLoop>();
		results.add(new MarvinTaskLoop() {
			public void loop() {
				main();
			}
		});
		return results;
	}
	
	public void main(){
		try{
			List<String> voices = context.listenForVoice();
			ParseResult parsed = VoiceToTaskParser.parse(voices, context);
			if(parsed == null){
				context.speak(SpeechTexts.CANNOT_UNDERSTAND_COMMAND);
				try{
					Thread.sleep(3000);
				}
				catch(InterruptedException ie){
				}
			}
			else if(parsed.IsCalling){
				context.speak("yaa?");
				try{
					Thread.sleep(2000);
				}
				catch(InterruptedException ie){
				}
			}
			else{
				context.finish(parsed.Task);
			}
		}
		catch(InterruptedException ie){
		}
		catch(ListenException le){
		}
	}
	
	
	@Override
	public void onStop() {
		super.onStop();
		context.cancelListening();
	}

}
