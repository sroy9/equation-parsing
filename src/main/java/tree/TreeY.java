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
	
	public TreeY() {
		equation = new Equation();
		varTokens = new HashMap<String, List<Integer>>();
	}
	
	public TreeY(TreeY other) {
		equation = new Equation(other.equation);
		varTokens = new HashMap<String, List<Integer>>();
		varTokens.putAll(other.varTokens);
	}
	
	public TreeY(SimulProb prob) {
		equation = new Equation(prob.equation);
		varTokens = new HashMap<String, List<Integer>>();
		varTokens.putAll(m);
	}
	
	public static float getLoss(TreeY y1, TreeY y2) {
		return Equation.getLoss(y1.equation, y2.equation);
	}
	
	@Override
	public String toString() {
		return ""+equation;
	}
}