package semparse;

import java.io.Serializable;

import structure.EqParse;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class SemY extends EqParse implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	
	public SemY() {
		super();
	}
	
	public SemY(SemY other) {
		super(other);
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