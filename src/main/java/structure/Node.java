package structure;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;

public class Node {
	
	public String label;
	public IntPair span;
	public List<Node> children;
	public int tokenIndex;
	
	public Node() {
		children = new ArrayList<>();
	}
	
	public Node(String label, int index, IntPair span, List<Node> children) {
		this.label = label;
		this.span = span;
		this.tokenIndex = index;
		this.children = children;
	}
	
	public Node(Node other) {
		this.label = other.label;
		this.span = other.span;
		this.tokenIndex = other.tokenIndex;
		for(Node child : other.children) {
			this.children.add(new Node(child));			
		}
	}
	
	@Override
	public String toString() {
		if(children.size() == 0) return label;
		return children.get(0).toString() + " " + label + " " + 
				children.get(1).toString();
	}
	
	public static float getLoss(Node node1, Node node2) {
		if(node1.children.size() != node2.children.size()) return 4.0f;
		float loss = 0.0f;
		if(node1.children.size() == 0) {
			loss += 1.0;
		} else {
			if(!node1.label.equals(node2.label)) loss += 1.0;
			loss += Math.min(
					getLoss(node1.children.get(0), node2.children.get(0)) + 
					getLoss(node1.children.get(1), node2.children.get(1)), 
					getLoss(node1.children.get(0), node2.children.get(1)) +
					getLoss(node1.children.get(1), node2.children.get(0)));
		}
		return loss;
	}
	
	public static Node parseNode(String eqString) {
		eqString = eqString.trim();
		int index = eqString.indexOf("=");
		if(index != -1) {
			Node node = new Node();
			node.label = "EQ";
			node.children.add(parseNode(eqString.substring(0, index)));
			node.children.add(parseNode(eqString.substring(index+1)));
			return node;
		}
		index = eqString.indexOf("*|/");
		if(index != -1) {
			Node node = new Node();
			if(eqString.charAt(index) == '*') node.label = "MUL";
			else node.label = "DIV";
			node.children.add(parseNode(eqString.substring(0, index)));
			node.children.add(parseNode(eqString.substring(index+1)));
			return node;
		}
		while(true) {
			index = eqString.indexOf("+|-");
			if(index == -1) break;
			if(eqString.charAt(index) != '-' || eqString.charAt(index-1) != '(') break;
		}
		if(index != -1) {
			Node node = new Node();
			if(eqString.charAt(index) == '+') node.label = "ADD";
			else node.label = "SUB";
			node.children.add(parseNode(eqString.substring(0, index)));
			node.children.add(parseNode(eqString.substring(index+1)));
			return node;
		}
		Node node = new Node();
		if(eqString.contains("V")) {
			node.label = "VAR";
		} else {
			node.label = "NUM";
		}
		return node;
	}
	
}
