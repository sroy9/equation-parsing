package lca;

import java.io.Serializable;

import structure.Node;
import structure.SimulProb;
import utils.Tools;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class LcaY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public int numOccur;
	
	public LcaY(int n) {
		numOccur = n;
	}
	
	public LcaY(LcaY other) {
		numOccur = other.numOccur;
	}
	
	public LcaY(SimulProb prob, int quantIndex) {
		numOccur = 0;
		for(Node leaf : prob.equation.root.getLeaves()) {
			if(leaf.label.equals("NUM") && Tools.safeEquals(
					leaf.value, Tools.getValue(prob.quantities.get(quantIndex)))) {
				numOccur++;
			}
		}
		if(numOccur>2) {
			System.err.println("Issue here NumOccur > 2 : "+prob.index+" : "+prob.text);
		}
	}
	
	public static float getLoss(LcaY gold, LcaY pred) {
		if(gold.numOccur == pred.numOccur) return 0.0f;
		else return 1.0f;
	}
	
	@Override
	public String toString() {
		return ""+numOccur;
	}
}