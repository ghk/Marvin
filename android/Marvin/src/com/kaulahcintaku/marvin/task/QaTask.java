package com.kaulahcintaku.marvin.task;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes.Name;

import android.util.Log;

import com.kaulahcintaku.marvin.MarvinActivity;
import com.kaulahcintaku.marvin.R;
import com.kaulahcintaku.marvin.parser.qa.QaCallback;
import com.kaulahcintaku.marvin.parser.qa.QaException;
import com.kaulahcintaku.marvin.parser.qa.QaService;

public class QaTask extends MarvinTask implements QaCallback{
	private String question;

	public QaTask(String question) {
		super();
		name = "Question Answering";
		this.question = question;
	}
	
	@Override
	public List<MarvinTaskLoop> getLoops() {
		List<MarvinTaskLoop> results = new ArrayList<MarvinTaskLoop>();
		results.add(new MarvinTaskLoop() {
			public void loop() {
				ask();
			}
		});
		return results;
	}
	
	public void ask(){
		context.log(R.id.log1, question);
		try{
			String answer = new QaService().ask(question, this);
			context.speak(answer);
			context.log(R.id.log4, "answer: "+answer);
			int delay =  answer.length() * 300;
			context.log(R.id.log5, "delayna: "+delay);
			try{
				Thread.sleep(delay);
			}
			catch(InterruptedException ie){
			}
		}
		catch(QaException exception){
			context.speak("aku tidak tahu");
			Log.e(MarvinActivity.TAG, exception.getMessage(), exception);
			context.log(R.id.log4, exception.getMessage());
			try{
				Thread.sleep(2000);
			}
			catch(InterruptedException ie){
			}
		}
		finally{
			context.finish();
		}
	}

	@Override
	public boolean isCancelled() {
		return context.isLoopCanceled();
	}

	@Override
	public void onTranslateForwardComplete(String result) {
		context.log(R.id.log2, result);
	}

	@Override
	public void onAskWolframComplete(String result) {
		context.log(R.id.log3, result);
	}
	
	
}
