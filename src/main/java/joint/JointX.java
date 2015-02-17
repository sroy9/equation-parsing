package joint;

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

public class JointX implements IInstance {

	public int problemIndex;
	public TextAnnotation ta;
	public List<Constituent> posTags;
	public List<Constituent> lemmas;
	public List<Constituent> parse;
	public List<QuantSpan> quantities;
	public List<Pair<String, IntPair>> skeleton;
	public List<Trigger> triggers;
	public Map<Integer, Boolean> partitions;
	public List<IntPair> eqSpans;
	
	public JointX(SimulProb simulProb, boolean useGold) {
		quantities = simulProb.quantities;
		problemIndex = simulProb.index;
		ta = simulProb.ta;
		posTags = simulProb.posTags;
		parse = simulProb.parse;
		lemmas = simulProb.lemmas;
		skeleton = simulProb.skeleton;
		triggers = simulProb.triggers;
		partitions = new HashMap<Integer, Boolean>();
		if(useGold) {
			for(int i=0; i<simulProb.triggers.size()-1; ++i) {
				int index1 = simulProb.triggers.get(i).index;
				int index2 = simulProb.triggers.get(i+1).index;
				if(simulProb.ta.getSentenceFromToken(index1) == 
						simulProb.ta.getSentenceFromToken(index2)) {
					partitions.put(i, true);
					for(Node pair : simulProb.nodes) {
						if(pair.span.getFirst() <= i && 
								pair.span.getSecond() > i+1) {
							partitions.put(i, false);
							break;
						}
					}
				}
			}
		}
		eqSpans = new ArrayList<>();
		extractEqSpans();
	}
	
	public void extractEqSpans() {
		for(int i=0; i<ta.size(); ++i) {
			if(KnowledgeBase.mathIndicatorSet.contains(
					ta.getToken(i).toLowerCase())) {
				int minDist = Integer.MAX_VALUE;
				int pivot = -1;
				for(int j=0; j<triggers.size(); j++) {
					int dist = Math.abs(triggers.get(j).index - i);
					if(dist < minDist) {
						minDist = dist;
						pivot = j;
					}
				}
				int start = pivot, end = pivot+1;
				for(int j=start-1; j>=0; --j) {
					int index1 = triggers.get(j).index;
					int index2 = triggers.get(j+1).index;
					if(ta.getSentenceFromToken(index1) == 
							ta.getSentenceFromToken(index2) && 
							partitions.containsKey(j) &&
							partitions.get(j) == false) {
						start = j;
					} else {
						break;
					}
				}
				for(int j=end; j<triggers.size(); ++j) {
					int index1 = triggers.get(j-1).index;
					int index2 = triggers.get(j).index;
					if(ta.getSentenceFromToken(index1) == 
							ta.getSentenceFromToken(index2) && 
							partitions.containsKey(j-1) &&
							partitions.get(j-1) == false) {
						end = j+1;
					} else {
						break;
					}
				}
				eqSpans.add(new IntPair(start, end));
				i = triggers.get(end-1).index+1;
			}	
		}
	}
}
