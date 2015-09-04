package numoccur;

import java.io.Serializable;

import structure.Node;
import structure.SimulProb;
import tree.TreeX;
import tree.TreeY;
import utils.Tools;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class NumoccurY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public int numOccur;
	
	public NumoccurY(int n) {
		numOccur = n;
	}
	
	public NumoccurY(NumoccurY other) {
		numOccur = other.numOccur;
	}
	
	public NumoccurY(SimulProb prob, int quantIndex) {
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
	
	public NumoccurY(TreeX x, TreeY y, int quantIndex) {
		numOccur = 0;
		for(Node leaf : y.equation.root.getLeaves()) {
			if(leaf.label.equals("NUM") && Tools.safeEquals(
					leaf.value, Tools.getValue(x.quantities.get(quantIndex)))) {
				numOccur++;
			}
		}
	}
	
	public static float getLoss(NumoccurY gold, NumoccurY pred) {
		if(gold.numOccur == pred.numOccur) return 0.0f;
		else return 1.0f;
	}
	
	@Override
	public String toString() {
		return ""+numOccur;
	}
}