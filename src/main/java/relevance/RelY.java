package relevance;

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

public class RelY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public int decision;
	
	public RelY(int n) {
		decision = n;
	}
	
	public RelY(RelY other) {
		decision = other.decision;
	}
	
	public RelY(SimulProb prob, int quantIndex) {
		decision = 0;
		for(Node leaf : prob.equation.root.getLeaves()) {
			if(leaf.label.equals("NUM") && Tools.safeEquals(
					leaf.value, Tools.getValue(prob.quantities.get(quantIndex)))) {
				decision = 1;
				break;
			}
		}
	}
	
	public static float getLoss(RelY gold, RelY pred) {
		if(gold.decision == pred.decision) return 0.0f;
		else return 1.0f;
	}
	
	@Override
	public String toString() {
		return ""+decision;
	}
}