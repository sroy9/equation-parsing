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
		if(y1.isOneVar != y2.isOneVar) loss += 10.0;
		for(int i=0; i<5; i++) {
			List<Pair<Operation, Double>> pairList1 = y1.terms.get(i);
			List<Pair<Operation, Double>> pairList2 = y2.terms.get(i);
			if(pairList1.size() != pairList2.size()) loss += 4.0;
			else {
				loss += getLossPairLists(pairList1, pairList2);
			}
		}
		for(int i=0; i<4; ++i) {
			if(y1.operations.get(i) != y2.operations.get(i)) {
				loss += 1;
			}
		}
		return loss;
	}
	
	public static float getLossPairLists(List<Pair<Operation, Double>> pairList1,
			List<Pair<Operation, Double>> pairList2) {
		if(pairList1.size() == 0) {
			return 0.0f;
		}
		if(pairList1.size() == 1) {
			float loss = 0.0f;
			Pair<Operation, Double> pair1 = pairList1.get(0);
			Pair<Operation, Double> pair2 = pairList2.get(0);
			if(pair1.getFirst() != pair2.getFirst()) loss += 1.0;
			if(!Tools.safeEquals(pair1.getSecond(), pair2.getSecond())) loss += 1.0;
			return loss;
		} 
		if(pairList2.size() == 2) {
			float loss1 = 0.0f, loss2 = 0.0f;
			Pair<Operation, Double> pair1 = pairList1.get(0);
			Pair<Operation, Double> pair2 = pairList2.get(0);
			if(pair1.getFirst() != pair2.getFirst()) loss1 += 1.0;
			if(!Tools.safeEquals(pair1.getSecond(), pair2.getSecond())) loss1 += 1.0;
			pair1 = pairList1.get(1);
			pair2 = pairList2.get(1);
			if(pair1.getFirst() != pair2.getFirst()) loss1 += 1.0;
			if(!Tools.safeEquals(pair1.getSecond(), pair2.getSecond())) loss1 += 1.0;
			pair1 = pairList1.get(0);
			pair2 = pairList2.get(1);
			if(pair1.getFirst() != pair2.getFirst()) loss2 += 1.0;
			if(!Tools.safeEquals(pair1.getSecond(), pair2.getSecond())) loss2 += 1.0;
			pair1 = pairList1.get(1);
			pair2 = pairList2.get(0);
			if(pair1.getFirst() != pair2.getFirst()) loss2 += 1.0;
			if(!Tools.safeEquals(pair1.getSecond(), pair2.getSecond())) loss2 += 1.0;
			return Math.min(loss1, loss2);
		}
		System.err.println("Issue in getLossPairLists");
		return 0.0f;
	}
}