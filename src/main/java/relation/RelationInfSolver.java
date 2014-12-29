package relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import structure.Equation;
import structure.EquationSolver;
import structure.Operation;
import structure.PairComparator;
import utils.Tools;
import edu.illinois.cs.cogcomp.core.datastructures.BoundedPriorityQueue;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class RelationInfSolver extends AbstractInferenceSolver implements
		Serializable {

	private static final long serialVersionUID = 5253748728743334706L;
	private RelationFeatGen featGen;

	public RelationInfSolver(RelationFeatGen featGen) {
		this.featGen = featGen;
	}

	@Override
	public IStructure getBestStructure(WeightVector wv, IInstance x)
			throws Exception {
		return getLossAugmentedBestStructure(wv, x, null);
	}
		
	@Override
	public float getLoss(IInstance arg0, IStructure arg1, IStructure arg2) {
		RelationY r1 = (RelationY) arg1;
		RelationY r2 = (RelationY) arg2;
		return RelationY.getLoss(r1, r2);
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector wv,
			IInstance x, IStructure goldStructure) throws Exception {
		RelationX prob = (RelationX) x;
		RelationY gold = (RelationY) goldStructure;
		RelationY pred = new RelationY();
		boolean noR = true;
		for(int i=0; i<prob.index; ++i) {
			if(prob.relations.get(i).startsWith("R")) {
				noR = false;
				break;
			}
		}
		
		List<String> relations = null;
		if(noR) relations = Arrays.asList("R1", "BOTH", "NONE");
		else relations = Arrays.asList("R1", "R2", "BOTH", "NONE");
		
		double maxScore = -Double.MAX_VALUE, score;
		String bestRelation = null;
		for(String relation : relations) {
			pred = new RelationY(relation);
			score = wv.dotProduct(featGen.getFeatureVector(prob, pred)) + 
					(goldStructure == null ? 0 : RelationY.getLoss(gold, pred));
			if(score > maxScore) {
				maxScore = score;
				bestRelation = relation;
			}
		}
		pred = new RelationY(bestRelation);
		
		return pred;
	}
}
