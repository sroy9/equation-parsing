package structure;

import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;

public class Node {
	
	public String label;
	public IntPair span;
	public List<Node> children;
	public String eqString;
	
	public Node(String label, IntPair span, List<Node> children) {
		this.label = label;
		this.span = span;
		this.children = children;
		this.eqString = "";
	}
	
	@Override
	public String toString() {
		return "("+label+", "+span+")";
	}
	
}
