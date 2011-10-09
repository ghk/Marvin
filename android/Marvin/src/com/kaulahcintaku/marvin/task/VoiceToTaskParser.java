package com.kaulahcintaku.marvin.task;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.kaulahcintaku.marvin.ConfigurationKeys;
import com.kaulahcintaku.marvin.MarvinActivity;
import com.kaulahcintaku.marvin.body.command.MarvinCommand;
import com.kaulahcintaku.marvin.task.HeadTask.HeadPositions;

public class VoiceToTaskParser {
	
	public static class ParseResult{
		
		public boolean IsTask;
		public MarvinTask Task;
		
		public ParseResult(MarvinTask task) {
			Task = task;
			IsTask = true;
		}
		
		public boolean IsCalling;
		public ParseResult(){
			IsCalling = true;
		}
	}
	
	private static String[] questionPrefixes = new String[]{
		"kapan", "siapa", "apa", "berapa", "dimana"
	};
	
	private static HashSet<String> names = new HashSet<String>(Arrays.asList(new String[]{
			"marvin", "vin", "fin"
	}));
	private static HashMap<String, Object[]> configures = new HashMap<String, Object[]>();
	private static HashSet<String> commands = new HashSet<String>();
	
	private static HashMap<String, int[]> moves = new HashMap<String, int[]>();
	private static HashMap<String, Float> turns = new HashMap<String, Float>();
	private static HashMap<String, Float> relativeTurns = new HashMap<String, Float>();
	private static HashMap<String, HeadPositions> heads = new HashMap<String, HeadPositions>();
	
	static{
		commands.addAll(names);
		
		configures.put("jangan berisik", new Object[]{ConfigurationKeys.NOISY_MODE, false});
		configures.put("jangan aktif", new Object[]{ConfigurationKeys.ACTIVE_MODE, false});
		configures.put("boleh berisik", new Object[]{ConfigurationKeys.NOISY_MODE, true});
		configures.put("boleh aktif", new Object[]{ConfigurationKeys.ACTIVE_MODE, true});
		commands.addAll(configures.keySet());
		
		moves.put("maju", new int[]{255, 1});
		moves.put("maju terus", new int[]{255, 2});
		moves.put("mundur", new int[]{-255, 1});
		moves.put("mundur terus", new int[] {-255, 2});
		commands.addAll(moves.keySet());
		
		turns.put("hadap utara", 0.0f);
		turns.put("hadap barat", 270.0f);
		turns.put("hadap timur",90.0f);
		turns.put("hadap selatan", 180.0f);
		commands.addAll(turns.keySet());
		
		relativeTurns.put("hadap kiri", -90.0f);
		relativeTurns.put("hadap kanan", 90.0f);
		relativeTurns.put("hadap belakang", 180.0f);
		commands.addAll(relativeTurns.keySet());
		
		heads.put("angguk", new HeadPositions(170, 90)
			.then(180, 90, 200)
			.then(120, 90, 1200)
			.then(180, 90, 1200)
			.then(120, 90, 1200)
			.then(170, 90, 200));
		heads.put("geleng", new HeadPositions(170, 90)
			.then(170, 60, 600)
			.then(170, 120, 1200)
			.then(170, 60, 1200)
			.then(170, 90, 600));
		heads.put("geleng kepala", heads.get("geleng"));
		heads.put("tengok kanan", new HeadPositions(170, 0));
		heads.put("tengok kiri", new HeadPositions(170, 180));
		heads.put("tengok depan", new HeadPositions(170, 90));
		heads.put("tengok atas", new HeadPositions(90, 90));
		commands.addAll(heads.keySet());
		
	}
	
	public static ParseResult parse(List<String> voices, MarvinTaskContext context){
		int totalLength = 0;
		for(String voice: voices)
			totalLength += voice.length();
		float avgLength = totalLength / (float) voices.size();
		
		if(avgLength < 15)
			return processShortVoices(voices, context);
		return processLongVoices(voices, context);
	}
	
