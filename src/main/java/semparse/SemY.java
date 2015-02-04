package semparse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import structure.EqParse;
import structure.Equation;
import structure.SimulProb;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class SemY extends EqParse implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public List<IntPair> spans;
	
	public SemY() {
		super();
		spans = new ArrayList<>();
	}
	
	public SemY(SemY other) {
		super(other);
		spans = new ArrayList<>();
		spans.addAll(other.spans);
	}
	
	public SemY(SimulProb prob) {
		this();
		for(Pair<String, IntPair> pair : prob.eqParse.nodes) {
			boolean allow = true;
			for(Pair<String, IntPair> bigPair : prob.eqParse.nodes) {
				if(bigPair.getFirst().equals("EQ") && 
						bigPair.getSecond().getFirst()<=pair.getSecond().getFirst() &&
						pair.getSecond().getSecond()<=bigPair.getSecond().getSecond()) {
					allow = true;
					break;
				}
			}
			if(allow) {
				if(!Tools.isConstituentIndex(prob.chunks, pair.getSecond().getFirst()) || 
						!Tools.isConstituentIndex(prob.chunks, pair.getSecond().getSecond())) {
					System.out.println("Text : "+prob.ta.getText());
					System.out.println("Chunks : "+prob.chunks);
					System.out.println("Leaving out : "+pair);
					continue;
				}
				nodes.add(pair);
				if(pair.getFirst().equals("EQ")) {
					spans.add(pair.getSecond());
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