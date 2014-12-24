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
		
		PairComparator<RelationY> latticePairComparator = 
				new PairComparator<RelationY>() {};
		BoundedPriorityQueue<Pair<RelationY, Double>> beam1 = 
				new BoundedPriorityQueue<Pair<RelationY, Double>>(50, latticePairComparator);
		BoundedPriorityQueue<Pair<RelationY, Double>> beam2 = 
				new BoundedPriorityQueue<Pair<RelationY, Double>>(50, latticePairComparator);
		
		List<String> relations = Arrays.asList("R1", "R2", "BOTH", "NONE");
		beam1.add(new Pair<RelationY, Double>(new RelationY(), 0.0));
		for(int i=0; i<prob.quantities.size(); ++i) {
			for(String relation : relations) {
				for(Pair<RelationY, Double> pair : beam1) {
					RelationY relationY = new RelationY(pair.getFirst());
					relationY.addRelation(relation);
					beam2.add(new Pair<RelationY, Double>(relationY, pair.getSecond()+
							wv.dotProduct(featGen.getFeatureVector(prob, relationY, i))));
				}
			}
			beam1.clear();
			beam1.addAll(beam2);
			beam2.clear();
		}
		System.out.println("Inferred : "+beam2.element().getFirst());
		return beam2.element().getFirst();
	}
}
