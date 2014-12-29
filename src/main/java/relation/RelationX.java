package relation;

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

public class RelationX implements IInstance {

	public int problemIndex;
	public TextAnnotation ta;
	public List<Constituent> posTags;
	public List<Constituent> lemmas;
	public List<QuantSpan> quantities;
	public List<String> relations;
	public List<Pair<String, IntPair>> skeleton;
	public int index;

	public RelationX(SimulProb simulProb, int index) {
		quantities = simulProb.quantities;
		problemIndex = simulProb.index;
		this.index = index;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		lemmas = simulProb.lemmas;
		skeleton = simulProb.skeleton;
		relations = new ArrayList<>();
		for(int i=0; i<index; i++) {
			relations.add(simulProb.relations.get(i));
		}
	}
}
