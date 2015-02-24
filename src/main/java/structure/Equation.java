package structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import utils.Tools;

// Holds canonical equations
// Ax +/- By +/- C = 0
// where A = \product A_i, B = \product B_i and C = \product C_i
// and A_i's, B_i's and C_i's are present in text

public class Equation implements Serializable {
	
	private static final long serialVersionUID = -1593105262537880720L;
	public Node root;
	
	public Equation() {
		root = new Node();
	}
	
	public Equation(Equation eq) {
		root = new Node(eq.root);
	}
	
	// Assumes no bracketed addition 2*(x+y)
	public Equation(String eqString) {
		root = Node.parseNode(eqString);
	}
	
	public String toString() {
		return root.toString();
	}
	
	public static float getLoss(Equation y1, Equation y2) {
		return Node.getLoss(y1.root, y2.root);
	}

}
