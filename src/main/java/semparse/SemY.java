package semparse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;

import structure.Equation;
import structure.SimulProb;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class SemY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public Map<Integer, String> partitions;
	public List<Pair<String, IntPair>> nodes;
	public List<IntPair> spans;
	
	public SemY() {
		spans = new ArrayList<>();
		partitions = new HashMap<>();
	}
	
	public SemY(SemY other) {
		spans = new ArrayList<>();
		spans.addAll(other.spans);
		partitions = new HashMap<>();
		partitions.putAll(other.partitions);
	}
	
	public SemY(SimulProb prob) {
		this();
		for(Pair<String, IntPair> pair : prob.nodes) {
			boolean allow = true;
			for(Pair<String, IntPair> bigPair : prob.nodes) {
				if(bigPair.getFirst().equals("EQ") && 
						bigPair.getSecond().getFirst()<=pair.getSecond().getFirst() &&
						pair.getSecond().getSecond()<=bigPair.getSecond().getSecond()) {
					allow = true;
					break;
				}
			}
			if(allow) {
				nodes.add(pair);
				partitions.put(pair.getSecond().getFirst(), "B-PART");
				if(pair.getFirst().equals("EQ")) {
					spans.add(pair.getSecond());
					for(int i=pair.getSecond().getFirst(); 
							i<pair.getSecond().getSecond(); ++i) {
						if(!partitions.containsKey(i)) {
							partitions.put(i, "I-PART");
						}
					}
				}
			}
		}
	}
	
	public static float getLoss(SemY y1, SemY y2) {
		float loss = 0.0f;
		for(Pair<String, IntPair> pair1 : y1.nodes) {
			boolean found = false;
			for(Pair<String, IntPair> pair2 : y2.nodes) {
				if(pair1.getFirst().equals(pair2.getFirst()) && 
						pair1.getSecond().equals(pair2.getSecond())) {
					found = true;
					break;
				}
			}
			if(!found) loss += 1.0;
		}
		for(Pair<String, IntPair> pair1 : y2.nodes) {
			boolean found = false;
			for(Pair<String, IntPair> pair2 : y1.nodes) {
				if(pair1.getFirst().equals(pair2.getFirst()) && 
						pair1.getSecond().equals(pair2.getSecond())) {
					found = true;
					break;
				}
			}
			if(!found) loss += 1.0;
		}
		return loss;
	}
}