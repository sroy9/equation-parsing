package structure;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;

/**
 * Class to read brat annotations from file
 * @author sroy9
 *
 */
public class Span {
	
	public IntPair ip;
	public String label; 
	
	public Span(String label, IntPair ip) {
		this.label = label;
		this.ip = ip;
	}
	
	@Override
	public String toString() {
		return "[" + label + " " + ip+"] ";
	}
 }
