package semparse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import structure.SimulProb;
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
	public List<QuantSpan> relationQuantities;
	public List<Constituent> posTags;
	public List<Constituent> lemmas;
	public List<Constituent> parse;
	public List<Constituent> dependencyParse;
	public List<Pair<String, IntPair>> skeleton;
	public List<QuantSpan> quantities;
	public boolean isOneVar;

	public SemX(SimulProb simulProb, String relation) throws Exception {
		problemIndex = simulProb.index;
		quantities = simulProb.quantities;
		relationQuantities = new ArrayList<>();
		isOneVar = simulProb.isOneVar;
		for(int i=0; i<simulProb.relations.size(); ++i) {
			String str = simulProb.relations.get(i);
			if(str.equals(relation) || str.equals("BOTH")) {
				relationQuantities.add(quantities.get(i));
			}
		}
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		lemmas = simulProb.lemmas;
		parse = simulProb.parse;
		skeleton = simulProb.skeleton;
	}
}
