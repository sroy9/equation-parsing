package tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import structure.Equation;
import structure.Node;
import structure.SimulProb;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class TreeY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public Equation equation;
	public Map<String, List<Integer>> varTokens;
	public List<Node> nodes;
	
	public TreeY() {
		equation = new Equation();
		nodes = new ArrayList<>();
		varTokens = new HashMap<String, List<Integer>>();
	}
	
	public TreeY(TreeY other) {
		equation = new Equation(other.equation);
		nodes = new ArrayList<>();
		nodes.addAll(other.nodes);
		varTokens = new HashMap<String, List<Integer>>();
		varTokens.putAll(other.varTokens);
	}
	
	public TreeY(SimulProb prob) {
		equation = new Equation(prob.equation);
		nodes = new ArrayList<>();
		varTokens = new HashMap<String, List<Integer>>();
		varTokens.putAll(prob.varTokens);
	}
	
	public static float getLoss(TreeY gold, TreeY pred) {
		if(pred.varTokens.get("V1").size() > 1 || 
				(pred.varTokens.containsKey("V2") && pred.varTokens.get("V2").size() > 1)) {
			System.err.println("Error in TreeY getLoss() function");
		}
		float loss1 = Equation.getLoss(gold.equation, pred.equation, true) + 
				SimulProb.getVarTokenLoss(gold.varTokens, pred.varTokens, true);
		float loss2 = Equation.getLoss(gold.equation, pred.equation, false) + 
				SimulProb.getVarTokenLoss(gold.varTokens, pred.varTokens, false);
		return Math.min(loss1, loss2);
	}
	
	@Override
	public String toString() {
		return ""+equation;
	}
}