package var;

import java.util.List;

import structure.SimulProb;
import tree.TreeX;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.IInstance;

public class VarX implements IInstance {

	public int problemIndex;
	public TextAnnotation ta;
	public List<Constituent> posTags;
//	public List<Constituent> lemmas;
	public List<Constituent> parse;
	public List<QuantSpan> quantities;
	public List<IntPair> candidateVars;
	
	public VarX(SimulProb simulProb) {
		quantities = simulProb.quantities;
		problemIndex = simulProb.index;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		candidateVars = simulProb.candidateVars;
	}
	
	public VarX(TreeX simulProb) {
		quantities = simulProb.quantities;
		problemIndex = simulProb.problemIndex;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		candidateVars = simulProb.candidateVars;
	}
	
}
