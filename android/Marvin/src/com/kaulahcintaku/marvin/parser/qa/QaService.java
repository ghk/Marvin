package com.kaulahcintaku.marvin.parser.qa; 

import java.util.HashSet;
import java.util.Set;

import android.util.Log;

import com.google.api.translate.Language;
import com.google.api.translate.Translate;
import com.kaulahcintaku.marvin.MarvinActivity;
import com.wolfram.alpha.WAEngine;
import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAPlainText;
import com.wolfram.alpha.WAPod;
import com.wolfram.alpha.WAQuery;
import com.wolfram.alpha.WAQueryResult;
import com.wolfram.alpha.WASubpod;

public class QaService {
	private static final String wolframId = "7EU6RK-6UAEJVR38E";
	
	private final WAEngine wolfram ;
	private final Set<String> wolframResultIds = new HashSet<String>();
	
	public QaService() {
		wolfram = new WAEngine();
		wolfram.setAppID(wolframId);
		wolframResultIds.add("Result");
		wolframResultIds.add("Value");
	}
	public String ask(String question, QaCallback callback) throws QaException{
		Log.d(MarvinActivity.TAG, "idQuestion "+ question);
		
		String enQuestion = translate(question, true);
		Log.d(MarvinActivity.TAG, "enQuestion "+ enQuestion);
		callback.onTranslateForwardComplete(enQuestion);
		
		if(callback.isCancelled())
			throw new QaException("cancelled");
		String enAnswer = askWolfram(enQuestion);
		Log.d(MarvinActivity.TAG, "enAnswer "+ enAnswer);
		callback.onAskWolframComplete(enAnswer);
		
		if(callback.isCancelled())
			throw new QaException("cancelled");
		String idAnswer = translate(enAnswer, false);
		Log.d(MarvinActivity.TAG, "idAnswer "+ idAnswer);
		
		return idAnswer;
	}
	
	private String translate(String source, boolean toEnglish) throws QaException {
		Language sLang = toEnglish ? Language.INDONESIAN : Language.ENGLISH;
		Language tLang = toEnglish ? Language.ENGLISH : Language.INDONESIAN;
		try{
		    Translate.setHttpReferrer("http://www.gozalikumara.com/");
		    return Translate.execute(source, sLang, tLang);
		}
		catch (Exception e) {
			throw new QaException("Error on translating from "+sLang+" to "+tLang, e);
		}
	}
	
	
	private String askWolfram(String question) throws QaException{
	    wolfram.addFormat("plaintext");

        WAQuery query = wolfram.createQuery();
        
        query.setInput(question);
        
        try {
            WAQueryResult queryResult = wolfram.performQuery(query);
            
            if (queryResult.isError()) {
            	throw new QaException("wolfram query error: "+queryResult.getErrorCode()+" "+queryResult.getErrorMessage());
            } else if (!queryResult.isSuccess()) {
            	throw new QaException("wolfram query not success");
            } else {
                for (WAPod pod : queryResult.getPods()) {
                    if (!pod.isError() && pod.getID() != null && wolframResultIds.contains(pod.getID())){
                        for (WASubpod subpod : pod.getSubpods()) {
                            for (Object element : subpod.getContents()) {
                                if (element instanceof WAPlainText) {
                                	return ((WAPlainText) element).getText();
                                }
                            }
                        }
                    }
                }
            	throw new QaException("wolfram query doesn't contains plain text, ponds count:"+queryResult.getPods().length);
            }
        } catch (WAException e) {
        	throw new QaException("wolfram exception", e);
        }
	}
 }
