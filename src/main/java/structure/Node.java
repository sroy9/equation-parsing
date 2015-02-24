package structure;

import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;

public class Node {
	
	public String label;
	public int index;
	public List<Node> children;
	public String eqString;
	
	public Node(String label, int index, List<Node> children) {
		this.label = label;
		this.index = index;
		this.children = children;
		this.eqString = "";
	}
	
	@Override
	public String toString() {
		return "("+label+", "+index+")";
	}
	
}
