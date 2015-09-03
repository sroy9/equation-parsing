package equation;

import java.io.Serializable;

import structure.Equation;
import structure.SimulProb;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class EquationY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public Equation equation;
	
	public EquationY() {
		equation = new Equation();
	}
	
	public EquationY(EquationY other) {
		equation = new Equation(other.equation);
	}
	
	public EquationY(SimulProb prob) {
		equation = new Equation(prob.equation);
	}
	
	public static float getLoss(EquationY gold, EquationY pred) {
		return Equation.getLoss(gold.equation, pred.equation, false);
	}
	
	@Override
	public String toString() {
		return "Equation : "+equation;
	}
}