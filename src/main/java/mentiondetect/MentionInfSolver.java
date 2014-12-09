package mentiondetect;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.BoundedPriorityQueue;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class MentionInfSolver extends AbstractInferenceSolver implements Serializable{

	private static final long serialVersionUID = -5897765443533109252L;
	private MentionFeatureExtractor featGen;

	public MentionInfSolver(MentionFeatureExtractor featGen) {
		this.featGen = featGen;
	}

	@Override
	public IStructure getBestStructure(WeightVector weight, IInstance ins)
			throws Exception {
		return getLossAugmentedBestStructure(weight, ins, null);
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector weight,
			IInstance ins, IStructure goldStructure) throws Exception {
		LabelSet gold = (LabelSet) goldStructure;
		VarSet varSet = (VarSet) ins;
		if(goldStructure != null) {
			assert varSet.quantities.size() == gold.labels.size();
		}
		List<String> labels = Arrays.asList("E1", "E2", "E3", "O");
		int numOfQuantities = varSet.quantities.size();
		BoundedPriorityQueue<Pair<LabelSet, Double>> beam = 
				new BoundedPriorityQueue<>(50);
		List<Pair<LabelSet, Double>> presentBeam = new ArrayList<>();
		beam.add(new Pair<LabelSet, Double>(new LabelSet(), 0.0));
		for(int i=0; i<numOfQuantities; i++) {
			presentBeam.clear();
			Iterator<Pair<LabelSet, Double>> it = beam.iterator();
			while(it.hasNext()) {
				presentBeam.add(it.next());
			}
			beam.clear();
			for(Pair<LabelSet, Double> pair : presentBeam) {
				for(String label : labels) {
					LabelSet labelSet = new LabelSet(pair.getFirst());
					labelSet.addLabel(label);
					Double score = pair.getSecond() + weight.dotProduct(
							featGen.getFeatureVector(varSet, labelSet, i))
							+ ((goldStructure!=null && 
							!labelSet.labels.get(i).equals(gold.labels.get(i)))?1:0);
					assert labelSet.labels.size() == i;
					beam.add(new Pair<LabelSet, Double>(labelSet, score));
				}
			}	
		}
		return beam.element().getFirst();
	}

	@Override
	public float getLoss(IInstance ins, IStructure gold, IStructure pred) {
		LabelSet goldLabelSet = (LabelSet) gold;
		LabelSet predLabelSet = (LabelSet) pred;
		assert goldLabelSet.labels.size() == predLabelSet.labels.size();
		float hammingLoss = 0;
		for(int i = 0; i < goldLabelSet.labels.size(); i++) {
			if(!goldLabelSet.labels.get(i).equals(predLabelSet.labels.get(i))) {
				hammingLoss += 1.0;
			}
		}
		return hammingLoss;
	}
	

}