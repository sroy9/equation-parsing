package structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;

public class Node {
	
	public String label;
	public String varId;
	public Double value;
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
		if(children.size() == 0) return label + "_" + (label.equals("NUM") ? value : varId);
		return children.get(0).toString() + " " + label + " " + 
				children.get(1).toString();
	}
	
	public static float getLoss(Node node1, Node node2, boolean varNameSwap) {
		if(node1.children.size() != node2.children.size()) return 4.0f;
		if(!node1.label.equals(node2.label)) return 4.0f;
		if(node1.children.size() == 0) {
			if(node1.label == "NUM" && !Tools.safeEquals(node1.value, node2.value)) return 1.0f;
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
		System.out.println("EqString : "+eqString);
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
	
	public static int indexOfMathOp(String equationString, List<Character> keys, int start) {
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
}
