package joint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import partition.PartitionY;
import structure.Equation;
import structure.KnowledgeBase;
import structure.SimulProb;
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
	public List<Pair<Integer, String>> triggers;
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
				int index1 = simulProb.triggers.get(i).getFirst();
				int index2 = simulProb.triggers.get(i+1).getFirst();
				if(simulProb.ta.getSentenceFromToken(index1) == 
						simulProb.ta.getSentenceFromToken(index2)) {
					partitions.put(i, true);
					for(Pair<String, IntPair> pair : simulProb.nodes) {
						if(pair.getSecond().getFirst() <= i && 
								pair.getSecond().getSecond() > i+1) {
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
					int dist = Math.abs(triggers.get(j).getFirst() - i);
					if(dist < minDist) {
						minDist = dist;
						pivot = j;
					}
				}
				int start = pivot, end = pivot+1;
				for(int j=start-1; j>=0; --j) {
					int index1 = triggers.get(j).getFirst();
					int index2 = triggers.get(j+1).getFirst();
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
					int index1 = triggers.get(j-1).getFirst();
					int index2 = triggers.get(j).getFirst();
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
				i = triggers.get(end-1).getFirst()+1;
			}	
		}
	}
}
