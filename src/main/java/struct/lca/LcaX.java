package struct.lca;

import java.util.List;
import java.util.Map;

import lasttwo.LastTwoX;
import structure.Node;
import structure.SimulProb;
import tree.TreeX;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.IInstance;

public class LcaX implements IInstance {

	public int problemIndex;
	public TextAnnotation ta;
	public List<Constituent> posTags;
	public List<Constituent> lemmas;
	public List<Constituent> parse;
	public List<QuantSpan> quantities;
	public List<IntPair> candidateVars;
	public Map<String, List<Integer>> varTokens;
	public List<Node> nodes;
	
	public LcaX(SimulProb simulProb, Map<String, List<Integer>> varTokens,
			List<Node> nodes) {
		quantities = simulProb.quantities;
		problemIndex = simulProb.index;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		candidateVars = simulProb.candidateVars;
		this.varTokens = varTokens;
		this.nodes = nodes;
	}
	
	public LcaX(TreeX simulProb, Map<String, List<Integer>> varTokens,
			List<Node> nodes) {
		quantities = simulProb.quantities;
		problemIndex = simulProb.problemIndex;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		candidateVars = simulProb.candidateVars;
		this.varTokens = varTokens;
		this.nodes = nodes;
	}
	
	public LcaX(LastTwoX simulProb, Map<String, List<Integer>> varTokens,
			List<Node> nodes) {
		quantities = simulProb.quantities;
		problemIndex = simulProb.problemIndex;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		candidateVars = simulProb.candidateVars;
		this.varTokens = varTokens;
		this.nodes = nodes;
	}
	
}
