package semparse;

import java.util.ArrayList;
import java.util.List;

import structure.KnowledgeBase;
import structure.SimulProb;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.IInstance;

public class SemX implements IInstance {

	public int problemIndex;
	public TextAnnotation ta;
	public List<Constituent> posTags;
	public List<Constituent> lemmas;
	public List<Constituent> parse;
	public List<Constituent> dependencyParse;
	public List<Pair<String, IntPair>> skeleton;
	public List<QuantSpan> quantities;
	public List<Integer> mathyTokenIndices;
	
	public SemX(SimulProb simulProb) throws Exception {
		problemIndex = simulProb.index;
		quantities = simulProb.quantities;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		lemmas = simulProb.lemmas;
		parse = simulProb.parse;
		skeleton = simulProb.skeleton;
		mathyTokenIndices = new ArrayList<>();
		for(int i=0; i<ta.size(); ++i) {
			String token = ta.getToken(i);
			if(KnowledgeBase.mathWordSet.contains(token)) {
				mathyTokenIndices.add(i);
			}
		}
	}
	
}
