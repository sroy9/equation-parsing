package numoccur;

import java.io.Serializable;
import java.util.ArrayList;

import com.google.common.collect.MinMaxPriorityQueue;

import structure.PairComparator;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class NumoccurInfSolver extends AbstractInferenceSolver implements
		Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	private NumoccurFeatGen featGen;

	public NumoccurInfSolver(NumoccurFeatGen featGen) 
			throws Exception {
		this.featGen = featGen;
	}

	@Override
	public IStructure getBestStructure(WeightVector wv, IInstance x)
			throws Exception {
		return getLossAugmentedBestStructure(wv, x, null);
	}
		
	@Override
	public float getLoss(IInstance arg0, IStructure arg1, IStructure arg2) {
		NumoccurY r1 = (NumoccurY) arg1;
		NumoccurY r2 = (NumoccurY) arg2;
		return NumoccurY.getLoss(r1, r2);
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector wv,
			IInstance x, IStructure goldStructure) throws Exception {
		PairComparator<NumoccurY> pairComparator = 
				new PairComparator<NumoccurY>() {};
		MinMaxPriorityQueue<Pair<NumoccurY, Double>> beam1 = 
				MinMaxPriorityQueue.orderedBy(pairComparator).
				maximumSize(200).create();
		MinMaxPriorityQueue<Pair<NumoccurY, Double>> beam2 = 
				MinMaxPriorityQueue.orderedBy(pairComparator).
				maximumSize(200).create();
		NumoccurX prob = (NumoccurX) x;
		beam1.add(new Pair<NumoccurY, Double>(new NumoccurY(new ArrayList<Integer>()), 0.0));
		// Predict number of occurrences of each quantity
		for(int i=0; i<prob.quantities.size(); ++i) {
			for(Pair<NumoccurY, Double> pair : beam1) {
				for(int j=0; j<3; ++j) {
					double score = wv.dotProduct(featGen.getIndividualFeatureVector(prob, i, j));
					NumoccurY y = new NumoccurY(pair.getFirst());
					y.numOccurList.add(j);
					beam2.add(new Pair<NumoccurY, Double>(y, pair.getSecond()+score));
				}
			}
			beam1.clear();
			beam1.addAll(beam2);
			beam2.clear();
		}
		for(Pair<NumoccurY, Double> pair : beam1) {
			beam2.add(new Pair<NumoccurY, Double>(pair.getFirst(), pair.getSecond() + 
					wv.dotProduct(featGen.getGlobalFeatureVector(prob, pair.getFirst()))));
		}
		return beam2.element().getFirst();
	}
	
}
