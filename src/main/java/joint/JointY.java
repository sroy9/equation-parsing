package joint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import structure.Equation;
import structure.SimulProb;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class JointY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public List<Equation> equations;
	public boolean isOneVar;
	
	public JointY() {
		equations = new ArrayList<>();
	}
	
	public JointY(JointY other) {
		equations = new ArrayList<>();
		for(Equation eq : other.equations) {
			equations.add(new Equation(eq));
		}
		isOneVar = other.isOneVar;
	}
	
	public JointY(SimulProb prob) {
		equations = new ArrayList<>();
		for(Equation eq : prob.equations) {
			equations.add(eq);
		}
		isOneVar = prob.isOneVar;
	}
	
	public static float getLoss(JointY r1, JointY r2) {
		return 0.0f;
	}
	
	@Override
	public String toString() {
		return ""+Arrays.asList(equations);
	}
}