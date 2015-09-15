package struct.lca;

import java.io.Serializable;

import structure.Equation;
import structure.SimulProb;
import tree.TreeY;
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
	
	public LcaY(TreeY prob) {
		equation = new Equation(prob.equation);
		
	}
	
	public static float getLoss(LcaY gold, LcaY pred) {
		return Equation.getLoss(gold.equation, pred.equation, false);
	}
	
	@Override
	public String toString() {
		return "Equation : "+equation;
	}
}