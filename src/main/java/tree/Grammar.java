package tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import structure.Node;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;

public class Grammar {

	public static TreeY mergeByRule(TreeX x, TreeY y) {
		List<Node> nodes = new ArrayList<>();
		nodes.addAll(y.nodes);
		for(Node node : nodes) {
			if(node.label.equals("NUM")) node.charIndex = 
					(x.quantities.get(node.index).start+x.quantities.get(node.index).end)/2;
			if(node.label.equals("VAR")) {
				int start = x.ta.getTokenCharacterOffset(x.candidateVars.get(node.index).getFirst()).getFirst();
				int end = x.ta.getTokenCharacterOffset(x.candidateVars.get(node.index).getSecond()-1).getSecond()-1;
				node.charIndex = (start+end)/2;
			}
		}
		// Sort the nodes based on charIndex
		Collections.sort(nodes, new Comparator<Node>() {
			@Override
			public int compare(Node o1, Node o2) {
				return Integer.compare(o1.charIndex, o2.charIndex);
			}
		});
		List<Node> nodeList = parse(x, nodes);
		for(Node node : nodeList) {
			if(node.label.equals("EQ")) {
				y.equation.root = node;
				return y;
			}
		}
		return null;
	}
	
	public static List<Node> parse(TreeX x, List<Node> leaves) {
		List<List<List<Node>>> cky = new ArrayList<List<List<Node>>>();
		int n = leaves.size();
		for(int i=0; i<=n; ++i) {
			cky.add(new ArrayList<List<Node>>());
			for(int j=0; j<=n; ++j) {
				cky.get(i).add(new ArrayList<Node>());
			}
		}
		for(int i=0; i<n; ++i) {
			cky.get(i).get(i+1).add(leaves.get(i));
		}
		for(int i=n-1; i>=0; --i) {
			for(int j=i+2; j<=n; ++j) {
				parse(x, i, j, cky, leaves);
			}
		}
		return cky.get(0).get(n);
	}
	
	public static void parse(TreeX x, int start, int end, 
			List<List<List<Node>>> cky,
			List<Node> leaves) {
		for(int i=start+1; i<end; ++i) {
			parse(x, start, i, end, cky, leaves);
		}
	}
	
	public static void parse(TreeX x, int start, int mid, int end, 
			List<List<List<Node>>> cky,
			List<Node> leaves) {
		String midPhrase = x.ta.getText().toLowerCase().substring(
				leaves.get(mid-1).charIndex, leaves.get(mid).charIndex);
		boolean verbInMidPhrase = false;
		for(int i=x.ta.getTokenIdFromCharacterOffset(leaves.get(mid-1).charIndex);
				i<x.ta.getTokenIdFromCharacterOffset(leaves.get(mid).charIndex);
				++i) {
			if(x.posTags.get(i).getLabel().startsWith("VB")) {
				verbInMidPhrase = true;
				break;
			}
		}
		String prePhrase = "";
		if(start == 0) prePhrase = x.ta.getText().toLowerCase().substring(
				0, leaves.get(start).charIndex);
		else prePhrase = x.ta.getText().toLowerCase().substring(
				leaves.get(start-1).charIndex, leaves.get(start).charIndex);
		String leftToken = "";
		if(start+1 == mid && leaves.get(start).label.equals("NUM")) {
			QuantSpan qs = x.quantities.get(leaves.get(start).index);
			leftToken = x.ta.getText().toLowerCase().substring(qs.start, qs.end);
		}
		for(Node node1 : cky.get(start).get(mid)) {
			for(Node node2 : cky.get(mid).get(end)) {
				if(leftToken.contains("thrice") || leftToken.contains("triple") ||
						leftToken.contains("double") || leftToken.contains("twice") ||
						leftToken.contains("half")) {
					Node node = new Node("MUL", -1, Arrays.asList(node1, node2));
					cky.get(start).get(end).add(node);
					continue;
				}
				if(prePhrase.contains("sum of") || midPhrase.contains("added to") || 
						midPhrase.contains("plus")) {
					Node node = new Node("ADD", -1, Arrays.asList(node1, node2));
					cky.get(start).get(end).add(node);
				}
				if(prePhrase.contains("difference of") || midPhrase.contains("exceeds") || 
						midPhrase.contains("subtracted") || midPhrase.contains("minus")) {
					Node node = new Node("SUB", -1, Arrays.asList(node1, node2));
					cky.get(start).get(end).add(node);
				}
				if(prePhrase.contains("product of") || midPhrase.contains("times") || 
						midPhrase.contains("multiplied by")) {
					Node node = new Node("MUL", -1, Arrays.asList(node1, node2));
					cky.get(start).get(end).add(node);
				}
				if(prePhrase.contains("ratio of") || midPhrase.contains("to")) {
					Node node = new Node("DIV", -1, Arrays.asList(node1, node2));
					cky.get(start).get(end).add(node);
				}
				if(start == 0 && end == leaves.size() && verbInMidPhrase) {
					Node node = new Node("EQ", -1, Arrays.asList(node1, node2));
					cky.get(start).get(end).add(node);
				}
			}
		}
	}
	
}
