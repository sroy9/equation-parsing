package semparse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import structure.Equation;
import structure.Operation;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
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
		for(int i=0; i<eq.terms.size(); ++i) {
			for(int j=0; j<eq.terms.get(i).size(); ++j) {
				emptySlots.add(new IntPair(i, j));
			}
		}
	}
	
	public SemY(SemY other) {
		super(other);
		emptySlots = new ArrayList<IntPair>();
		templateNo = other.templateNo;
		for(IntPair slot : other.emptySlots) {
			emptySlots.add(slot);
		}
	}
	
	public static float getLoss(SemY y1, SemY y2) {
		float loss1 = SemY.getLossOrderFixed(y1, y2);
		// copy made to compute the alternate loss
		SemY y11 = new SemY(y1);
		exchange(y11.terms);
		exchange(y11.operations);
		float loss2 = SemY.getLossOrderFixed(y11, y2);
		return Math.min(loss1, loss2);
	}
	
	public static <A> void exchange(List<A> l) {
		A tmp = l.get(0);
		l.set(0, l.get(2));
		l.set(2, tmp);
		tmp = l.get(1);
		l.set(1, l.get(3));
		l.set(3, tmp);
	}
	
	public static float getLossOrderFixed(SemY y1, SemY y2) {
		float loss = 0.0f;
		for(int i=0; i<5; i++) {
			List<Pair<Operation, Double>> pairList1 = y1.terms.get(i);
			List<Pair<Operation, Double>> pairList2 = y2.terms.get(i);
			if(pairList1.size() != pairList2.size()) loss += 4.0;
			else {
				for(int j=0; j<pairList1.size(); ++j) {
					Pair<Operation, Double> pair1 = pairList1.get(j);
					Pair<Operation, Double> pair2 = pairList2.get(j);
					if(!Tools.safeEquals(pair1.getSecond(), pair2.getSecond())) {
						loss += 1;
					}
					if(pair1.getFirst() != pair2.getFirst()) {
						loss += 1;
					}
				}
			}
		}
		for(int i=0; i<4; ++i) {
			if(y1.operations.get(i) != y2.operations.get(i)) {
				loss += 1;
			}
		}
		return loss;
	}
}