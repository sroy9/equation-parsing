package structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;

public class QuantState {
	
	public String label;
	public Map<String, List<IntPair>> mentionLocMap;

	public QuantState(String label) {
		this.label = label;
		mentionLocMap = new HashMap<String, List<IntPair>>();
	}
	
	public void addToCluster(String mention, IntPair ip) {
		if(!mentionLocMap.containsKey(mention)) {
			mentionLocMap.put(mention, new ArrayList<IntPair>());
		}
		mentionLocMap.get(mention).add(ip);
	}
}
