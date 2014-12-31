package relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import driver.FullSystem;
import structure.Equation;
import structure.EquationSolver;
import structure.SimulProb;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class RelationY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public List<String> relations;
	public List<Equation> equations;
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
		for(Equation eq : other.equations) {
			equations.add(new Equation(eq));
		}
		isOneVar = other.isOneVar;
	}
	
	public RelationY(SimulProb prob) {
		relations = new ArrayList<>();
		for(String relation : prob.relations) {
			relations.add(relation);
		}
		equations = new ArrayList<>();
		for(Equation eq : prob.equations) {
			equations.add(new Equation(eq));
		}
		isOneVar = prob.isOneVar;
	}
	
	public static float getLoss(RelationY r1, RelationY r2) {
		assert r1.relations.size() == r2.relations.size();
		float loss = 0.0f;
		for(int i=0; i<r1.relations.size(); ++i) {
			if(!r1.relations.get(i).equals(r2.relations.get(i))) {
				loss+=1.0;
			}
		}
		List<Double> soln1 = EquationSolver.solve(r1.equations);
		List<Double> soln2 = EquationSolver.solve(r2.equations);
		if(!FullSystem.hasSameSolution(soln1, soln2)) loss += 10.0;
		return loss;
	}
	
	@Override
	public String toString() {
		return ""+Arrays.asList(relations);
	}
}