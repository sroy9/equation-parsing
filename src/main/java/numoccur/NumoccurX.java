package numoccur;

import java.util.List;

import structure.SimulProb;
import tree.TreeX;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.IInstance;

public class NumoccurX implements IInstance {

	public int problemIndex;
	public TextAnnotation ta;
	public List<Constituent> posTags;
	public List<QuantSpan> quantities;
	public int quantIndex;
	
	public NumoccurX(SimulProb simulProb, int quantIndex) {
		quantities = simulProb.quantities;
		problemIndex = simulProb.index;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		this.quantIndex = quantIndex;
	}
	
	public NumoccurX(TreeX x, int quantIndex) {
		quantities = x.quantities;
		problemIndex = x.problemIndex;
		ta = x.ta;
		posTags = x.posTags;
		this.quantIndex = quantIndex;
	}
	
	public NumoccurX(struct.numoccur.NumoccurX x, int quantIndex) {
		quantities = x.quantities;
		problemIndex = x.problemIndex;
		ta = x.ta;
		posTags = x.posTags;
		this.quantIndex = quantIndex;
	}
	
}
