package relevance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.MinMaxPriorityQueue;

import structure.Equation;
import structure.KnowledgeBase;
import structure.Node;
import structure.PairComparator;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class RelInfSolver extends AbstractInferenceSolver implements
		Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	private RelFeatGen featGen;

	public RelInfSolver(RelFeatGen featGen) 
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
		RelY r1 = (RelY) arg1;
		RelY r2 = (RelY) arg2;
		return RelY.getLoss(r1, r2);
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector wv,
			IInstance x, IStructure goldStructure) throws Exception {
		RelX prob = (RelX) x;
		double score0 = wv.dotProduct(featGen.getFeatureVector(prob, new RelY(0)));
		double score1 = wv.dotProduct(featGen.getFeatureVector(prob, new RelY(1)));
		if(score0 > score1) return new RelY(0);
		else return new RelY(1);
	}
	
}
