package partition;

import java.util.ArrayList;
import java.util.List;

import structure.KnowledgeBase;
import structure.SimulProb;
import structure.Trigger;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.IInstance;

public class PartitionX implements IInstance {

	public int problemIndex;
	public TextAnnotation ta;
	public List<Constituent> posTags;
	public List<Constituent> lemmas;
	public List<Constituent> parse;
	public List<Constituent> dependencyParse;
	public List<Pair<String, IntPair>> skeleton;
	public List<QuantSpan> quantities;
	public List<Trigger> triggers;
	public int index1, index2;
	
	public PartitionX(SimulProb simulProb, int index1, int index2) 
			throws Exception {
		problemIndex = simulProb.index;
		quantities = simulProb.quantities;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		lemmas = simulProb.lemmas;
		parse = simulProb.parse;
		skeleton = simulProb.skeleton;
		triggers = simulProb.triggers;
		this.index1 = index1;
		this.index2 = index2;
	}
	
}
