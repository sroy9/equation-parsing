package numoccur;

import java.util.List;

import structure.SimulProb;
import tree.TreeX;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.IInstance;

public class NumoccurX implements IInstance {

	public int problemIndex;
	public TextAnnotation ta;
	public List<Constituent> posTags;
	public List<Constituent> lemmas;
	public List<Constituent> parse;
	public List<QuantSpan> quantities;
	public int quantIndex;
	
	public NumoccurX(SimulProb simulProb, int quantIndex) {
		quantities = simulProb.quantities;
		problemIndex = simulProb.index;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		parse = simulProb.parse;
		lemmas = simulProb.lemmas;
		this.quantIndex = quantIndex;
	}
	
	public NumoccurX(TreeX x, int quantIndex) {
		quantities = x.quantities;
		problemIndex = x.problemIndex;
		ta = x.ta;
		posTags = x.posTags;
		parse = x.parse;
		lemmas = x.lemmas;
		this.quantIndex = quantIndex;
	}
	
}
