package structure;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	
}
