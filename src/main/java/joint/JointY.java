package joint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import structure.Equation;
import structure.SimulProb;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class JointY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public List<String> relations;
	public List<Equation> equations;
	public boolean isOneVar;
	
	public JointY() {
		relations = new ArrayList<>();
		equations = new ArrayList<>();
	}
	
	public JointY(JointY other) {
		relations = new ArrayList<>();
		equations = new ArrayList<>();
		for(String relation : other.relations) {
			relations.add(relation);
		}
		for(Equation eq : other.equations) {
			equations.add(eq);
		}
		isOneVar = other.isOneVar;
	}
	
	public JointY(SimulProb prob) {
		relations = new ArrayList<>();
		equations = new ArrayList<>();
		for(String relation : prob.relations) {
			relations.add(relation);
		}
		for(Equation eq : prob.equations) {
			equations.add(eq);
		}
		isOneVar = prob.isOneVar;
	}
	
	public static float getLoss(JointY r1, JointY r2) {
		return 0.0f;
	}
	
	public static float getRelationLoss(JointY r1, JointY r2) {
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
	
	public static float getNumVarLoss(JointY r1, JointY r2) {
		if(r1.isOneVar == r2.isOneVar) return 0.0f;
		else return 10.0f;
	}
	
	@Override
	public String toString() {
		return ""+Arrays.asList(relations);
	}
	
	public static <A> void exchange(List<A> l) {
		A tmp = l.get(0);
		l.set(0, l.get(2));
		l.set(2, tmp);
		tmp = l.get(1);
		l.set(1, l.get(3));
		l.set(3, tmp);
	}
}