	public static ParseResult processShortVoices(List<String> voices, MarvinTaskContext context){
		float bestScore =  0.6f;
		String bestCommand = null;
		
		outerloop:
		for(String command: commands){
			for(String voice: voices){
				int lev = getLevenshteinDistance(voice,command);
				if(lev == 0){
					bestScore = 1.0f;
					bestCommand = command;
					break outerloop;
				}
				int maxLength = Math.max(voice.length(), command.length());
				float score = (maxLength - lev) / (float)maxLength;
				if(score > bestScore){
					bestCommand = command;
					bestScore = score;
				}
			}
		}
		
		Log.d(MarvinActivity.TAG, "best:"+bestCommand+" score: "+bestScore);
		
		if(bestCommand == null){
			return null;
		}
		
		if(names.contains(bestCommand))
			return new ParseResult();
		
		if(configures.containsKey(bestCommand)){
			Object[] co = configures.get(bestCommand);
			return new ParseResult(new ConfiguringTask((String)co[0], co[1]));
		}
		
		if(turns.containsKey(bestCommand))
			return new ParseResult(new MoveTask(turns.get(bestCommand)));
		
		if(relativeTurns.containsKey(bestCommand))
			return new ParseResult(new MoveTask(context.getDirection() + relativeTurns.get(bestCommand)));
		
		if(moves.containsKey(bestCommand)){
			int[] ma = moves.get(bestCommand);
			return new ParseResult(new MoveTask(ma[0], ma[0], ma[1] * 1000));
		}
		
		if(heads.containsKey(bestCommand)){
			return new ParseResult(new HeadTask(heads.get(bestCommand)));
		}
		
		return null;
	}
	
	public static ParseResult processLongVoices(List<String> voices, MarvinTaskContext context){
		
		//question
		for(String voice: voices){
			for(String questionPrefix: questionPrefixes)
				if(voice.startsWith(questionPrefix))
					return new ParseResult(new QaTask(voice));
		}
		
		return null;
	}
	
	public static int getLevenshteinDistance (String s, String t) {
		  if (s == null || t == null) {
		    throw new IllegalArgumentException("Strings must not be null");
		  }
				
		  /*
		    The difference between this impl. and the previous is that, rather 
		     than creating and retaining a matrix of size s.length()+1 by t.length()+1, 
		     we maintain two single-dimensional arrays of length s.length()+1.  The first, d,
		     is the 'current working' distance array that maintains the newest distance cost
		     counts as we iterate through the characters of String s.  Each time we increment
		     the index of String t we are comparing, d is copied to p, the second int[].  Doing so
		     allows us to retain the previous cost counts as required by the algorithm (taking 
		     the minimum of the cost count to the left, up one, and diagonally up and to the left
		     of the current cost count being calculated).  (Note that the arrays aren't really 
		     copied anymore, just switched...this is clearly much better than cloning an array 
		     or doing a System.arraycopy() each time  through the outer loop.)

		     Effectively, the difference between the two implementations is this one does not 
		     cause an out of memory condition when calculating the LD over two very large strings.  		
		  */		
				
		  int n = s.length(); // length of s
		  int m = t.length(); // length of t
				
		  if (n == 0) {
		    return m;
		  } else if (m == 0) {
		    return n;
		  }

		  int p[] = new int[n+1]; //'previous' cost array, horizontally
		  int d[] = new int[n+1]; // cost array, horizontally
		  int _d[]; //placeholder to assist in swapping p and d

		  // indexes into strings s and t
		  int i; // iterates through s
		  int j; // iterates through t

		  char t_j; // jth character of t

		  int cost; // cost

		  for (i = 0; i<=n; i++) {
		     p[i] = i;
		  }
				
		  for (j = 1; j<=m; j++) {
		     t_j = t.charAt(j-1);
		     d[0] = j;
				
		     for (i=1; i<=n; i++) {
		        cost = s.charAt(i-1)==t_j ? 0 : 1;
		        // minimum of cell to the left+1, to the top+1, diagonally left and up +cost				
		        d[i] = Math.min(Math.min(d[i-1]+1, p[i]+1),  p[i-1]+cost);  
		     }

		     // copy current distance counts to 'previous row' distance counts
		     _d = p;
		     p = d;
		     d = _d;
		  } 
				
		  // our last action in the above loop was to switch d and p, so p now 
		  // actually has the most recent cost counts
		  return p[n];
		}

}
