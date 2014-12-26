package semparse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import structure.Equation;
import structure.Operation;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class SemY extends Equation implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	List<IntPair> emptySlots;
	int templateNo;
	
	public SemY() {
		super();
		emptySlots = new ArrayList<IntPair>();
	}
	
	public SemY(Equation eq) {
		super(eq);
		emptySlots = new ArrayList<IntPair>();
	}
	
	public SemY(SemY other) {
		super(other);
		emptySlots = new ArrayList<IntPair>();
		templateNo = other.templateNo;
		for(IntPair slot : emptySlots) {
			emptySlots.add(slot);
		}
	}
	
	public static float getLoss(SemY y1, SemY y2) {
		assert y1.templateNo == y2.templateNo;
		float loss = 0.0f;
		for(int i=0; i<y1.terms.size(); i++) {
			List<Pair<Operation, Double>> pairList = y1.terms.get(i);
			for(int j=0; j<pairList.size(); ++j) {
				Pair<Operation, Double> pair1 = pairList.get(j);
				Pair<Operation, Double> pair2 = y2.terms.get(i).get(j);
				if(!Tools.safeEquals(pair1.getSecond(), pair2.getSecond())) {
					loss += 1;
				}
			}
		}
		return loss;
	}
}