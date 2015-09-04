package lca;

import java.util.List;

import structure.Node;
import structure.SimulProb;
import tree.TreeX;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
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
	public Node leaf1, leaf2;
	
	public LcaX(SimulProb simulProb, Node leaf1, Node leaf2) {
		quantities = simulProb.quantities;
		problemIndex = simulProb.index;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		parse = simulProb.parse;
		lemmas = simulProb.lemmas;
		candidateVars = simulProb.candidateVars;
		this.leaf1 = leaf1;
		this.leaf2 = leaf2;
	}
	
	public LcaX(TreeX simulProb, Node leaf1, Node leaf2) {
		quantities = simulProb.quantities;
		problemIndex = simulProb.problemIndex;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		parse = simulProb.parse;
		lemmas = simulProb.lemmas;
		candidateVars = simulProb.candidateVars;
		this.leaf1 = leaf1;
		this.leaf2 = leaf2;
	}
	
	
	
}