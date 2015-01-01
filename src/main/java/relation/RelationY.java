package relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import driver.FullSystem;
import semparse.SemY;
import structure.Equation;
import structure.EquationSolver;
import structure.SimulProb;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class RelationY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public List<String> relations;
	public List<SemY> equations;
	public boolean isOneVar;
	
	public RelationY() {
		relations = new ArrayList<>();
		equations = new ArrayList<>();
	}
	
	public RelationY(RelationY other) {
		relations = new ArrayList<>();
		for(String relation : other.relations) {
			relations.add(relation);
		}
		equations = new ArrayList<>();
		for(SemY eq : other.equations) {
			equations.add(new SemY(eq));
		}
		isOneVar = other.isOneVar;
	}
	
	public RelationY(SimulProb prob) {
		relations = new ArrayList<>();
//		for(String relation : prob.relations) {
//			relations.add(relation);
//		}
		equations = new ArrayList<>();
		for(Equation eq : prob.equations) {
			equations.add(new SemY(eq));
		}
		isOneVar = prob.isOneVar;
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
	
	public static float getEquationLoss(RelationY r1, RelationY r2) {
		if(r1.equations.size() != r2.equations.size()) return 10.0f;
		float loss = 0.0f;
		if(r1.equations.size() == 1) {
			loss += SemY.getLoss(r1.equations.get(0), r2.equations.get(0));
		}
		if(r1.equations.size() == 2) {
			loss += Math.min(
					SemY.getLoss(r1.equations.get(0), r2.equations.get(0)) + 
					SemY.getLoss(r1.equations.get(1), r2.equations.get(1)),
					SemY.getLoss(r1.equations.get(0), r2.equations.get(1)) + 
					SemY.getLoss(r1.equations.get(1), r2.equations.get(0)));
		}
		List<Double> soln1 = EquationSolver.solveSemYs(r1.equations);
		List<Double> soln2 = EquationSolver.solveSemYs(r2.equations);
		if(!FullSystem.hasSameSolution(soln1, soln2)) loss += 4.0f;
		return loss;
	}
	
	public static float getLoss(RelationY r1, RelationY r2) {
		assert r1.relations.size() == r2.relations.size();
		return getRelationLoss(r1, r2) + getEquationLoss(r1, r2);
	}
	
	@Override
	public String toString() {
		return ""+Arrays.asList(relations);
	}
}