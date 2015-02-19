package semparse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import partition.PartitionY;
import structure.Equation;
import structure.KnowledgeBase;
import structure.Node;
import structure.SimulProb;
import structure.Trigger;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.IInstance;

public class SemX implements IInstance {

	public int problemIndex;
	public TextAnnotation ta;
	public List<Constituent> posTags;
	public List<Constituent> lemmas;
	public List<Constituent> parse;
	public List<QuantSpan> quantities;
	public List<Pair<String, IntPair>> skeleton;
	public List<Trigger> triggers;
	public Map<Integer, Boolean> partitions;
	public IntPair eqSpan;
	
	public SemX(SimulProb simulProb, IntPair span) {
		quantities = simulProb.quantities;
		problemIndex = simulProb.index;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		parse = simulProb.parse;
		lemmas = simulProb.lemmas;
		skeleton = simulProb.skeleton;
		triggers = simulProb.triggers;
		eqSpan = span;
	}
	
}
