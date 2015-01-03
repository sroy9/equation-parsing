package relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import learnInf.Driver;
import semparse.SemY;
import structure.Equation;
import structure.EquationSolver;
import structure.SimulProb;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class RelationY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public List<String> relations;
	public boolean isOneVar;
	
	public RelationY() {
		relations = new ArrayList<>();
	}
	
	public RelationY(RelationY other) {
		relations = new ArrayList<>();
		for(String relation : other.relations) {
			relations.add(relation);
		}
		isOneVar = other.isOneVar;
	}
	
	public RelationY(SimulProb prob) {
		relations = new ArrayList<>();
		for(String relation : prob.relations) {
			relations.add(relation);
		}
		isOneVar = prob.isOneVar;
	}
	
	public static float getLoss(RelationY r1, RelationY r2) {
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
	
	@Override
	public String toString() {
		return ""+Arrays.asList(relations);
	}
}