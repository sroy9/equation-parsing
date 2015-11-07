package lasttwo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import structure.Equation;
import structure.Node;
import structure.SimulProb;
import tree.TreeY;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class LastTwoY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public Equation equation;
	public Map<String, List<Integer>> varTokens;
	public boolean coref;
	List<Node> nodes;
	
	public LastTwoY() {
		equation = new Equation();  
		varTokens = new HashMap<String, List<Integer>>();
		coref = false;
		nodes = new ArrayList<>();
	}
	
	public LastTwoY(LastTwoY other) {
		equation = new Equation(other.equation);
		varTokens = other.varTokens;
		coref = other.coref;
		nodes = other.nodes;
	}
	
	public LastTwoY(SimulProb prob) {
		equation = new Equation(prob.equation);
		varTokens = prob.varTokens;
		coref = prob.coref;
		nodes = new ArrayList<>();
	}
	
	public LastTwoY(TreeY prob) {
		equation = new Equation(prob.equation);
		varTokens = prob.varTokens;
		coref = prob.coref;
		nodes = prob.nodes;
	}
	
	public static float getLoss(LastTwoY gold, LastTwoY pred) {
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
		return "VarTokens : "+Arrays.asList(varTokens)+"\nEquation : "+equation+"\nCoref : "+coref;
	}
}