package tree;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import joint.JointY;
import lasttwo.LasttwoY;
import structure.Equation;
import structure.SimulProb;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class TreeY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public Equation equation;
	public Map<String, List<Integer>> varTokens;
	
	public TreeY() {
		equation = new Equation();
		varTokens = new HashMap<String, List<Integer>>();
	}
	
	public TreeY(TreeY other) {
		equation = new Equation(other.equation);
		varTokens = other.varTokens;
	}
	
	public TreeY(SimulProb prob) {
		equation = new Equation(prob.equation);
		varTokens = prob.varTokens;
	}
	
	public TreeY(JointY prob) {
		equation = new Equation(prob.equation);
		varTokens = prob.varTokens;
	}
	
	public TreeY(LasttwoY prob) {
		equation = new Equation(prob.equation);
		varTokens = prob.varTokens;
	}
	
	public static float getLoss(TreeY gold, TreeY pred) {
		if(pred.varTokens.get("V1").size() > 1 || 
				(pred.varTokens.containsKey("V2") && pred.varTokens.get("V2").size() > 1)) {
			System.err.println("Error in TreeY getLoss() function");
		}
		float loss1 = 
				Equation.getLoss(gold.equation, pred.equation, true) + 
				SimulProb.getVarTokenLoss(gold.varTokens, pred.varTokens, true);
		float loss2 = 
				Equation.getLoss(gold.equation, pred.equation, false) + 
				SimulProb.getVarTokenLoss(gold.varTokens, pred.varTokens, false);
		return Math.min(loss1, loss2);
	}
	
	@Override
	public String toString() {
		return "Equation : "+equation;
	}
}