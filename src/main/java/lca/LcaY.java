package lca;

import java.io.Serializable;

import structure.Equation;
import structure.SimulProb;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class LcaY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public Equation equation;
	
	public LcaY() {
		equation = new Equation();
	}
	
	public LcaY(LcaY other) {
		equation = new Equation(other.equation);
	}
	
	public LcaY(SimulProb prob) {
		equation = new Equation(prob.equation);
	}
	
	public static float getLoss(LcaY gold, LcaY pred) {
		return Equation.getLoss(gold.equation, pred.equation, true);
	}
	
	@Override
	public String toString() {
		return "Equation : "+equation;
	}
}