package joint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import structure.Equation;
import structure.EquationSolver;
import structure.Node;
import structure.SimulProb;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class JointY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public List<Equation> equations;
	
	public JointY() {
		equations = new ArrayList<>();
	}
	
	public JointY(JointY other) {
		equations = new ArrayList<>();
		for(Equation eq : other.equations) {
			equations.add(new Equation(eq));
		}
	}
	
	public JointY(SimulProb prob) {
		equations = new ArrayList<>();
		for(Equation eq : prob.equations) {
			equations.add(eq);
		}
	}
	
	public static float getEquationLoss(JointY y1, JointY y2) {
		if(y1.equations.size() != y2.equations.size()) return 10.0f;
		if(y1.equations.size() == 1) return Equation.getLoss(
				y1.equations.get(0), y2.equations.get(0));
		if(y1.equations.size() == 2) {
			float loss1 = Equation.getLoss(y1.equations.get(0), y2.equations.get(0)) + 
					Equation.getLoss(y1.equations.get(1), y2.equations.get(1));
			float loss2 = Equation.getLoss(y1.equations.get(0), y2.equations.get(1)) + 
					Equation.getLoss(y1.equations.get(1), y2.equations.get(0));
			return Math.min(loss1, loss2);
		}
		return 10.0f;		
	}
	
	public static float getSolutionLoss(JointY y1, JointY y2) {
		if(EquationSolver.doesHaveSameSolution(y1.equations, y2.equations)) {
			return 0.0f;
		}
		return 5.0f;
	}
	
	public static float getLoss(JointY y1, JointY y2) {
		return getEquationLoss(y1, y2) + getSolutionLoss(y1, y2);
	}
	
	@Override
	public String toString() {
		return ""+Arrays.asList(equations);
	}
}