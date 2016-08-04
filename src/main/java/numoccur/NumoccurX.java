package numoccur;

import java.util.List;

import joint.JointX;
import structure.SimulProb;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.IInstance;

public class NumoccurX implements IInstance {

	public int problemIndex;
	public TextAnnotation ta;
	public List<Constituent> posTags;
	public List<QuantSpan> quantities;
	
	public NumoccurX(SimulProb simulProb) {
		quantities = simulProb.quantities;
		problemIndex = simulProb.index;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
	}
	
	public NumoccurX(JointX x) {
		quantities = x.quantities;
		problemIndex = x.problemIndex;
		ta = x.ta;
		posTags = x.posTags;
	}
	
}
