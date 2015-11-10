package tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import structure.Equation;
import structure.Node;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;

public class Grammar {

	public static void populateAndSortByCharIndex(List<Node> nodes, TextAnnotation ta, 
			List<QuantSpan> quantities, List<IntPair> candidateVars) {
		for(Node node : nodes) {
			if(node.label.equals("NUM")) node.charIndex = 
					(quantities.get(node.index).start+quantities.get(node.index).end)/2;
			if(node.label.equals("VAR")) {
				int start = ta.getTokenCharacterOffset(candidateVars.get(node.index).getFirst()).getFirst();
				int end = ta.getTokenCharacterOffset(candidateVars.get(node.index).getSecond()-1).getSecond()-1;
				node.charIndex = (start+end)/2;
			}
		}
		Collections.sort(nodes, new Comparator<Node>() {
			@Override
			public int compare(Node o1, Node o2) {
				return Integer.compare(o1.charIndex, o2.charIndex);
			}
		});
	}
	
	public static Equation mergeByRule(TextAnnotation ta, List<Constituent> posTags, 
			List<QuantSpan> quantities, List<IntPair> candidateVars, List<Node> leaves) {
		List<Node> nodes = new ArrayList<>();
		nodes.addAll(leaves);
		populateAndSortByCharIndex(nodes, ta, quantities, candidateVars);
		List<Node> nodeList = parse(ta, posTags, quantities, candidateVars, nodes);
		for(Node node : nodeList) {
			if(node.label.equals("EQ")) {
				Equation eq = new Equation();
				eq.root = node;
				return eq;
			}
		}
		return null;
	}
	
	public static List<Node> parse(TextAnnotation ta, List<Constituent> posTags, 
			List<QuantSpan> quantities, List<IntPair> candidateVars, List<Node> leaves) {
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
				parse(ta, posTags, quantities, candidateVars, i, j, cky, leaves);
			}
		}
		return cky.get(0).get(n);
	}
	
	public static void parse(TextAnnotation ta, List<Constituent> posTags, 
			List<QuantSpan> quantities, List<IntPair> candidateVars,  
			int start, int end, 
			List<List<List<Node>>> cky,
			List<Node> leaves) {
		for(int i=start+1; i<end; ++i) {
			parse(ta, posTags, quantities, candidateVars, start, i, end, cky, leaves);
		}
	}
	
	public static void parse(TextAnnotation ta, List<Constituent> posTags, 
			List<QuantSpan> quantities, List<IntPair> candidateVars,
			int start, int mid, int end, 
			List<List<List<Node>>> cky,
			List<Node> leaves) {
		String midPhrase = ta.getText().toLowerCase().substring(
				leaves.get(mid-1).charIndex, leaves.get(mid).charIndex);
		boolean verbInMidPhrase = false;
		for(int i=ta.getTokenIdFromCharacterOffset(leaves.get(mid-1).charIndex);
				i<ta.getTokenIdFromCharacterOffset(leaves.get(mid).charIndex);
				++i) {
			if(posTags.get(i).getLabel().startsWith("VB")) {
				verbInMidPhrase = true;
				break;
			}
		}
		String prePhrase = "";
		if(start == 0) prePhrase = ta.getText().toLowerCase().substring(
				0, leaves.get(start).charIndex);
		else prePhrase = ta.getText().toLowerCase().substring(
				leaves.get(start-1).charIndex, leaves.get(start).charIndex);
		String leftToken = "";
		if(start+1 == mid && leaves.get(start).label.equals("NUM")) {
			QuantSpan qs = quantities.get(leaves.get(start).index);
			leftToken = ta.getText().toLowerCase().substring(qs.start, qs.end);
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
