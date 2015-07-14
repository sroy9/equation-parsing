package structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;

public class Node implements Serializable {
	
	private static final long serialVersionUID = -1127009463482561785L;
	public String label;
	public String varId;
	public Double value;
	public IntPair span;
	public List<Node> children;
	public int tokenIndex;
	
	public Node() {
		children = new ArrayList<>();
	}
	
	public Node(String label, int index, List<Node> children) {
		this.label = label;
		this.tokenIndex = index;
		this.children = children;
	}
	
	public Node(Node other) {
		this();
		this.label = other.label;
		this.span = other.span;
		this.tokenIndex = other.tokenIndex;
		this.value = other.value;
		this.varId = other.varId;
		for(Node child : other.children) {
			this.children.add(new Node(child));			
		}
	}
	
	public String getLambdaExpression() {
		if(children.size() == 0) return (label.equals("NUM") ? value : varId)+":n";
		if(label.equals("ADD")) return "(add:<n,<n,n>> "+ children.get(0).toString() 
				+ " " + children.get(1).toString() + ")";
		if(label.equals("SUB")) return "(sub:<n,<n,n>> "+ children.get(0).toString() 
				+ " " + children.get(1).toString() + ")";
		if(label.equals("MUL")) return "(mul:<n,<n,n>> "+ children.get(0).toString() 
				+ " " + children.get(1).toString() + ")";
		if(label.equals("DIV")) return "(div:<n,<n,n>> "+ children.get(0).toString() 
				+ " " + children.get(1).toString() + ")";
		if(label.equals("EQ")) return "(eq:<n,<n,t>> "+ children.get(0).toString() 
				+ " " + children.get(1).toString() + ")";
		return null;
	}
	
	@Override
	public String toString() {
		if(children.size() == 0) return label + "_" + 
				(label.equals("NUM") ? value : varId);
		return children.get(0).toString() + " " + label + " " + 
				children.get(1).toString();
	}
	
	public static float getLoss(Node node1, Node node2, boolean varNameSwap) {
//		System.out.println("NodeLoss called with "+node1+" and "+node2);
		if(node1.children.size() != node2.children.size()) return 4.0f;
		if(!node1.label.equals(node2.label)) return 4.0f;
		if(node1.children.size() == 0) {
			if(node1.label == "NUM" && !Tools.safeEquals(node1.value, node2.value)) {
				return 1.0f;
			}
			if(node1.label == "VAR") {
				if(!varNameSwap && !node1.varId.equals(node2.varId)) return 1.0f;
				if(varNameSwap && node1.varId.equals(node2.varId)) return 1.0f;
			}
		} else {
			if(node1.label.equals("SUB") || node1.label.equals("DIV")) {
				return getLoss(node1.children.get(0), node2.children.get(0), varNameSwap) + 
						getLoss(node1.children.get(1), node2.children.get(1), varNameSwap);
			} else {
				return Math.min(
					getLoss(node1.children.get(0), node2.children.get(0), varNameSwap) + 
					getLoss(node1.children.get(1), node2.children.get(1), varNameSwap), 
					getLoss(node1.children.get(0), node2.children.get(1), varNameSwap) +
					getLoss(node1.children.get(1), node2.children.get(0), varNameSwap));
			}
		}
		return 0.0f;
	}
	
	public static Node parseNode(String eqString) {
		eqString = eqString.trim();
//		System.out.println("EqString : "+eqString);
		int index = eqString.indexOf("=");
		if(index != -1) {
			Node node = new Node();
			node.label = "EQ";
			node.children.add(parseNode(eqString.substring(0, index)));
			node.children.add(parseNode(eqString.substring(index+1)));
			return node;
		}
		int loc = -1;
		while(true) {
			index = indexOfMathOp(eqString, Arrays.asList('+','-'), loc+1);
			if(index == -1) break;
			if(eqString.charAt(index) != '-' || eqString.charAt(index-1) != '(') break;
			loc = index+1;
		}
		if(index != -1) {
			Node node = new Node();
			if(eqString.charAt(index) == '+') node.label = "ADD";
			else node.label = "SUB";
			node.children.add(parseNode(eqString.substring(0, index)));
			node.children.add(parseNode(eqString.substring(index+1)));
			return node;
		}
		index = indexOfMathOp(eqString, Arrays.asList('*','/'), 0);
		if(index != -1) {
			Node node = new Node();
			if(eqString.charAt(index) == '*') node.label = "MUL";
			else node.label = "DIV";
			node.children.add(parseNode(eqString.substring(0, index)));
			node.children.add(parseNode(eqString.substring(index+1)));
			return node;
		}
		Node node = new Node();
		if(eqString.contains("V")) {
			node.label = "VAR";
			node.varId = eqString.trim();
		} else {
			node.label = "NUM";
			node.value = Double.parseDouble(eqString.replaceAll("\\(|\\)", ""));
		}
		return node;
	}
	
	public static int indexOfMathOp(
			String equationString, List<Character> keys, int start) {
		int finalIndex = -1, index;
		for(Character key : keys) {
			index = equationString.indexOf(key, start);
			if(index >= 0 && finalIndex >= 0 && index < finalIndex) {
				finalIndex = index;
			}
			if(finalIndex < 0 && index >= 0) {
				finalIndex = index;
			}
		}
		return finalIndex;
	}
	
	public List<Node> getLeaves() {
		List<Node> leaves = new ArrayList<Node>();
		if(children.size() == 0) {
			leaves.add(this);
		} else {
			leaves.addAll(children.get(0).getLeaves());
			leaves.addAll(children.get(1).getLeaves());
		}
		return leaves;
	}
	
	public IntPair getSpanningTokenIndices() {
		List<Node> leaves = getLeaves();
		int min = 1000, max = -1;
		for(Node leaf : leaves) {
			if(leaf.tokenIndex > max) max = leaf.tokenIndex;
			if(leaf.tokenIndex < min) min = leaf.tokenIndex;
		}
		return new IntPair(min, max);
	}
	
	public List<Node> getAllSubNodes() {
		List<Node> all = new ArrayList<Node>();
		all.add(this);
		if(children.size() == 2) {
			all.addAll(children.get(0).getAllSubNodes());
			all.addAll(children.get(1).getAllSubNodes());
		}
		return all;
	}
	
	public boolean hasVariable() {
		if(label.equals("VAR")) return true;
		else if(label.equals("NUM")) return false;
		else return children.get(0).hasVariable() || children.get(1).hasVariable();
	}
}
