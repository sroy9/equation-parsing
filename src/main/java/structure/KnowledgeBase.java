package structure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import reader.DocReader;
import utils.Params;

public class KnowledgeBase {
	
	public static Map<String, List<String>> mathNodeMap;
	public static Set<String> mathNodeSet;
	public static Set<String> mathIndicatorSet;
	public static Set<String> specialVarTokens;
	
	static {
		// Math knowledge
		mathNodeMap = new HashMap<String, List<String>>();
		mathNodeMap.put("ADD", Arrays.asList("plus", "more", "sum", "exceeds", 
				"added", "older", "faster", "greater", "longer", "increased"));
		mathNodeMap.put("SUB", Arrays.asList("subtracted", "minus", "less", 
				"short", "difference", "differs", "younger", "slower", "fewer", 
				"shorter", "decreased"));
		mathNodeMap.put("MUL", Arrays.asList("product"));
		mathNodeMap.put("DIV", Arrays.asList("ratio"));
		mathNodeSet = new HashSet<>();
		mathNodeSet.addAll(mathNodeMap.get("ADD"));
		mathNodeSet.addAll(mathNodeMap.get("SUB"));
		mathNodeSet.addAll(mathNodeMap.get("MUL"));
		mathNodeSet.addAll(mathNodeMap.get("DIV"));
		mathIndicatorSet = new HashSet<>();
		mathIndicatorSet.addAll(mathNodeSet);
		mathIndicatorSet.addAll(Arrays.asList(
				"times", "thrice", "triple", "twice", "double", "half"));
		
		// Variable Knowledge
		specialVarTokens = new HashSet<String>();
		specialVarTokens.addAll(Arrays.asList("one", "other", "another", "first", 
				"second", "larger", "smaller", "greater", "lesser", "x", "this", "itself", "he"));
	}
	
	public static List<String> extractMathTokens(Double threshold) throws Exception {
		Map<String, Integer> gigaWordCounts = new HashMap<String, Integer>();
		Map<String, Integer> mathWordCounts = new HashMap<String, Integer>();
		int gigaWordSize = 0, mathWordSize = 0;
		List<String> mathTokens = new ArrayList<String>();
		
		System.out.println("Going over gigaword");
		for(String fileName : Params.gigaWordFiles) {
			System.out.println("Reading file "+fileName);
			BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
			String line;
			while((line = br.readLine()) != null) {
				for(String token : line.split(" ")) {
					String tkn = token.toLowerCase();
					if(!gigaWordCounts.containsKey(tkn)) {
						gigaWordCounts.put(tkn, 0);
					}
					gigaWordCounts.put(tkn, gigaWordCounts.get(tkn)+1);
					gigaWordSize++;
				}
			}
			br.close();
		}
		
		System.out.println("Going over math problems");
		List<SimulProb> probList = DocReader.readSimulProbFromBratDir(Params.annotationDir);
		for(SimulProb prob : probList) {
			for(String token : prob.ta.getTokens()) {
				String tkn = token.toLowerCase();
				if(!mathWordCounts.containsKey(tkn)) {
					mathWordCounts.put(tkn, 0);
				}
				mathWordCounts.put(tkn, mathWordCounts.get(tkn)+1);
				mathWordSize++;
			}
		}
		
		for(String key : mathWordCounts.keySet()) {
			if(!gigaWordCounts.containsKey(key)) {
				mathTokens.add(key);
			} else {
				double probability = (mathWordCounts.get(key) * gigaWordSize) / 
						(gigaWordCounts.get(key) * mathWordSize); 
				if(probability > threshold) {
					mathTokens.add(key);
				}
			}
		}
		
		return mathTokens;
	}
	
	public static void main(String args[]) throws Exception {
		System.out.println(Arrays.asList(extractMathTokens(100.0)));
	}
	
	
	
}
