package structure;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;

public class Node {
	
	public String label;
	public IntPair span;
	
	public Node(String label, IntPair span) {
		this.label = label;
		this.span = span;
	}
	
}
