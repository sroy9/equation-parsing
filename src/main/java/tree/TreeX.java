package tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import structure.Node;
import structure.SimulProb;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.IInstance;

public class TreeX implements IInstance {

	public int problemIndex;
	public TextAnnotation ta;
	public List<Constituent> posTags;
	public List<Constituent> lemmas;
	public List<Constituent> parse;
	public List<QuantSpan> quantities;
	public List<Pair<String, IntPair>> skeleton;
	public List<Integer> relevantQuantIndices;
	
	public TreeX(SimulProb simulProb) {
		quantities = simulProb.quantities;
		problemIndex = simulProb.index;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		parse = simulProb.parse;
		lemmas = simulProb.lemmas;
		skeleton = simulProb.skeleton;
		relevantQuantIndices = new ArrayList<Integer>();
		for(Node leaf : simulProb.equation.root.getLeaves()) {
			if(leaf.label.equals("NUM")) {
				for(int i=0; i<simulProb.quantities.size(); ++i) {
					if(Tools.safeEquals(leaf.value, Tools.getValue(
							simulProb.quantities.get(i)))) {
						relevantQuantIndices.add(i);
						break;
					}
				}
			}
		}
	}
	
}
