package structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import reader.DocReader;
import utils.Tools;

public class Node implements Serializable {
	
	private static final long serialVersionUID = -1127009463482561785L;
	public String label;
	public String varId; // V1 or V2
	public Double value;
	public List<Node> children;
	// For quantities : index of quantities list
	// For variables : index of candidate variables list
	public int index;
	
	public Node() {
		children = new ArrayList<>();
	}
	
	public Node(String label, int index, List<Node> children) {
		this.label = label;
		this.index = index;
		this.children = children;
	}
	
	public Node(Node other) {
		this();
		this.label = other.label;
		this.index = other.index;
		this.value = other.value;
		this.varId = other.varId;
		for(Node child : other.children) {
			this.children.add(new Node(child));			
		}
	}
	
	public String getLambdaExpression() {
		if(children.size() == 0) {
			if(DocReader.preds == null) DocReader.preds = new HashSet<>();
			DocReader.preds.add((label.equals("NUM") ? value : varId)+":n");
			return (label.equals("NUM") ? value : varId)+":n";
		}
		if(label.equals("ADD")) return "(add:<n,<n,n>> "+ children.get(0).getLambdaExpression()
				+ " " + children.get(1).getLambdaExpression() + ")";
		if(label.equals("SUB")) return "(sub:<n,<n,n>> "+ children.get(0).getLambdaExpression() 
				+ " " + children.get(1).getLambdaExpression() + ")";
		if(label.equals("MUL")) return "(mul:<n,<n,n>> "+ children.get(0).getLambdaExpression() 
				+ " " + children.get(1).getLambdaExpression() + ")";
		if(label.equals("DIV")) return "(div:<n,<n,n>> "+ children.get(0).getLambdaExpression() 
				+ " " + children.get(1).getLambdaExpression() + ")";
		if(label.equals("EQ")) return "(eq:<n,<n,t>> "+ children.get(0).getLambdaExpression() 
				+ " " + children.get(1).getLambdaExpression() + ")";
		return null;
	}
	
	@Override
	public String toString() {
		if(children.size() == 0) return label + "_" + 
				(label.equals("NUM") ? value : varId);
		return "("+children.get(0).toString() + " " + label + " " + 
				children.get(1).toString()+")";
	}
	
	public static float getLoss(Node node1, Node node2, boolean varNameSwap) {
//		System.out.println("NodeLoss called with "+node1+" and "+node2);
		if(node1.children.size() != node2.children.size()) return 4.0f;
		if(!node1.label.equals(node2.label)) return 4.0f;
		if(node1.children.size() == 0) {
			if(node1.label.equals("NUM") && !Tools.safeEquals(node1.value, node2.value)) {
				return 1.0f;
			}
			if(node1.label.equals("VAR")) {
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
		if(eqString.charAt(0)=='(' && eqString.charAt(eqString.length()-1)==')') {
			eqString = eqString.substring(1, eqString.length()-1);
		}
		index = indexOfMathOp(eqString, Arrays.asList('+', '-', '*', '/'));
		Node node = new Node();
		if(index > 0) {
			if(eqString.charAt(index) == '+') node.label = "ADD";
			else if(eqString.charAt(index) == '-') node.label = "SUB";
			else if(eqString.charAt(index) == '*') node.label = "MUL";
			else if(eqString.charAt(index) == '/') node.label = "DIV";
			else node.label = "ISSUE";
			node.children.add(parseNode(eqString.substring(0, index)));
			node.children.add(parseNode(eqString.substring(index+1)));
			return node;
		} else {
			if(eqString.contains("V")) {
				node.label = "VAR";
				node.varId = eqString.trim();
			} else {
				node.label = "NUM";
				node.value = Double.parseDouble(eqString.trim());
			}
		}
		return node;
	}
	
//	public static int indexOfMathOp(
//			String equationString, List<Character> keys) {
//		int index = -1;
//		for(Character key : keys) {
//			index = equationString.indexOf(key);
//			if(index >= 0) {
//				boolean inBracket = false;
//				for(int i=index; i>=0; --i) {
//					if(equationString.charAt(i) == ')') return index;
//					if(equationString.charAt(i) == '(') {
//						inBracket = true;
//						break; 
//					}
//				}
//				if(inBracket) continue;
//				return index;
//			}
//		}
//		return index;
//	}
	
	public static int indexOfMathOp(String equationString, List<Character> keys) {
		for(int index=0; index<equationString.length(); ++index) {
			if(keys.contains(equationString.charAt(index))) {
				int open = 0, close = 0;
				for(int i=index; i>=0; --i) {
					if(equationString.charAt(i) == ')') close++;
					if(equationString.charAt(i) == '(') open++;
				}
				if(open==close) {
					return index;
				}
			}
		}
		return -1;
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
	
	public boolean hasLeaf(Node leaf) {
		if(label.equals("VAR") && leaf.label.equals("VAR")) {
			if(varId.equals(leaf.varId)) return true;
		}
		if(label.equals("NUM") && leaf.label.equals("NUM")) {
			if(Tools.safeEquals(leaf.value, value)) return true;
		}
		if(label.equals("VAR") || label.equals("NUM")) return false;
		return children.get(0).hasLeaf(leaf) || children.get(1).hasLeaf(leaf);
	}
	
	public String findLabelofLCA(Node leaf1, Node leaf2) {
		String label = "NONE";
		int subtreeSize = 1000;
		boolean reverse = false;
		for(Node node : getAllSubNodes()) {
			if(node.children.size() == 0) continue;
			if(node.children.get(0).hasLeaf(leaf1) && node.children.get(1).hasLeaf(leaf2)
					&& node.getAllSubNodes().size() < subtreeSize) {
				label = node.label;
				subtreeSize = node.getAllSubNodes().size();
			}
			if(node.children.get(0).hasLeaf(leaf2) && node.children.get(1).hasLeaf(leaf1)
					&& node.getAllSubNodes().size() < subtreeSize) {
				label = node.label;
				subtreeSize = node.getAllSubNodes().size();
				reverse = true;
			}
		}
		if((label.equals("SUB") || label.equals("DIV")) && reverse) {
			return label+"_REV";
		}
		return label;
	}
	
	public String getSignature() {
		if(children.size() == 0) return label;
		return "("+children.get(0).getSignature() + " " + label + "_" +
				children.get(1).getSignature()+")";
	}
}
