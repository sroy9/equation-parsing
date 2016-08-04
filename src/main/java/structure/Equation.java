package structure;

import java.io.Serializable;
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
	
	public String getLambdaExpression() {
		return root.getLambdaExpression();
	}
	
	public String toString() {
		return root.toString();
	}
	
	public static float getLoss(Equation y1, Equation y2, boolean varNameSwap) {
		return Node.getLoss(y1.root, y2.root, varNameSwap);
	}

}
