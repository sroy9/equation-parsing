package tree;

import java.io.Serializable;

import joint.JointY;
import structure.Equation;
import structure.SimulProb;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class TreeY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public Equation equation;
	
	public TreeY() {
		equation = new Equation();  
	}
	
	public TreeY(TreeY other) {
		equation = new Equation(other.equation);
	}
	
	public TreeY(SimulProb prob) {
		equation = new Equation(prob.equation);
	}
	
	public TreeY(JointY prob) {
		equation = new Equation(prob.equation);
	}
	
	public static float getLoss(TreeY gold, TreeY pred) {
		return Equation.getLoss(gold.equation, pred.equation, false);
	}
	
	@Override
	public String toString() {
		return "Equation : "+equation;
	}
}