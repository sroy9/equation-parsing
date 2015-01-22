package relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import structure.SimulProb;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class RelationY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public List<String> relations;
	public List<IntPair> eqSpans;
	public boolean isOneVar;
	
	public RelationY() {
		relations = new ArrayList<>();
		eqSpans = new ArrayList<>();
	}
	
	public RelationY(RelationY other) {
		relations = new ArrayList<>();
		eqSpans = new ArrayList<>();
		for(String relation : other.relations) {
			relations.add(relation);
		}
		for(IntPair eqSpan : other.eqSpans) {
			eqSpans.add(eqSpan);
		}
		isOneVar = other.isOneVar;
	}
	
	public RelationY(SimulProb prob) {
		relations = new ArrayList<>();
		eqSpans = new ArrayList<>();
		for(String relation : prob.relations) {
			relations.add(relation);
		}
		for(Pair<String, IntPair> pair : prob.eqParse.nodes) {
			if(pair.getFirst().equals("EQ")) {
				eqSpans.add(pair.getSecond());
			}
		}
		isOneVar = prob.isOneVar;
	}
	
	public static float getLoss(RelationY r1, RelationY r2) {
		return getNumVarLoss(r1, r2) + getRelationLoss(r1, r2) + getEqSpanLoss(r1, r2);
	}
	
	public static float getRelationLoss(RelationY r1, RelationY r2) {
		assert r1.relations.size() == r2.relations.size();
		float loss1 = 0.0f, loss2 = 0.0f;
		for(int i=0; i<r1.relations.size(); ++i) {
			String relation1 = r1.relations.get(i);
			String relation2 = r2.relations.get(i);
			if(relation1.equals("BOTH") || relation1.equals("NONE")) {
				if(!relation1.equals(relation2)) {
					loss1 += 1.0;
					loss2 += 1.0;
				}
 			} else if((relation1.equals("R1") && !relation2.equals("R1")) ||
 					(relation1.equals("R2") && !relation2.equals("R2"))){
 				loss1 += 1.0;
 			} else if((relation1.equals("R1") && !relation2.equals("R2")) ||
 					(relation1.equals("R2") && !relation2.equals("R1"))){
 				loss2 += 1.0;
 			} 
		}
		return Math.min(loss1, loss2);
	}
	
	public static float getNumVarLoss(RelationY r1, RelationY r2) {
		if(r1.isOneVar == r2.isOneVar) return 0.0f;
		else return 10.0f;
	}
	
	public static float getEqSpanLoss(RelationY r1, RelationY r2) {
		float loss = 0.0f;
		for(IntPair ip1 : r1.eqSpans) {
			boolean found = false;
			for(IntPair ip2 : r2.eqSpans) {
				if(ip1.equals(ip2)) {
					found = true;
					break;
				}
			}
			if(!found) loss += 1.0;
		}
		for(IntPair ip1 : r2.eqSpans) {
			boolean found = false;
			for(IntPair ip2 : r1.eqSpans) {
				if(ip1.equals(ip2)) {
					found = true;
					break;
				}
			}
			if(!found) loss += 1.0;
		}
		return loss;
	}
	
	@Override
	public String toString() {
		return ""+Arrays.asList(relations);
	}
}