package lasttwo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import joint.JointY;
import structure.Equation;
import structure.Node;
import structure.SimulProb;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class LasttwoY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public Equation equation;
	public Map<String, List<Integer>> varTokens;
	public boolean coref;
	public List<Node> nodes;
	public double varScore;
	
	public LasttwoY() {
		equation = new Equation();
		varTokens = new HashMap<String, List<Integer>>();
		nodes = new ArrayList<Node>();
	}
	
	public LasttwoY(LasttwoY other) {
		equation = new Equation(other.equation);
		varTokens = new HashMap<String, List<Integer>>();
		for(String key : other.varTokens.keySet()) {
			varTokens.put(key, new ArrayList<Integer>());
			varTokens.get(key).addAll(other.varTokens.get(key));
		}
		coref = other.coref;
		nodes = new ArrayList<Node>();
		for(Node node : other.nodes) {
			nodes.add(new Node(node));
		}
		varScore = other.varScore;
	}
	
	public LasttwoY(SimulProb prob) {
		equation = new Equation(prob.equation);
		varTokens = prob.varTokens;
		coref = prob.coref;
		nodes = new ArrayList<Node>();
	}
	
	public LasttwoY(JointY prob) {
		equation = new Equation(prob.equation);
		varTokens = new HashMap<String, List<Integer>>();
		for(String key : prob.varTokens.keySet()) {
			varTokens.put(key, new ArrayList<Integer>());
			varTokens.get(key).addAll(prob.varTokens.get(key));
		}
		coref = prob.coref;
		nodes = new ArrayList<Node>();
		for(Node node : prob.nodes) {
			nodes.add(new Node(node));
		}
	}
	
	public static float getLoss(LasttwoY gold, LasttwoY pred) {
		if(pred.varTokens.get("V1").size() > 1 || 
				(pred.varTokens.containsKey("V2") && pred.varTokens.get("V2").size() > 1)) {
			System.err.println("Error in TreeY getLoss() function");
		}
		float loss1 = 
				Equation.getLoss(gold.equation, pred.equation, true) + 
				SimulProb.getVarTokenLoss(gold.varTokens, gold.coref, 
						pred.varTokens, pred.coref, true);
		float loss2 = 
				Equation.getLoss(gold.equation, pred.equation, false) + 
				SimulProb.getVarTokenLoss(gold.varTokens, gold.coref, 
						pred.varTokens, pred.coref, false);
		return Math.min(loss1, loss2);
	}
	
	@Override
	public String toString() {
		return "VarTokens : "+Arrays.asList(varTokens)+" Equation : "+equation;
	}
}