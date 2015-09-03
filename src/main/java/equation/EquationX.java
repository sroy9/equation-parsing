package equation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import structure.Node;
import structure.SimulProb;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.IInstance;

public class EquationX implements IInstance {

	public int problemIndex;
	public TextAnnotation ta;
	public List<Constituent> posTags;
	public List<Constituent> lemmas;
	public List<Constituent> parse;
	public List<QuantSpan> quantities;
	public List<IntPair> candidateVars;
	public Map<String, List<Integer>> varTokens;
	public List<Node> nodes;
 	
	public EquationX(SimulProb simulProb, Map<String, List<Integer>> varTokens) {
		quantities = simulProb.quantities;
		problemIndex = simulProb.index;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		parse = simulProb.parse;
		lemmas = simulProb.lemmas;
		candidateVars = simulProb.candidateVars;
		this.varTokens = varTokens;
		nodes = new ArrayList<Node>();
		for(Node leaf : simulProb.equation.root.getLeaves()) {
			if(leaf.label.equals("NUM")) {
				for(int i=0; i<simulProb.quantities.size(); ++i) {
					if(Tools.safeEquals(Tools.getValue(simulProb.quantities.get(i)), leaf.value)) {
						Node node = new Node("NUM", i, new ArrayList<Node>());
						node.value = leaf.value;
						nodes.add(node);
						break;
					}
				}
			}
			if(leaf.label.equals("VAR")) {
				for(String key : varTokens.keySet()) {
					if(varTokens.get(key).size() == 0) continue;
					if(!leaf.varId.equals(key)) continue;
					Node node = new Node("VAR", varTokens.get(key).get(0), new ArrayList<Node>());
					node.varId = leaf.varId;
					nodes.add(node);
					break;
				}
			}
		}
	}
	

	public IntPair getSpanningTokenIndices(Node node) {
		List<Node> leaves = node.getLeaves();
		int min = 1000, max = -1;
		for(Node leaf : leaves) {
			IntPair ip = null;
			if(leaf.label.equals("VAR")) {
				ip = candidateVars.get(leaf.index);
			} else {
				QuantSpan qs = quantities.get(leaf.index);
				ip = new IntPair(ta.getTokenIdFromCharacterOffset(qs.start), 
						ta.getTokenIdFromCharacterOffset(qs.end));
			}
			if(ip.getSecond() > max) max = ip.getSecond();
			if(ip.getFirst() < min) min = ip.getFirst();
		}
		return new IntPair(min, max);
	}
	
}
