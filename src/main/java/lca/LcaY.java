package lca;

import java.io.Serializable;

import structure.Node;
import structure.SimulProb;
import tree.TreeY;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class LcaY implements IStructure, Serializable {
	
	private static final long serialVersionUID = 2399969922362221136L;
	public String operation;
	
	public LcaY(String op) {
		operation = op;
	}
	
	public LcaY(LcaY other) {
		operation = other.operation;
	}
	
	public LcaY(SimulProb prob, Node leaf1, Node leaf2) {
		operation = prob.equation.root.findLabelofLCA(leaf1, leaf2);
	}
	
	public LcaY(TreeY prob, Node leaf1, Node leaf2) {
		operation = prob.equation.root.findLabelofLCA(leaf1, leaf2);
	}
	
	public static float getLoss(LcaY gold, LcaY pred) {
		if(gold.operation.equals(pred.operation)) return 0.0f;
		else return 1.0f;
	}
	
	@Override
	public String toString() {
		return operation;
	}
}